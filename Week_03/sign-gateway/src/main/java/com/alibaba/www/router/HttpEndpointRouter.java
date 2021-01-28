package com.alibaba.www.router;

import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface HttpEndpointRouter {

    RouteDefinition route();

    default List<RouteDefinition> pathMatcher(GatewayProperties gatewayProperties,String uri){
        List<RouteDefinition> res = new ArrayList<>();
        Map<String,RouteDefinition> routes = gatewayProperties.getRoutes();
        for(Map.Entry<String, RouteDefinition> entry : routes.entrySet()){
            RouteDefinition routeDefinition = entry.getValue();
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            List<String> patternPathList = routeDefinition.getPredicates();
            for(String pattern : patternPathList){
                boolean march = antPathMatcher.match(pattern,uri);
                if(march == true){
                    res.add(routeDefinition);
                    break;
                }
            }
        }
        return res;
    }
}
