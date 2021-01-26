package com.alibaba.www.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sign.gateway")
public class GatewayProperties {

    private List<RouteDefinition> routes;

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    @PostConstruct
    public void init(){
        System.out.println("PostConstruct");
        System.out.println(routes);
    }

    @Override
    public String toString() {
        return "GatewayProperties{" +
                "routes=" + routes +
                '}';
    }
}
