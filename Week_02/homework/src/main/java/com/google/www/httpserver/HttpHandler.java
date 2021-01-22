package com.google.www.httpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

/**
 * @ClassName HttpHandler
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/15 0015
 * @Version V1.0
 **/
public class HttpHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
            String uri = fullHttpRequest.uri();
            if(uri.contains("/test")){
                handlerTest(fullHttpRequest,ctx);
            }else {
                handlerDefault(fullHttpRequest,ctx);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    private void handlerTest(FullHttpRequest fullHttpRequest, ChannelHandlerContext context){
        FullHttpResponse response = null;
        try {
            String value = "hello,netty";
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
            response.headers().set("Content-Type","application/json");
            response.headers().setInt("Content-Length",response.content().readableBytes());
        }catch (Exception e){
            logger.error("处理测试接口出错");
            e.printStackTrace();
        }finally {
            if(fullHttpRequest != null){
                if(!HttpUtil.isKeepAlive(fullHttpRequest)){
                    context.write(response).addListener(ChannelFutureListener.CLOSE);
                }else {
                    response.headers().set(CONNECTION,KEEP_ALIVE);
                    context.write(response);
                }
            }
        }
    }

    private void handlerDefault(FullHttpRequest fullHttpRequest, ChannelHandlerContext context){
        FullHttpResponse response = null;
        try {
            String value = "请访问http://localhost:8804/test";
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
            response.headers().set("Content-Type","application/json");
            response.headers().setInt("Content-Length",response.content().readableBytes());
        }catch (IOException e){
            logger.error("处理默认接口出错");
            e.printStackTrace();
        }finally {
            if(fullHttpRequest != null){
                if(!HttpUtil.isKeepAlive(fullHttpRequest)){
                    context.write(response).addListener(ChannelFutureListener.CLOSE);
                }else {
                    response.headers().set(CONNECTION,KEEP_ALIVE);
                    context.write(response);
                }
            }
        }
    }
}
