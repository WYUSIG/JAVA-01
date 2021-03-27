## Spring-Cloud-Gateway源码系列学习

版本 v2.2.6.RELEASE





### 默认的GlobalFilter都有哪些

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/1616746517(1).jpg)

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/20210326180111.png)

根据debug可以得出以下order顺序表格，一个请求都会经过这些GlobalFilter，这些 GlobalFilter 会在 FilteringWebHandler 通过 GatewayFilterAdapter 适配成GatewayFilter

| GlobalFilter类              | order                    |
| --------------------------- | ------------------------ |
| RemoveCachedBodyFilter      | Integer.MIN_VALUE        |
| AdaptCachedBodyGlobalFilter | Integer.MIN_VALUE + 1000 |
| NettyWriteResponseFilter    | -1                       |
| ForwardPathFilter           | 0                        |
| GatewayMetricsFilter        | 0                        |
| RouteToRequestUrlFilter     | 10000                    |
| LoadBalancerClientFilter    | 10100                    |
| WebsocketRoutingFilter      | Integer.MAX_VALUE        |
| NettyRoutingFilter          | Integer.MAX_VALUE        |
| ForwardRoutingFilter        | Integer.MAX_VALUE        |

>tip：其中最应该关注的分别是NettyWriteResponseFilter和NettyRoutingFilter，NettyRoutingFilter负责发送请求到route目标网址(需要合并)，而NettyWriteResponseFilter负责把响应结果发回客户端(根据是否是流数据，分别处理)

### RemoveCachedBodyFilter源码分析

>在流结束之前移除exchange上下文里的CACHED_REQUEST_BODY_ATTR

```java
public class RemoveCachedBodyFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(RemoveCachedBodyFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //注意doFinally，相当于try finally,在流结束之前做的，也就是最后才移除CACHED_REQUEST_BODY_ATTR
		return chain.filter(exchange).doFinally(s -> {
            //倒数第一执行
			Object attribute = exchange.getAttributes().remove(CACHED_REQUEST_BODY_ATTR);
			if (attribute != null && attribute instanceof PooledDataBuffer) {
				PooledDataBuffer dataBuffer = (PooledDataBuffer) attribute;
				if (dataBuffer.isAllocated()) {
					if (log.isTraceEnabled()) {
						log.trace("releasing cached body in exchange attribute");
					}
					dataBuffer.release();
				}
			}
		});
	}

    //在Ordered接口里面 int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;最高优先级
	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
```

### AdaptCachedBodyGlobalFilter源码分析

>这一步主要是判断需不需要进行RequestBody的缓存，如果有缓存则对exchange进行一些调整(替换成缓存的)

```java
public class AdaptCachedBodyGlobalFilter
		implements GlobalFilter, Ordered, ApplicationListener<EnableBodyCachingEvent> {

    //记录需要缓存的 routeId
	private ConcurrentMap<String, Boolean> routesToCache = new ConcurrentHashMap<>();

    //通过事件驱动 更新需要缓存的 routeId
	@Override
	public void onApplicationEvent(EnableBodyCachingEvent event) {
		this.routesToCache.putIfAbsent(event.getRouteId(), true);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// the cached ServerHttpRequest is used when the ServerWebExchange can not be
		// mutated, for example, during a predicate where the body is read, but still
		// needs to be cached.
        //如果RequestBody和Request都缓存的话，将会获得到，@see ServerWebExchangeUtils#cacheRequestBodyAndRequest return执行方法的第二个参数
		ServerHttpRequest cachedRequest = exchange
				.getAttributeOrDefault(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR, null);
		if (cachedRequest != null) {
            //移除
			exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
            //替换exchange 的 request 为 缓存的 cachedRequest
			return chain.filter(exchange.mutate().request(cachedRequest).build());
		}

		//获取 CACHED_REQUEST_BODY_ATTR
		DataBuffer body = exchange.getAttributeOrDefault(CACHED_REQUEST_BODY_ATTR, null);
        //获取 Route
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        //如果 body 不为空 或者 Route不需要缓存 直接开始下一个filter
		if (body != null || !this.routesToCache.containsKey(route.getId())) {
			return chain.filter(exchange);
		}
		
        //ServerWebExchangeUtils.cacheRequestBody就是缓存RequestBody，但不会缓存整个Request，也就是不会保存CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR
		return ServerWebExchangeUtils.cacheRequestBody(exchange, (serverHttpRequest) -> {
			// don't mutate and build if same request object
            // 如果相等则不需要修改exchange的request
			if (serverHttpRequest == exchange.getRequest()) {
				return chain.filter(exchange);
			}
            //否则request进行替换
			return chain.filter(exchange.mutate().request(serverHttpRequest).build());
		});
	}

    //Integer.MIN_VALUE + 1000
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1000;
	}

}
```

### NettyWriteResponseFilter源码分析

>跟NettyRoutingFilter一对的，NettyRoutingFilter负责请求目标地址返回响应结果，NettyWriteResponseFilter负责把响应结果发回给客户端

```java
public class NettyWriteResponseFilter implements GlobalFilter, Ordered {

	/**
	 * Order for write response filter.
	 */
	public static final int WRITE_RESPONSE_FILTER_ORDER = -1;

	private static final Log log = LogFactory.getLog(NettyWriteResponseFilter.class);

	private final List<MediaType> streamingMediaTypes;

	public NettyWriteResponseFilter(List<MediaType> streamingMediaTypes) {
		this.streamingMediaTypes = streamingMediaTypes;
	}

    // -1
	@Override
	public int getOrder() {
		return WRITE_RESPONSE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// NOTICE: nothing in "pre" filter stage as CLIENT_RESPONSE_CONN_ATTR is not added
		// until the NettyRoutingFilter is run
		// @formatter:off
		return chain.filter(exchange)
				.doOnError(throwable -> cleanup(exchange))
				.then(Mono.defer(() -> {
                    //倒数第二执行这里，注意.then
                    //获得NettyRoutingFilter返回的Response
					Connection connection = exchange.getAttribute(CLIENT_RESPONSE_CONN_ATTR);
					if (connection == null) {
                        //如果NettyRoutingFilter 返回的 connection为null，直接返回空
						return Mono.empty();
					}
					if (log.isTraceEnabled()) {
						log.trace("NettyWriteResponseFilter start inbound: "
								+ connection.channel().id().asShortText() + ", outbound: "
								+ exchange.getLogPrefix());
					}
                    // 
					ServerHttpResponse response = exchange.getResponse();

					// TODO: needed?
                    //获得connection的inbound数据流，并转成DataBuffer类型元素
					final Flux<DataBuffer> body = connection
							.inbound()
							.receive()
							.retain()
							.map(byteBuf -> wrap(byteBuf, response));

                    //获取响应内容类型
					MediaType contentType = null;
					try {
						contentType = response.getHeaders().getContentType();
					}
					catch (Exception e) {
						if (log.isTraceEnabled()) {
							log.trace("invalid media type", e);
						}
					}
                    //针对流类型按流和普通的分表处理
					return (isStreamingMediaType(contentType)
							? response.writeAndFlushWith(body.map(Flux::just))
							: response.writeWith(body));
				})).doOnCancel(() -> cleanup(exchange));
		// @formatter:on
	}

    //把netty请求的ByteBuf转成DataBuffer
	protected DataBuffer wrap(ByteBuf byteBuf, ServerHttpResponse response) {
		DataBufferFactory bufferFactory = response.bufferFactory();
		if (bufferFactory instanceof NettyDataBufferFactory) {
			NettyDataBufferFactory factory = (NettyDataBufferFactory) bufferFactory;
			return factory.wrap(byteBuf);
		}
		// MockServerHttpResponse creates these
		else if (bufferFactory instanceof DefaultDataBufferFactory) {
			DataBuffer buffer = ((DefaultDataBufferFactory) bufferFactory)
					.allocateBuffer(byteBuf.readableBytes());
			buffer.write(byteBuf.nioBuffer());
			byteBuf.release();
			return buffer;
		}
		throw new IllegalArgumentException(
				"Unkown DataBufferFactory type " + bufferFactory.getClass());
	}

	private void cleanup(ServerWebExchange exchange) {
		Connection connection = exchange.getAttribute(CLIENT_RESPONSE_CONN_ATTR);
		if (connection != null) {
			connection.dispose();
		}
	}

	// TODO: use framework if possible
	// TODO: port to WebClientWriteResponseFilter
	private boolean isStreamingMediaType(@Nullable MediaType contentType) {
		return (contentType != null && this.streamingMediaTypes.stream()
				.anyMatch(contentType::isCompatibleWith));
	}

}
```

### ForwardPathFilter源码分析

>判断是不是forward，是则替换request的path

```java
public class ForwardPathFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Route
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        //获取Uri
		URI routeUri = route.getUri();
        //获取协议，如https
		String scheme = routeUri.getScheme();
        //如果已经处理了(可以看看NettyRoutingFilter#filter)或者不是转发的
		if (isAlreadyRouted(exchange) || !"forward".equals(scheme)) {
            //下个filter
			return chain.filter(exchange);
		}
        //如果forward则替换path
		exchange = exchange.mutate()
				.request(exchange.getRequest().mutate().path(routeUri.getPath()).build())
				.build();
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
```

### RouteToRequestUrlFilter源码分析

>该类主要是拼接目标uri，例如 request = http://localhost:8080/111, route.uri = https://www.baidu.com，则mergeuri = https://www.baidu.com/111

```java
public class RouteToRequestUrlFilter implements GlobalFilter, Ordered {

	/**
	 * Order of Route to URL.
	 */
	public static final int ROUTE_TO_URL_FILTER_ORDER = 10000;

	private static final Log log = LogFactory.getLog(RouteToRequestUrlFilter.class);

	private static final String SCHEME_REGEX = "[a-zA-Z]([a-zA-Z]|\\d|\\+|\\.|-)*:.*";
	static final Pattern schemePattern = Pattern.compile(SCHEME_REGEX);

	/* for testing */
	static boolean hasAnotherScheme(URI uri) {
		return schemePattern.matcher(uri.getSchemeSpecificPart()).matches()
				&& uri.getHost() == null && uri.getRawPath() == null;
	}

	@Override
	public int getOrder() {
		return ROUTE_TO_URL_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Route
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
		if (route == null) {
			return chain.filter(exchange);
		}
		log.trace("RouteToRequestUrlFilter start");
        //获取请求uri
		URI uri = exchange.getRequest().getURI();
		boolean encoded = containsEncodedParts(uri);
        //获取目标uri
		URI routeUri = route.getUri();

        //如果是其他协议
		if (hasAnotherScheme(routeUri)) {
			// this is a special url, save scheme to special attribute
			// replace routeUri with schemeSpecificPart
			exchange.getAttributes().put(GATEWAY_SCHEME_PREFIX_ATTR,
					routeUri.getScheme());
			routeUri = URI.create(routeUri.getSchemeSpecificPart());
		}
		
        
		if ("lb".equalsIgnoreCase(routeUri.getScheme()) && routeUri.getHost() == null) {
			// Load balanced URIs should always have a host. If the host is null it is
			// most
			// likely because the host name was invalid (for example included an
			// underscore)
			throw new IllegalStateException("Invalid host: " + routeUri.toString());
		}

        //拼接 目标 Uri
		URI mergedUrl = UriComponentsBuilder.fromUri(uri)
				// .uri(routeUri)
				.scheme(routeUri.getScheme()).host(routeUri.getHost())
				.port(routeUri.getPort()).build(encoded).toUri();
        //存进GATEWAY_REQUEST_URL_ATTR
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, mergedUrl);
        //下一个filter
		return chain.filter(exchange);
	}

}
```

### NettyRoutingFilter

>真正发送请求去目标地址，拿回响应结果

```java
public class NettyRoutingFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(NettyRoutingFilter.class);

	private final HttpClient httpClient;

	private final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider;

	private final HttpClientProperties properties;

	// do not use this headersFilters directly, use getHeadersFilters() instead.
	private volatile List<HttpHeadersFilter> headersFilters;

	public NettyRoutingFilter(HttpClient httpClient,
			ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider,
			HttpClientProperties properties) {
		this.httpClient = httpClient;
		this.headersFiltersProvider = headersFiltersProvider;
		this.properties = properties;
	}

	public List<HttpHeadersFilter> getHeadersFilters() {
		if (headersFilters == null) {
			headersFilters = headersFiltersProvider.getIfAvailable();
		}
		return headersFilters;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取RouteToRequestUrlFilter合并好的uri
		URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

        //获取协议
		String scheme = requestUrl.getScheme();
        //只能处理http/https协议的，如果有其他filter处理了这个请求(isAlreadyRouted)则不做处理
		if (isAlreadyRouted(exchange)
				|| (!"http".equals(scheme) && !"https".equals(scheme))) {
			return chain.filter(exchange);
		}
        //设置这个请求为已处理
		setAlreadyRouted(exchange);

        
		ServerHttpRequest request = exchange.getRequest();

        //获取请求 method (GET/POST...)
		final HttpMethod method = HttpMethod.valueOf(request.getMethodValue());
        
		final String url = requestUrl.toASCIIString();

		HttpHeaders filtered = filterRequest(getHeadersFilters(), exchange);
		
        //Request Header
		final DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
		filtered.forEach(httpHeaders::set);

		boolean preserveHost = exchange
				.getAttributeOrDefault(PRESERVE_HOST_HEADER_ATTRIBUTE, false);
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        //创建请求流
		Flux<HttpClientResponse> responseFlux = getHttpClient(route, exchange)
				.headers(headers -> {
					headers.add(httpHeaders);
					// Will either be set below, or later by Netty
					headers.remove(HttpHeaders.HOST);
					if (preserveHost) {
						String host = request.getHeaders().getFirst(HttpHeaders.HOST);
						headers.add(HttpHeaders.HOST, host);
					}
				}).request(method).uri(url).send((req, nettyOutbound) -> {
					if (log.isTraceEnabled()) {
						nettyOutbound
								.withConnection(connection -> log.trace("outbound route: "
										+ connection.channel().id().asShortText()
										+ ", inbound: " + exchange.getLogPrefix()));
					}
					return nettyOutbound.send(request.getBody().map(this::getByteBuf));
				}).responseConnection((res, connection) -> {

					// Defer committing the response until all route filters have run
					// Put client response as ServerWebExchange attribute and write
					// response later NettyWriteResponseFilter
            		//保存响应信息到exchange上下文，流到NettyWriteResponseFilter的.then把响应结果发回客户端
            		//把响应结果放到CLIENT_RESPONSE_ATTR
					exchange.getAttributes().put(CLIENT_RESPONSE_ATTR, res);
            		//把connection放到CLIENT_RESPONSE_CONN_ATTR
					exchange.getAttributes().put(CLIENT_RESPONSE_CONN_ATTR, connection);

					ServerHttpResponse response = exchange.getResponse();
					// put headers and status so filters can modify the response
					HttpHeaders headers = new HttpHeaders();

					res.responseHeaders().forEach(
							entry -> headers.add(entry.getKey(), entry.getValue()));

            		//获取数据类型，并保存到ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR
					String contentTypeValue = headers.getFirst(HttpHeaders.CONTENT_TYPE);
					if (StringUtils.hasLength(contentTypeValue)) {
						exchange.getAttributes().put(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR,
								contentTypeValue);
					}

            		//把请求目标地址返回的status设置到exchange.getResponse
					setResponseStatus(res, response);

					//确保Response的HttpHeadersFilter在请求响应后执行
					HttpHeaders filteredResponseHeaders = HttpHeadersFilter.filter(
							getHeadersFilters(), headers, exchange, Type.RESPONSE);

					if (!filteredResponseHeaders
							.containsKey(HttpHeaders.TRANSFER_ENCODING)
							&& filteredResponseHeaders
									.containsKey(HttpHeaders.CONTENT_LENGTH)) {
						// It is not valid to have both the transfer-encoding header and
						// the content-length header.
						// Remove the transfer-encoding header in the response if the
						// content-length header is present.
						response.getHeaders().remove(HttpHeaders.TRANSFER_ENCODING);
					}

					exchange.getAttributes().put(CLIENT_RESPONSE_HEADER_NAMES,
							filteredResponseHeaders.keySet());

					response.getHeaders().putAll(filteredResponseHeaders);

					return Mono.just(res);
				});

		Duration responseTimeout = getResponseTimeout(route);
		if (responseTimeout != null) {
            //发起请求
			responseFlux = responseFlux
                	//设置响应超时时间
					.timeout(responseTimeout, Mono.error(new TimeoutException(
							"Response took longer than timeout: " + responseTimeout)))
                	//传播一个超时异常
					.onErrorMap(TimeoutException.class,
							th -> new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
									th.getMessage(), th));
		}

		return responseFlux.then(chain.filter(exchange));
	}
	
    //把DataBuffer转成ByteBuf，用于netty请求目标地址
	protected ByteBuf getByteBuf(DataBuffer dataBuffer) {
		if (dataBuffer instanceof NettyDataBuffer) {
			NettyDataBuffer buffer = (NettyDataBuffer) dataBuffer;
			return buffer.getNativeBuffer();
		}
		// MockServerHttpResponse creates these
		else if (dataBuffer instanceof DefaultDataBuffer) {
			DefaultDataBuffer buffer = (DefaultDataBuffer) dataBuffer;
			return Unpooled.wrappedBuffer(buffer.getNativeBuffer());
		}
		throw new IllegalArgumentException(
				"Unable to handle DataBuffer of type " + dataBuffer.getClass());
	}

    //把请求目标地址返回的status设置到exchange.getResponse
	private void setResponseStatus(HttpClientResponse clientResponse,
			ServerHttpResponse response) {
		HttpStatus status = HttpStatus.resolve(clientResponse.status().code());
		if (status != null) {
			response.setStatusCode(status);
		}
		else {
			while (response instanceof ServerHttpResponseDecorator) {
				response = ((ServerHttpResponseDecorator) response).getDelegate();
			}
			if (response instanceof AbstractServerHttpResponse) {
				((AbstractServerHttpResponse) response)
						.setStatusCodeValue(clientResponse.status().code());
			}
			else {
				// TODO: log warning here, not throw error?
				throw new IllegalStateException("Unable to set status code "
						+ clientResponse.status().code() + " on response of type "
						+ response.getClass().getName());
			}
		}
	}

	/**
	 * Creates a new HttpClient with per route timeout configuration. Sub-classes that
	 * override, should call super.getHttpClient() if they want to honor the per route
	 * timeout configuration.
	 * @param route the current route.
	 * @param exchange the current ServerWebExchange.
	 * @param chain the current GatewayFilterChain.
	 * @return
	 */
    //创建一个基于Netty的HttpClient
	protected HttpClient getHttpClient(Route route, ServerWebExchange exchange) {
		Object connectTimeoutAttr = route.getMetadata().get(CONNECT_TIMEOUT_ATTR);
		if (connectTimeoutAttr != null) {
			Integer connectTimeout = getInteger(connectTimeoutAttr);
			return this.httpClient.tcpConfiguration((tcpClient) -> tcpClient
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout));
		}
		return httpClient;
	}

    //根据route的Metadata的CONNECT_TIMEOUT_ATTR获取连接超时时间
	static Integer getInteger(Object connectTimeoutAttr) {
		Integer connectTimeout;
		if (connectTimeoutAttr instanceof Integer) {
			connectTimeout = (Integer) connectTimeoutAttr;
		}
		else {
			connectTimeout = Integer.parseInt(connectTimeoutAttr.toString());
		}
		return connectTimeout;
	}

    //根据route获取响应超时时间
	private Duration getResponseTimeout(Route route) {
		Object responseTimeoutAttr = route.getMetadata().get(RESPONSE_TIMEOUT_ATTR);
		Long responseTimeout = null;
		if (responseTimeoutAttr != null) {
			if (responseTimeoutAttr instanceof Number) {
				responseTimeout = ((Number) responseTimeoutAttr).longValue();
			}
			else {
				responseTimeout = Long.valueOf(responseTimeoutAttr.toString());
			}
		}
		return responseTimeout != null ? Duration.ofMillis(responseTimeout)
				: properties.getResponseTimeout();
	}

}
```

### ForwardRoutingFilter源码分析

>判断是不是forward，是则将请求转发给 DispatcherHandler,当作当前实例的url处理

```java
public class ForwardRoutingFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(ForwardRoutingFilter.class);

	private final ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;

	// do not use this dispatcherHandler directly, use getDispatcherHandler() instead.
	private volatile DispatcherHandler dispatcherHandler;

	public ForwardRoutingFilter(
			ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
		this.dispatcherHandlerProvider = dispatcherHandlerProvider;
	}

	private DispatcherHandler getDispatcherHandler() {
		if (dispatcherHandler == null) {
			dispatcherHandler = dispatcherHandlerProvider.getIfAvailable();
		}

		return dispatcherHandler;
	}

    //Integer.MIN_VALUE
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获得 request uri
		URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

        // 判断是否能够处理
        //加入请求url为http://127.0.0.1:8080/globalfilters，route的目标url为forward:///globalfilters
        //那么它会将请求转发给 DispatcherHandler，DispatcherHandler 匹配并转发到当前网关实例本地接口/globalfilters
		String scheme = requestUrl.getScheme();
		if (isAlreadyRouted(exchange) || !"forward".equals(scheme)) {
			return chain.filter(exchange);
		}

		// TODO: translate url?

		if (log.isTraceEnabled()) {
			log.trace("Forwarding to URI: " + requestUrl);
		}

        //将请求转发给 DispatcherHandler,当作当前实例的链接处理
		return this.getDispatcherHandler().handle(exchange);
	}

}
```



