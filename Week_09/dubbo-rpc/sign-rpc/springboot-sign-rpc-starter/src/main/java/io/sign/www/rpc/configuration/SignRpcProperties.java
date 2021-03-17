package io.sign.www.rpc.configuration;

import io.sign.www.rpc.client.SignRpcProxy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * RPC 配置实体类
 *
 * @author sign
 * @since 1.0
 **/
@ConfigurationProperties(prefix="spring.sign.rpc")
@Data
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SignRpcProperties {

    private String nacosAddress;

    private String type;

    private String providerIp;

    private int providerPort;

    @PostConstruct
    public void init() throws Exception{
        if (providerIp == null) {
            InetAddress ip4 = Inet4Address.getLocalHost();
            providerIp = ip4.getHostAddress();
        }
        SignRpcProxy.setNacosAddress(nacosAddress);
    }
}
