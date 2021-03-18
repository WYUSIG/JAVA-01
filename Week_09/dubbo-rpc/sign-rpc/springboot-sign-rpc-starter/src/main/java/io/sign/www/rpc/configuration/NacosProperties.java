package io.sign.www.rpc.configuration;

import lombok.Data;

/**
 * RPC 注册中心 nacos 配置
 *
 * @author sign
 * @since 1.0
 **/
@Data
public class NacosProperties {

    /**
     * nacos 连接地址
     */
    private String address;

    /**
     * nacos 分组，相同分组的服务消费者和提供者会匹配
     */
    private String group;
}
