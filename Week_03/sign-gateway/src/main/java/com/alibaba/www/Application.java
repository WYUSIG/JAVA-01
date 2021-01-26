package com.alibaba.www;

import com.alibaba.www.filter.AspectFilter;
import com.alibaba.www.inbound.HttpInboundServer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.AntPathMatcher;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;

@EnableAspectJAutoProxy
public class Application {

    public final static String GATEWAY_NAME = "sign-gateway";

    public final static String GATEWAY_VERSION = "1.0.0";

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(Application.class,
                AspectFilter.class);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        reader.loadBeanDefinitions("classpath:/META-INF/spring-context.xml");
        applicationContext.refresh();
        Map<String, Object> yamlMap = applicationContext.getBeanFactory().getBean("gatewayMap", Map.class);
        System.out.println(yamlMap);
        String proxyPort = System.getProperty("proxyPort","8888");
        String proxyServers = System.getProperty("proxyServers","http://localhost:8801,http://localhost:8802");
        int port = Integer.parseInt(proxyPort);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" starting...");
        HttpInboundServer server = new HttpInboundServer(port, Arrays.asList(proxyServers.split(",")));
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" started at http://localhost:" + port + " for server:" + server.toString());
        try {
            test();
            server.run();
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            applicationContext.close();
        }
    }


    private static void test(){
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match1 = antPathMatcher.match("/freebytes/**", "/freebytes/1getA");
        boolean match2 = antPathMatcher.match("/freebytes/*/get*", "/freebytes/te/getA");
        boolean match3 = antPathMatcher.match("/freebytes/*/get*", "/freebytes/te/1getA");
        System.out.println(match1);     //true
        System.out.println(match2);     //true
        System.out.println(match3);     //false
    }
}
