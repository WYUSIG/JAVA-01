package com.alibaba.www.util;

import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import org.springframework.aop.framework.ProxyFactory;


/**
 * @ClassName FilterProxyFactory
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/28 0028
 * @Version V1.0
 **/
public class ProxyFactoryUtil {

    public static ProxyFactory getEndpointRouterProxyFactory(GatewayProperties gatewayProperties, String uri) throws Exception {
        String strategy = gatewayProperties.getStrategy();
        Class clazz;
        Object targetObject;
        if (strategy.equals(GatewayProperties.POLLING_STRATEGY)) {
            clazz = Class.forName("com.alibaba.www.router.PollingHttpEndpointRouter");
            targetObject = clazz.getDeclaredConstructor(GatewayProperties.class, String.class).newInstance(gatewayProperties, uri);
        } else {
            clazz = Class.forName("com.alibaba.www.router.RandomHttpEndpointRouter");
            targetObject = clazz.getDeclaredConstructor(GatewayProperties.class, String.class).newInstance(gatewayProperties, uri);
        }
        ProxyFactory proxyFactory = new ProxyFactory(targetObject);
        return proxyFactory;
    }

    public static ProxyFactory getRequestFilterProxyFactory(RouteDefinition routeDefinition) throws Exception {
        // 目标对象(用户自定义)
        Class clazz = Class.forName(routeDefinition.getRequestFilter().getSig());
        Object targetObject = clazz.newInstance();
        // 注入目标对象（被代理）
        ProxyFactory proxyFactory = new ProxyFactory(targetObject);
        return proxyFactory;
    }

    public static ProxyFactory getResponseFilterProxyFactory(RouteDefinition routeDefinition) throws Exception {
        // 目标对象(用户自定义)
        Class clazz = Class.forName(routeDefinition.getResponseFilter().getSig());
        Object targetObject = clazz.newInstance();
        // 注入目标对象（被代理）
        ProxyFactory proxyFactory = new ProxyFactory(targetObject);
        return proxyFactory;
    }

}
