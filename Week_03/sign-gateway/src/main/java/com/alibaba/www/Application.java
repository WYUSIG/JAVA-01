package com.alibaba.www;

import com.alibaba.www.inbound.HttpInboundServer;
import com.alibaba.www.outbound.httpclient4.HttpClientHttpOutboundHandler;
import com.alibaba.www.outbound.netty4.Netty4HttpClient;
import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.util.SpringUtil;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
public class Application {

    public final static String GATEWAY_NAME = "sign-gateway";
    public final static String GATEWAY_VERSION = "1.0.0";

    @Bean
    public GatewayProperties gatewayProperties(){
        return new GatewayProperties();
    }

    @Bean
    public HttpInboundServer httpInboundServer(){
        return new HttpInboundServer();
    }

    @Bean
    public HttpClientHttpOutboundHandler httpClientHttpOutboundHandler(){
        return new HttpClientHttpOutboundHandler();
    }

    @Bean
    public Netty4HttpClient netty4HttpClient(){
        return new Netty4HttpClient();
    }

    @Bean
    public SpringUtil springUtil(){
        return new SpringUtil();
    }

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(Application.class);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        reader.loadBeanDefinitions("classpath:/META-INF/spring-context.xml");
        //加载网关配置元信息
        applicationContext.refresh();
        GatewayProperties gatewayProperties = applicationContext.getBean(GatewayProperties.class);
        gatewayProperties.loadProperties(applicationContext);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" starting...");
        HttpInboundServer server = applicationContext.getBean(HttpInboundServer.class);
        server.setPort(gatewayProperties.getPort());
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" started at http://localhost:" + gatewayProperties.getPort() + " for server:" + server.toString());
        try {
            server.run();
        }finally {
            applicationContext.close();
        }
    }

}
