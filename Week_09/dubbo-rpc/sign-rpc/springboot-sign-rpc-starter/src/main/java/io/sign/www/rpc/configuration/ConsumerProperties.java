package io.sign.www.rpc.configuration;

import lombok.Data;

import java.util.List;

/**
 * 消费者配置元信息
 *
 * @author sign
 * @since 1.0
 **/
@Data
public class ConsumerProperties {

    private List<String> services;
}
