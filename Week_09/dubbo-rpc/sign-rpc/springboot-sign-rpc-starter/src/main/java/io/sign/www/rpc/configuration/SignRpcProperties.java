package io.sign.www.rpc.configuration;

import io.sign.www.rpc.client.SignRpcProxy;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

/**
 * RPC 配置实体类
 *
 * @author sign
 * @since 1.0
 **/
@ConfigurationProperties(prefix="sign.rpc")
@Data
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SignRpcProperties implements ApplicationContextAware {

    /**
     * @see SignRpcConfiguration#TYPE_PROVIDER
     * @see SignRpcConfiguration#TYPE_CONSUMER
     * 区别在于服务提供者会开启netty http server
     * 服务消费者不会
     * 服务提供者想要消费服务也是允许的
     */
    private String type;

    /**
     * 当 type.equals("provider") 时必须配置
     * 用于注册服务
     */
    private String rpcIp;

    /**
     * 当 type.equals("provider") 时必须配置
     * 用于注册服务和 netty 启动
     */
    private int rpcPort;

    /**
     * nacos 配置
     */
    private NacosProperties nacos;


    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() throws Exception{
        if (rpcIp == null) {
            InetAddress ip4 = Inet4Address.getLocalHost();
            rpcIp = ip4.getHostAddress();
        }
        SignRpcProxy.setNacosAddress(nacos);
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
