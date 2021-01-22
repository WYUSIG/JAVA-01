package com.google.www.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName HttpServer04
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/15 0015
 * @Version V1.0
 **/
public class HttpServer04 {

    private static Logger logger = LoggerFactory.getLogger(HttpServer04.class);

    public static final String URL = "http://localhost:8804/test";

    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(3);
        EventLoopGroup workerGroup = new NioEventLoopGroup(100);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_REUSEADDR,true)
                    .option(ChannelOption.SO_RCVBUF,32*1024)
                    .option(ChannelOption.SO_SNDBUF,32*1024)
                    .option(EpollChannelOption.SO_REUSEPORT,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());// http 编解码
                            ch.pipeline().addLast("httpAggregator",new HttpObjectAggregator(512*1024)); // http 消息聚合器
                            ch.pipeline().addLast(new HttpHandler());
                        }
                    });
            Channel channel = b.bind(8804).sync().channel();
            logger.trace("开启netty http服务器");
            channel.closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
