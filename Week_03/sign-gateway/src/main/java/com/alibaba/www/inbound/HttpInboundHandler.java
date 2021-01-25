package com.alibaba.www.inbound;

import com.alibaba.www.filter.HeaderHttpRequestFilter;
import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.outbound.httpclient4.HttpClientHttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final List<String> proxyServer;
    private HttpOutboundHandler handler;
    private HttpRequestFilter filter = new HeaderHttpRequestFilter();

    public HttpInboundHandler(List<String> proxyServer){
        this.proxyServer = proxyServer;
        this.handler = new HttpClientHttpOutboundHandler(this.proxyServer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
            handler.handle(fullHttpRequest,ctx,filter);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

