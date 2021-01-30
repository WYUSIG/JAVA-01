package com.alibaba.www.outbound.netty4;

import com.alibaba.www.outbound.HttpOutboundHandler;
import com.alibaba.www.pojo.RouteDefinition;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Data
public class Netty4HttpClient implements HttpOutboundHandler {

    private ExecutorService proxyService;

    public Netty4HttpClient() {
        int cores = Runtime.getRuntime().availableProcessors();
        long keepAliveTime = 1000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        proxyService = new ThreadPoolExecutor(cores, cores,
                keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory("proxyService"), handler);
    }

    @Override
    public void handle(final RouteDefinition routeDefinition, final FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) throws Exception {
        proxyService.submit(() -> {
            try {
                createNettyClient(routeDefinition, fullHttpRequest, ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createNettyClient(final RouteDefinition routeDefinition, final FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        String uri = routeDefinition.getUri();
        URI javaUri = new URI(uri);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(javaUri.getHost(), javaUri.getPort()))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new HttpClientCodec());
                        socketChannel.pipeline().addLast(new HttpContentDecompressor());//这里要添加解压，不然打印时会乱码
                        socketChannel.pipeline().addLast("httpAggregator", new HttpObjectAggregator(512 * 1024));
                        socketChannel.pipeline().addLast(new Netty4HttpClientHandler(routeDefinition, ctx));

//                        socketChannel.pipeline().addLast(new HttpResponseDecoder());
//                     客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
//                        socketChannel.pipeline().addLast(new HttpRequestEncoder());
//                        socketChannel.pipeline().addLast(new Netty4HttpClientHandler(routeDefinition,ctx));
                    }
                });
        Channel channel = null;
        try {
            channel = bootstrap.connect().sync().channel();
            fullHttpRequest.setUri(uri);
            channel.writeAndFlush(fullHttpRequest).sync();
        } finally {
            if (channel != null) {
                channel.closeFuture().sync();
            }
            group.shutdownGracefully();
        }
    }

    private FullHttpRequest copyHeader(FullHttpRequest fullHttpRequest, URI uri) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        List<Map.Entry<String, String>> headEntryList = fullHttpRequest.headers().entries();
        for (Map.Entry<String, String> entry : headEntryList) {
            request.headers().set(entry.getKey(), entry.getValue());
        }
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        return request;
    }

}
