package com.alibaba.www.outbound;

import com.alibaba.www.filter.HttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpOutboundHandler {

    void handle(final FullHttpRequest fullHttpRequest, final ChannelHandlerContext ctx, HttpRequestFilter filter);
}
