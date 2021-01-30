package com.alibaba.www.outbound;

import com.alibaba.www.pojo.RouteDefinition;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpOutboundHandler {

    void handle(final RouteDefinition routeDefinition, final FullHttpRequest fullHttpRequest, final ChannelHandlerContext ctx) throws Exception;
}
