package com.alibaba.www.inbound;

import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import com.alibaba.www.proxy.ProxyFactoryUtil;
import com.alibaba.www.router.HttpEndpointRouter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);

    @Autowired
    private GatewayProperties gatewayProperties;


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
            String uri = fullHttpRequest.uri();
            //动态代理 路由
            ProxyFactory routeProxyFactory = ProxyFactoryUtil.getEndpointRouterProxyFactory(gatewayProperties,uri);
            //根据正则匹配及负载均衡策略命中一个路由
            RouteDefinition routeDefinition = ((HttpEndpointRouter)routeProxyFactory.getProxy()).route();
            ProxyFactory requestProxyFactory = ProxyFactoryUtil.getRequestFilterProxyFactory(routeDefinition);
            //aop动态代理requestFilter
            HttpRequestFilter requestFilter = (HttpRequestFilter)requestProxyFactory.getProxy();
            requestFilter.filter(fullHttpRequest,ctx);
            //根据配置元信息handler动态代理httpclient等handler
            ProxyFactory outboundHandlerProxyFactory = ProxyFactoryUtil.getOutboundHandlerProxyFactory(gatewayProperties.getHandler(),routeDefinition);
            HttpOutboundHandler outboundHandler = (HttpOutboundHandler)outboundHandlerProxyFactory.getProxy();
            outboundHandler.handle(fullHttpRequest,ctx);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

