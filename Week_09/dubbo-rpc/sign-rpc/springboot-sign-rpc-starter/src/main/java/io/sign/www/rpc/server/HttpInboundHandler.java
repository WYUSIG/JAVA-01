package io.sign.www.rpc.server;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.sign.www.rpc.api.SignRpcRequest;
import io.sign.www.rpc.api.SignRpcResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static ApplicationContext applicationContext;

    private SignRpcInvoker invoker;

    public HttpInboundHandler() {
        this.invoker = applicationContext.getBean(SignRpcInvoker.class);
    }

    public static void setApplicationContext(ApplicationContext ac) {
        applicationContext = ac;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            ByteBuf requestBuf = fullHttpRequest.content();
            String requestJsonStr = requestBuf.toString(CharsetUtil.UTF_8);
            SignRpcRequest signRpcRequest = JSON.parseObject(requestJsonStr, SignRpcRequest.class);

            SignRpcResponse signRpcResponse = invoker.invoke(signRpcRequest);
            String responseJsonStr = JSON.toJSONString(signRpcResponse);

            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(responseJsonStr.getBytes("UTF-8")));
            fullHttpResponse.headers().set("Content-Type", "application/json");
            fullHttpResponse.headers().setInt("Content-Length", fullHttpResponse.content().readableBytes());
            ctx.writeAndFlush(fullHttpResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

