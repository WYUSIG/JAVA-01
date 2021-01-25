package com.alibaba.www.outbound.httpclient4;

import com.alibaba.www.filter.HeaderHttpResponseFilter;
import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.filter.HttpResponseFilter;
import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.router.HttpEndpointRouter;
import com.alibaba.www.router.RandomHttpEndpointRouter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HttpClientHttpOutboundHandler implements HttpOutboundHandler {

    private CloseableHttpAsyncClient httpClient;
    private List<String> backendUrls;

    HttpResponseFilter filter = new HeaderHttpResponseFilter();
    HttpEndpointRouter router = new RandomHttpEndpointRouter();

    public HttpClientHttpOutboundHandler(List<String> backends){
        this.backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());
        int cores = Runtime.getRuntime().availableProcessors();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(1000)
                .setSoTimeout(1000)
                .setIoThreadCount(cores)
                .setRcvBufSize(32*1024)
                .build();
        httpClient = HttpAsyncClients.custom().setMaxConnTotal(40)
                .setMaxConnPerRoute(8)
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setKeepAliveStrategy((response,context)->6000)
                .build();
        httpClient.start();
    }


    public HttpClientHttpOutboundHandler(HttpResponseFilter filter, HttpEndpointRouter router, List<String> backends) {
        this.filter = filter;
        this.router = router;
        this.backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());
        int cores = Runtime.getRuntime().availableProcessors();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(1000)
                .setSoTimeout(1000)
                .setIoThreadCount(cores)
                .setRcvBufSize(32*1024)
                .build();
        httpClient = HttpAsyncClients.custom().setMaxConnTotal(40)
                .setMaxConnPerRoute(8)
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setKeepAliveStrategy((response,context)->6000)
                .build();
        httpClient.start();
    }

    @Override
    public void handle(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx, HttpRequestFilter filter) {

    }

    private String formatUrl(String backend){
        return backend.endsWith("/")?backend.substring(0,backend.length()-1):backend;
    }


}
