package com.alibaba.www.inbound;

import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.outbound.httpclient4.HttpClientHttpOutboundHandler;
import com.alibaba.www.outbound.netty4.Netty4HttpClient;
import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import com.alibaba.www.router.HttpEndpointRouter;
import com.alibaba.www.util.ProxyFactoryUtil;
import com.alibaba.www.util.SpringUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;


public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);


    private GatewayProperties gatewayProperties;

    private HttpClientHttpOutboundHandler httpClientHttpOutboundHandler;

    private Netty4HttpClient netty4HttpClient;


    public HttpInboundHandler() {
        //依赖查找
        this.gatewayProperties = (GatewayProperties) SpringUtil.getBean(GatewayProperties.class);
        this.httpClientHttpOutboundHandler = (HttpClientHttpOutboundHandler) SpringUtil.getBean(HttpClientHttpOutboundHandler.class);
        this.netty4HttpClient = (Netty4HttpClient) SpringUtil.getBean(Netty4HttpClient.class);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            String uri = formatUri(fullHttpRequest.uri());
            System.out.println("\n收到请求：" + uri);
            //动态代理 路由
            ProxyFactory routeProxyFactory = ProxyFactoryUtil.getEndpointRouterProxyFactory(gatewayProperties, uri);
            //根据正则匹配及负载均衡策略命中一个路由
            RouteDefinition routeDefinition = ((HttpEndpointRouter) routeProxyFactory.getProxy()).route();
            if (routeDefinition != null) {
                handleWithRoute(ctx,fullHttpRequest,routeDefinition,uri);
            } else {
                //匹配不上，加上/再次尝试
                uri = formatUriForNoMatcher(uri);
                ProxyFactory noMatchaerRouteProxyFactory = ProxyFactoryUtil.getEndpointRouterProxyFactory(gatewayProperties, uri);
                routeDefinition = ((HttpEndpointRouter) noMatchaerRouteProxyFactory.getProxy()).route();
                if(routeDefinition != null){
                    handleWithRoute(ctx,fullHttpRequest,routeDefinition,uri);
                }else {
                    System.out.println("匹配失败，没有命中路由为，uri:" + uri);
                    handlerNoPathMatcherRoute(ctx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleWithRoute(ChannelHandlerContext ctx,FullHttpRequest fullHttpRequest,RouteDefinition routeDefinition,String uri) throws Exception{
        System.out.println("匹配成功，" + uri + "命中路由为：");
        System.out.println("路由id\t\t链接\t\t匹配规则\t\trequest拦截器\t\tresponse拦截器");
        System.out.println(routeDefinition.getId() + "\t\t" + routeDefinition.getUri() + "\t\t" + routeDefinition.getPredicates() + "\t\t" + routeDefinition.getRequestFilter().getSig() + "\t\t" + routeDefinition.getResponseFilter().getSig());
        ProxyFactory requestProxyFactory = ProxyFactoryUtil.getRequestFilterProxyFactory(routeDefinition);
        //aop动态代理requestFilter
        HttpRequestFilter requestFilter = (HttpRequestFilter) requestProxyFactory.getProxy();
        requestFilter.filter(fullHttpRequest, ctx);
        //根据配置元信息handler动态代理httpclient等handler
        String handler = gatewayProperties.getHandler();
        if (GatewayProperties.HTTP_CLIENT_HANDLER.equals(handler)) {
            httpClientHttpOutboundHandler.handle(routeDefinition, fullHttpRequest, ctx);
        } else if (GatewayProperties.NETTY_HANDLER.equals(handler)) {
            netty4HttpClient.handle(routeDefinition, fullHttpRequest, ctx);
        } else {
            handlerNoOutbound(ctx);
        }
    }

    private String formatUri(String uri) {
        String[] s = uri.split("\\?");
        String parameters = "";
        if (s.length > 0) {
            uri = s[0].endsWith("/") ? s[0].substring(0, s[0].length() - 1) : s[0];
        }
        if (s.length > 1) {
            parameters = "?" + s[1];
            uri += parameters;
        }
        return uri;
    }

    private String formatUriForNoMatcher(String uri){
        String[] s = uri.split("\\?");
        String parameters = "";
        if (s.length > 0) {
            uri = s[0].endsWith("/") ? s[0] : s[0] + "/";
        }
        if (s.length > 1) {
            parameters = "?" + s[1];
            uri += parameters;
        }
        return uri;
    }

    private void handlerNoPathMatcherRoute(ChannelHandlerContext ctx) throws Exception {
        String msg = "没有匹配的路由";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void handlerNoOutbound(ChannelHandlerContext ctx) throws Exception {
        String msg = "handler配置元信息不支持";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
}

