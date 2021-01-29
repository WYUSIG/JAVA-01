package com.alibaba.www.inbound;

import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import com.alibaba.www.util.ProxyFactoryUtil;
import com.alibaba.www.router.HttpEndpointRouter;
import com.alibaba.www.util.SpringUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;


public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);


    private GatewayProperties gatewayProperties;


    public HttpInboundHandler(){
        this.gatewayProperties = (GatewayProperties)SpringUtil.getBean(GatewayProperties.class);
    }


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
            if(routeDefinition != null){
                System.out.println("匹配信息："+uri+"->"+routeDefinition.getUri());
                ProxyFactory requestProxyFactory = ProxyFactoryUtil.getRequestFilterProxyFactory(routeDefinition);
                //aop动态代理requestFilter
                HttpRequestFilter requestFilter = (HttpRequestFilter)requestProxyFactory.getProxy();
                requestFilter.filter(fullHttpRequest,ctx);
                //根据配置元信息handler动态代理httpclient等handler
                ProxyFactory outboundHandlerProxyFactory = ProxyFactoryUtil.getOutboundHandlerProxyFactory(gatewayProperties.getHandler(),routeDefinition);
                HttpOutboundHandler outboundHandler = (HttpOutboundHandler)outboundHandlerProxyFactory.getProxy();
                outboundHandler.handle(fullHttpRequest,ctx);
            }else {
                System.out.println("没有匹配的路由："+uri);
                handlerNoPathMatcherRoute(ctx);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handlerNoPathMatcherRoute(ChannelHandlerContext ctx) throws Exception{
        String msg = "没有匹配的路由";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
        response.headers().set("Content-Type","application/json");
        response.headers().setInt("Content-Length",response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
}

