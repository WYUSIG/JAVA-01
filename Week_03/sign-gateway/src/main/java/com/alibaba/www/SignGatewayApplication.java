package com.alibaba.www;

import com.alibaba.www.inbound.HttpInboundServer;
import com.alibaba.www.pojo.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class SignGatewayApplication {

    public final static String GATEWAY_NAME = "sign-gateway";

    public final static String GATEWAY_VERSION = "1.0.0";

    public static void main(String[] args) {
        SpringApplication.run(SignGatewayApplication.class, args);
        String proxyPort = System.getProperty("proxyPort","8888");
        String proxyServers = System.getProperty("proxyServers","http://localhost:8801,http://localhost:8802");
        int port = Integer.parseInt(proxyPort);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" starting...");
        HttpInboundServer server = new HttpInboundServer(port, Arrays.asList(proxyServers.split(",")));
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" started at http://localhost:" + port + " for server:" + server.toString());
        try {
//            System.out.println(GatewayProperties.getRoutes());
            server.run();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
