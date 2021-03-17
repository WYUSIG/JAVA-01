package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcInject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务消费者启动类
 *
 * @author sign
 * @since 1.0
 **/
@SpringBootApplication
@RestController("/user")
public class SignRpcClientApplication {

    @SignRpcInject
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(SignRpcClientApplication.class, args);
    }

    @PostMapping("/getUser")
    public User getUser(int id) {
        return userService.findById(id);
    }
}
