package io.sign.www.rpc.configuration;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.sign.www.rpc.annotation.SignRpcReference;
import io.sign.www.rpc.annotation.SignRpcService;
import io.sign.www.rpc.client.SignRpcProxy;
import io.sign.www.rpc.server.HttpInboundHandler;
import io.sign.www.rpc.server.HttpInboundInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Configuration
 *
 * @author sign
 * @since 1.0
 **/
@Configuration
@Slf4j
public class SignRpcConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private NamingService naming;

    @Autowired
    private SignRpcProperties signRpcProperties;

    public static final String TYPE_PROVIDER = "provider";

    public static final String TYPE_CONSUMER = "consumer";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        HttpInboundHandler.setApplicationContext(applicationContext);
    }

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                Class<?> objClz;
                if (AopUtils.isAopProxy(bean)) {
                    objClz = AopUtils.getTargetClass(bean);
                } else {
                    objClz = bean.getClass();
                }
                try {
                    if (objClz.isAnnotationPresent(SignRpcService.class)) {
                        //注册服务到 nacos
                        registerService(beanName);
                    }
                    for (Field field : objClz.getDeclaredFields()) {
                        //判断该字段是否有 SignRpcReference 注解
                        if (field.isAnnotationPresent(SignRpcReference.class)) {
                            Object proxyReference = SignRpcProxy.create(field.getType());
                            field.setAccessible(true);
                            field.set(bean, proxyReference);
                        }
                    }
                } catch (Exception e) {
                    throw new BeanCreationException(beanName, e);
                }
                return bean;
            }
        };
    }

    @PostConstruct
    private void initConfiguration() {
        if (signRpcProperties.getType().equals(TYPE_PROVIDER)) {
            new Thread(()->{
                try {
                    startNettyServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    private void registerService(String serviceName) throws Exception{
        if (naming == null) {
            synchronized (SignRpcConfiguration.class) {
                if (naming == null) {
                    naming = NacosFactory.createNamingService(signRpcProperties.getNacos().getAddress());
                }
            }
        }
        naming.registerInstance(serviceName, signRpcProperties.getNacos().getGroup(), signRpcProperties.getRpcIp(), signRpcProperties.getRpcPort());
    }

    /**
     * 启动 netty http 服务器
     * @throws Exception
     */
    private void startNettyServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .option(EpollChannelOption.SO_REUSEPORT, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new HttpInboundInitializer());
            Channel ch = b.bind(signRpcProperties.getRpcPort()).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}