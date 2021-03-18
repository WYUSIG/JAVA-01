package io.sign.www.rpc.configuration;

import io.sign.www.rpc.client.SignRpcProxy;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

    private String nacosAddress;

    private String type;

    private String providerIp;

    private int providerPort;

    private ConsumerProperties consumer;

    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() throws Exception{
        if (providerIp == null) {
            InetAddress ip4 = Inet4Address.getLocalHost();
            providerIp = ip4.getHostAddress();
        }
        SignRpcProxy.setNacosAddress(nacosAddress);
//        System.out.println("消费者");
//        System.out.println(consumer);
//        registerSingleton();
    }

    private void registerSingleton() throws ClassNotFoundException {
        List<String> services = consumer.getServices();
        if (services != null && services.size() != 0) {
            for (String service : services) {
                Class clazz = Class.forName(service);
                ((AnnotationConfigApplicationContext) applicationContext).getBeanFactory().registerSingleton(service, clazz.cast(SignRpcProxy.create(clazz)));
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
