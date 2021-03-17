package io.sign.www.rpc;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务提供者启动类
 *
 * @author sign
 * @since 1.0
 **/
@SpringBootApplication
public class SignRpcServiceApplication {

    public static void main(String[] args) throws NacosException {
//        String userService = "io.kimking.rpcfx.demo.api.UserService";
//        String groupName = "SignRpc";
//        NamingService naming = NacosFactory.createNamingService("192.168.1.91:8848");
//        naming.registerInstance(userService,groupName, "192.168.1.91", 8848);
        SpringApplication.run(SignRpcServiceApplication.class, args);
    }
}
