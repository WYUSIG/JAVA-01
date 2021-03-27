## Spring-Cloud-Gateway源码系列学习

版本 v2.2.6.RELEASE



### 请求路由匹配及其他处理流程

>先上结论，怕下面源码说多了，看了抓不住主线

DispatcherHandler初始化：**DispatcherHandler**#initStrategies 

DispatcherHandler请求处理：**DispatcherHandler**#handle

获取匹配路由(仅针对RoutePredicateHandlerMapping)：**AbstractHandlerMapping**#getHandler

获取匹配路由：**RoutePredicateHandlerMapping**#getHandlerInterna

获取匹配路由：**RoutePredicateHandlerMapping**#lookupRoute

**DispatcherHandler**#invokeHandler	

生成Filter处理链：**SimpleHandlerAdapter**#handle

生成Filter处理链：**FilteringWebHandler**#handle



### DispatcherHandler源码解析

>主要是将请求经过HandlerMapping、HandlerAdapter、HandlerResultHandler处理

```java
public class DispatcherHandler implements WebHandler, ApplicationContextAware{
    
    //只需要获得 applicationContext 就去执行 initStrategies 获得需要的Bean
    public DispatcherHandler(ApplicationContext applicationContext) {
		initStrategies(applicationContext);
	}
    
    //ApplicationContextAware接口实现方法，只需要获得 applicationContext 就去执行 initStrategies 获得需要的Bean
    @Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		initStrategies(applicationContext);
	}
    
    //通过 applicationContext 获取 HandlerMapping、HandlerAdapter、HandlerResultHandler
	protected void initStrategies(ApplicationContext context) {
        //集合类型依赖查找，返回给定类型或子类型的所有bean，后面两个boolean参数分别是 是否可以非单例、是否提前初始化
		Map<String, HandlerMapping> mappingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerMapping.class, true, false);

        //排序
		ArrayList<HandlerMapping> mappings = new ArrayList<>(mappingBeans.values());
		AnnotationAwareOrderComparator.sort(mappings);
        //转成只读的集合，然后赋值
		this.handlerMappings = Collections.unmodifiableList(mappings);

        //类似
		Map<String, HandlerAdapter> adapterBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerAdapter.class, true, false);

		this.handlerAdapters = new ArrayList<>(adapterBeans.values());
		AnnotationAwareOrderComparator.sort(this.handlerAdapters);

        //类似
		Map<String, HandlerResultHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerResultHandler.class, true, false);

		this.resultHandlers = new ArrayList<>(beans.values());
		AnnotationAwareOrderComparator.sort(this.resultHandlers);
	}
    
    //处理请求，Reactor的代码意思我猜的，哈哈哈，debug也看不到里面发生了啥
    @Override
	public Mono<Void> handle(ServerWebExchange exchange) {
		if (this.handlerMappings == null) {
			return createNotFoundError();
		}
		return Flux.fromIterable(this.handlerMappings)  //把List的handlerMappings转成Flux流
				.concatMap(mapping -> mapping.getHandler(exchange)) //依次执行mapping.getHandler，然后将Mono转成Flux
				.next() //取第一个元素
				.switchIfEmpty(createNotFoundError()) //如果为空则流一个异常
				.flatMap(handler -> invokeHandler(exchange, handler)) //HandlerAdapter依次处理
				.flatMap(result -> handleResult(exchange, result)); //HandlerResultHandler依次处理
	}
    
    private Mono<HandlerResult> invokeHandler(ServerWebExchange exchange, Object handler) {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter handlerAdapter : this.handlerAdapters) {
				if (handlerAdapter.supports(handler)) {
					return handlerAdapter.handle(exchange, handler);
				}
			}
		}
		return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
	}

	private Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		return getResultHandler(result).handleResult(exchange, result)
				.checkpoint("Handler " + result.getHandler() + " [DispatcherHandler]")
				.onErrorResume(ex ->
						result.applyExceptionHandler(ex).flatMap(exResult -> {
							String text = "Exception handler " + exResult.getHandler() +
									", error=\"" + ex.getMessage() + "\" [DispatcherHandler]";
							return getResultHandler(exResult).handleResult(exchange, exResult).checkpoint(text);
						}));
	}
}
```

#### 调试信息

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/1616577680(1).png)

**根据调试发现：**

**exchange**是DefaultServerWebExchange类型的，可以看一下具体源码

List<HandlerMapping>  **handlerMappings**有：

- WebFluxEndpointHandlerMapping
- ControllerEndpointHandlerMapping
- RouterFunctionMapping
- RequestMappingHandlerMapping
- RoutePredicateHandlerMapping
- SimpleUrlHandlerMapping

List<HandlerAdapter>  **handlerAdapters**有：

- RequestMappingHandlerAdapter
- HandlerFunctionAdapter
- SimpleHandlerAdapter

List<HandlerResultHandler>  **resultHandlers**有：

- ResponseEntityResultHandler
- ServerResponseResultHandler
- ResponseBodyResultHandler
- ViewResolutionResultHandler

### AbstractHandlerMapping源码解析

>AbstractHandlerMapping主要是调用子类的getHandlerInternal来处理逻辑

```java
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport implements HandlerMapping, Ordered, BeanNameAware {
		
	@Override
	public Mono<Object> getHandler(ServerWebExchange exchange) {
        //调用子类的getHandlerInternal方法
		return getHandlerInternal(exchange).map(handler -> {
			if (logger.isDebugEnabled()) {
				logger.debug(exchange.getLogPrefix() + "Mapped to " + handler);
			}
			ServerHttpRequest request = exchange.getRequest();
            //处理跨域
			if (hasCorsConfigurationSource(handler) || CorsUtils.isPreFlightRequest(request)) {
				CorsConfiguration config = (this.corsConfigurationSource != null ? this.corsConfigurationSource.getCorsConfiguration(exchange) : null);
				CorsConfiguration handlerConfig = getCorsConfiguration(handler, exchange);
				config = (config != null ? config.combine(handlerConfig) : handlerConfig);
				if (!this.corsProcessor.process(config, exchange) || CorsUtils.isPreFlightRequest(request)) {
					return REQUEST_HANDLED_HANDLER;
				}
			}
			return handler;
		});
	}
	
	//需要子类实现
	protected abstract Mono<?> getHandlerInternal(ServerWebExchange exchange);
}
```



### RoutePredicateHandlerMapping源码解析

>RoutePredicateHandlerMapping主要是负责根据谓词匹配路由，并把匹配成功的路由放进exchange上下文中，方便下游获取，而且把FilteringWebHandler带到了下游

```java
public class RoutePredicateHandlerMapping extends AbstractHandlerMapping {
	
	public RoutePredicateHandlerMapping(FilteringWebHandler webHandler,
			RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties,
			Environment environment) {
		//根据GatewayAutoConfiguration代码可以知道，注入的是@Bean->new FilteringWebHandler(globalFilters)
		this.webHandler = webHandler;
		
		//根据GatewayAutoConfiguration代码可以知道，注入的是CachingRouteLocator
		this.routeLocator = routeLocator;

		this.managementPort = getPortProperty(environment, "management.server.");
		this.managementPortType = getManagementPortType(environment);
		setOrder(1);
		setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
	}
	
	@Override
	protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
		// don't handle requests on management port if set and different than server port
		if (this.managementPortType == DIFFERENT && this.managementPort != null
				&& exchange.getRequest().getURI().getPort() == this.managementPort) {
			return Mono.empty();
		}
        //将“RoutePredicateHandlerMapping”放到exchange#attributes，key是“org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayHandlerMapper”
		exchange.getAttributes().put(GATEWAY_HANDLER_MAPPER_ATTR, getSimpleName());

        //调用lookupRoute进行匹配Route
		return lookupRoute(exchange)
				// .log("route-predicate-handler-mapping", Level.FINER) //name this
				.flatMap((Function<Route, Mono<?>>) r -> {
                    //移除经过谓词判断的rouId
					exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Mapping [" + getExchangeDesc(exchange) + "] to " + r);
					}

                    //把匹配成功的route放进exchange#attributes，一种厉害的上下文传递参数方式
					exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r);
					return Mono.just(webHandler);
				}).switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
					exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
					if (logger.isTraceEnabled()) {
						logger.trace("No RouteDefinition found for ["
								+ getExchangeDesc(exchange) + "]");
					}
				})));
	}
	
    //路由谓词判断，进行匹配
	protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
		return this.routeLocator.getRoutes()  //通过CachingRouteLocator获取Flux<Route>
				// individually filter routes so that filterWhen error delaying is not a
				// problem
				.concatMap(route -> Mono.just(route).filterWhen(r -> {
					// add the current route we are testing
					exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());//把经过谓词判断的rouId放到exchange#attributes
					//判断，return true会留下，return false会丢弃
                    return r.getPredicate().apply(exchange);
				})
						// instead of immediately stopping main flux due to error, log and
						// swallow it
						.doOnError(e -> logger.error(
								"Error applying predicate for route: " + route.getId(),
								e))
						.onErrorResume(e -> Mono.empty()))
				// .defaultIfEmpty() put a static Route not found
				// or .switchIfEmpty()
				// .switchIfEmpty(Mono.<Route>empty().log("noroute"))
				.next()
				// TODO: error handling
				.map(route -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Route matched: " + route.getId());
					}
                    //校验Route
					validateRoute(route, exchange);
					return route;
				});

		/*
		 * TODO: trace logging if (logger.isTraceEnabled()) {
		 * logger.trace("RouteDefinition did not match: " + routeDefinition.getId()); }
		 */
	}
}
```



### FilteringWebHandler源码解析

>FilteringWebHandler经过RoutePredicateHandlerMapping return后，经DispatcherHandler#HandlerAdapter invokeHandler后执行

```java
public class FilteringWebHandler implements WebHandler {
    
    private final List<GatewayFilter> globalFilters;
    
    //使用GatewayFilterAdapter将GlobalFilter转成GatewayFilter
    private static List<GatewayFilter> loadFilters(List<GlobalFilter> filters) {
		return filters.stream().map(filter -> {
			GatewayFilterAdapter gatewayFilter = new GatewayFilterAdapter(filter);
			if (filter instanceof Ordered) {
				int order = ((Ordered) filter).getOrder();
				return new OrderedGatewayFilter(gatewayFilter, order);
			}
			return gatewayFilter;
		}).collect(Collectors.toList());
	}
    
	@Override
	public Mono<Void> handle(ServerWebExchange exchange) {
        //从exchange上下文中取出匹配成功的route
		Route route = exchange.getRequiredAttribute(GATEWAY_ROUTE_ATTR);
        //获取所有GatewayFilter
		List<GatewayFilter> gatewayFilters = route.getFilters();

		List<GatewayFilter> combined = new ArrayList<>(this.globalFilters);
        //合并两个Filter列表
		combined.addAll(gatewayFilters);
		// TODO: needed or cached?
        //排序
		AnnotationAwareOrderComparator.sort(combined);

		if (logger.isDebugEnabled()) {
			logger.debug("Sorted gatewayFilterFactories: " + combined);
		}
	
        //生成Filter处理链头节点
		return new DefaultGatewayFilterChain(combined).filter(exchange);
	}
	
    //默认的GatewayFilterChain实现
	private static class DefaultGatewayFilterChain implements GatewayFilterChain {
        
        //当前Filter处理链 执行到了哪（下标）
        private final int index;

        //整条处理链的Filter
		private final List<GatewayFilter> filters;

        //默认下标从0开始
		DefaultGatewayFilterChain(List<GatewayFilter> filters) {
			this.filters = filters;
			this.index = 0;
		}

        //指定执行下标
		private DefaultGatewayFilterChain(DefaultGatewayFilterChain parent, int index) {
			this.filters = parent.getFilters();
			this.index = index;
		}
        
		@Override
		public Mono<Void> filter(ServerWebExchange exchange) {
			return Mono.defer(() -> {
				if (this.index < filters.size()) {
                    //根据执行下标拿出GatewayFilter
					GatewayFilter filter = filters.get(this.index);
                    //生成Filter处理链下一个节点
					DefaultGatewayFilterChain chain = new DefaultGatewayFilterChain(this,
							this.index + 1);
                    //执行Filter的逻辑
					return filter.filter(exchange, chain);
				}
				else {
					return Mono.empty(); // complete
				}
			});
		}
	}
}
```

