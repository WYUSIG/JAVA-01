package io.sign.www.rpc.configuration;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
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
import io.sign.www.rpc.annotation.SignRpcService;
import io.sign.www.rpc.server.HttpInboundInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private SignRpcProperties signRpcProperties;

    public static final String groupName = "SignRpc";

    public static final String TYPE_PROVIDER = "provider";
    public static final String TYPE_CONSUMER = "consumer";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void initConfiguration() throws Exception {
        String[] nameList = applicationContext.getBeanNamesForAnnotation(SignRpcService.class);
        registerServiceToNacos(nameList);
        startNettyServer();
        if (signRpcProperties.getType().equals(TYPE_CONSUMER)) {
//            Map<String,Object> beanMap = applicationContext.getBeansWithAnnotation(SignRpcService.class);
//            registerServiceToSpring();
        }
    }


    private void registerServiceToNacos(String[] nameList) {
        String nacosAddress = signRpcProperties.getNacosAddress();
        String providerIp = signRpcProperties.getProviderIp();
        Integer providerPort = signRpcProperties.getProviderPort();
        try {
            NamingService naming = NacosFactory.createNamingService(nacosAddress);
            for (String name : nameList) {
                naming.registerInstance(groupName, name, providerIp, providerPort);
            }
        } catch (NacosException e) {
            log.error("Nacos 服务注册失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }

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
            Channel ch = b.bind(signRpcProperties.getProviderPort()).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
