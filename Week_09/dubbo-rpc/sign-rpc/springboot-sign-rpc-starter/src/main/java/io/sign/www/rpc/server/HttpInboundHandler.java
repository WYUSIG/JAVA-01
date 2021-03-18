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
import io.sign.www.rpc.configuration.SignRpcProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static io.sign.www.rpc.configuration.SignRpcConfiguration.TYPE_PROVIDER;

@Component
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static ApplicationContext applicationContext;

    private SignRpcInvoker invoker;

    private SignRpcProperties signRpcProperties;

    public HttpInboundHandler() {
        this.invoker = (SignRpcInvoker) applicationContext.getBean(SignRpcInvoker.class);
        this.signRpcProperties = (SignRpcProperties) applicationContext.getBean(SignRpcProperties.class);
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
            if (signRpcProperties.getType().equals(TYPE_PROVIDER)) {
                handleByProvider(ctx, requestJsonStr);
            } else {
                handleByConsumer(ctx, requestJsonStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleByProvider(ChannelHandlerContext ctx, String requestJsonStr) throws Exception {
        SignRpcRequest signRpcRequest = JSON.parseObject(requestJsonStr, SignRpcRequest.class);

        SignRpcResponse signRpcResponse = invoker.invoke(signRpcRequest);
        String responseJsonStr = JSON.toJSONString(signRpcResponse);

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseJsonStr.getBytes("UTF-8")));
        fullHttpResponse.headers().set("Content-Type", "application/json");
        fullHttpResponse.headers().setInt("Content-Length", fullHttpResponse.content().readableBytes());
        ctx.writeAndFlush(fullHttpResponse);
    }

    private void handleByConsumer(ChannelHandlerContext ctx, String responseJsonStr) {
        //TODO
    }
}

