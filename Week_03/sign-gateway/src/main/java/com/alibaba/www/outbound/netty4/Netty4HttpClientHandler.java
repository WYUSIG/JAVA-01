package com.alibaba.www.outbound.netty4;

import com.alibaba.www.filter.HttpResponseFilter;
import com.alibaba.www.pojo.RouteDefinition;
import com.alibaba.www.util.ProxyFactoryUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.springframework.aop.framework.ProxyFactory;

public class Netty4HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private ChannelHandlerContext context;

    private RouteDefinition routeDefinition;

    public Netty4HttpClientHandler(RouteDefinition routeDefinition, ChannelHandlerContext context) {
        this.routeDefinition = routeDefinition;
        this.context = context;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        ProxyFactory proxyFactory = ProxyFactoryUtil.getResponseFilterProxyFactory(routeDefinition);
        HttpResponseFilter responseFilter = (HttpResponseFilter) proxyFactory.getProxy();
        responseFilter.filter(response);
        context.writeAndFlush(response).sync().addListener(ChannelFutureListener.CLOSE);;
        response.release();
    }

}
