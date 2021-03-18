package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

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
