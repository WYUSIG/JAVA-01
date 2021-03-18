package io.sign.www.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务消费者启动类
 *
 * @author sign
 * @since 1.0
 **/
@SpringBootApplication
public class SignRpcClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(SignRpcClientApplication.class, args);
    }
}
