package com.alibaba.www.proxy;

import com.alibaba.www.filter.HttpRequestFilter;
import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.www.pojo.GatewayProperties.POLLING_STRATEGY;

/**
 * @ClassName FilterProxyFactory
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/28 0028
 * @Version V1.0
 **/
public class ProxyFactoryUtil {

    public static ProxyFactory getEndpointRouterProxyFactory(GatewayProperties gatewayProperties,String uri) throws Exception{
        String strategy = gatewayProperties.getStrategy();
        Class clazz;
        Object targetObject;
        if(strategy.equals(GatewayProperties.POLLING_STRATEGY)){
            clazz = Class.forName("com.alibaba.www.router.PollingHttpEndpointRouter");
            targetObject = clazz.getDeclaredConstructor(GatewayProperties.class,String.class).newInstance(gatewayProperties,uri);
        }else {
            clazz = Class.forName("com.alibaba.www.router.RandomHttpEndpointRouter");
            targetObject = clazz.getDeclaredConstructor(GatewayProperties.class,String.class).newInstance(gatewayProperties,uri);
        }
        ProxyFactory proxyFactory = new ProxyFactory(targetObject);
        proxyFactory.setTargetClass(clazz);
        return proxyFactory;
    }

    public static ProxyFactory getRequestFilterProxyFactory(RouteDefinition routeDefinition) throws Exception {
        // 目标对象(用户自定义)
        Class clazz = Class.forName(routeDefinition.getRequestFilter().getSig());
        Object targetObject = clazz.newInstance();
        // 注入目标对象（被代理）
        ProxyFactory proxyFactory = new ProxyFactory(targetObject);
        proxyFactory.setTargetClass(clazz);
        return proxyFactory;
    }

    public static ProxyFactory getOutboundHandlerProxyFactory(String handler,RouteDefinition routeDefinition) throws Exception{
        if(handler.equals(GatewayProperties.HTTP_CLIENT_HANDLER)){
            Class clazz = Class.forName("com.alibaba.www.outbound.httpclient4.HttpClientHttpOutboundHandler");
            Constructor constructor = clazz.getDeclaredConstructor(RouteDefinition.class);
            Object targetObject = constructor.newInstance(routeDefinition);
            ProxyFactory proxyFactory = new ProxyFactory(targetObject);
            proxyFactory.setTarget(targetObject);
            proxyFactory.setTargetClass(clazz);
            return proxyFactory;
        }else {
            return null;
        }
    }
}
