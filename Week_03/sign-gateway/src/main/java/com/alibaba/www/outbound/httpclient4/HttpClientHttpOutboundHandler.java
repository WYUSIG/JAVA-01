package com.alibaba.www.outbound.httpclient4;

import com.alibaba.www.filter.HeaderHttpResponseFilter;
import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.filter.HttpResponseFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class HttpClientHttpOutboundHandler implements HttpOutboundHandler {

    private CloseableHttpAsyncClient httpClient;
    private ExecutorService proxyService;
    private List<String> backendUrls;

    HttpResponseFilter filter;



    @Override
    public void handle(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx, HttpRequestFilter filter) {

    }
}
