package com.alibaba.www.outbound.httpclient4;

import com.alibaba.www.filter.HttpResponseFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.pojo.RouteDefinition;
import com.alibaba.www.util.ProxyFactoryUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.springframework.aop.framework.ProxyFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpClientHttpOutboundHandler implements HttpOutboundHandler {

    private CloseableHttpAsyncClient httpClient;

    public HttpClientHttpOutboundHandler() {
        int cores = Runtime.getRuntime().availableProcessors();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(1000)
                .setSoTimeout(1000)
                .setIoThreadCount(cores)
                .setRcvBufSize(32 * 1024)
                .build();
        httpClient = HttpAsyncClients.custom().setMaxConnTotal(40)
                .setMaxConnPerRoute(8)
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setKeepAliveStrategy((response, context) -> 6000)
                .build();
        httpClient.start();
    }


    @Override
    public void handle(final RouteDefinition routeDefinition,final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) throws Exception {
        HttpMethod httpMethod = fullRequest.method();
        if(HttpMethod.GET.equals(httpMethod)){
            fetchGet(routeDefinition,fullRequest, ctx);
        }else {
            handlerPostMethod(ctx);
        }
    }

    private void fetchGet(final RouteDefinition routeDefinition,final FullHttpRequest inbound, final ChannelHandlerContext ctx) {
        final HttpGet httpGet = new HttpGet(routeDefinition.getUri());
        //把requestFilter处理过的header复制到httpGet
        copyHeader(inbound, httpGet);
        //HttpClient自带线程池，所以我就不用线程池了
        httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(final HttpResponse endpointResponse) {
                try {
                    handleResponse(routeDefinition,inbound, ctx, endpointResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(final Exception ex) {
                httpGet.abort();
                ex.printStackTrace();
            }

            @Override
            public void cancelled() {
                httpGet.abort();
            }
        });
    }

    private void handleResponse(final RouteDefinition routeDefinition,final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final HttpResponse endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            if (endpointResponse.getFirstHeader("Content-Length") == null) {
                handlerNoContentLength(ctx);
            } else {
                String length = endpointResponse.getFirstHeader("Content-Length").getValue();
                response.headers().setInt("Content-Length", Integer.parseInt(length));
                ProxyFactory proxyFactory = ProxyFactoryUtil.getResponseFilterProxyFactory(routeDefinition);
                HttpResponseFilter responseFilter = (HttpResponseFilter) proxyFactory.getProxy();
                responseFilter.filter(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
            ctx.close();
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private void copyHeader(FullHttpRequest request, HttpGet httpGet) {
        List<Map.Entry<String, String>> headEntryList = request.headers().entries();
        for (Map.Entry<String, String> entry : headEntryList) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }
    }

    private void handlerNoContentLength(ChannelHandlerContext ctx) throws Exception {
        String msg = "目标路由网址报文未携带Content-Length";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void handlerPostMethod(ChannelHandlerContext ctx) throws Exception{
        String msg = "POST请求在httpclient outbound上暂不支持";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", response.content().readableBytes());
        ctx.writeAndFlush(response).sync();
        ctx.close();
    }
}
