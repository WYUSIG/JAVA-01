package com.alibaba.www.router;

import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;

import java.util.List;
import java.util.Random;

public class RandomHttpEndpointRouter implements HttpEndpointRouter {

    private List<RouteDefinition> routeDefinitionList;

    public RandomHttpEndpointRouter(GatewayProperties gatewayProperties, String uri) {
        this.routeDefinitionList = pathMatcher(gatewayProperties, uri);
    }

    @Override
    public RouteDefinition route() {
        if(routeDefinitionList.size() == 0){
            return null;
        }
        if(routeDefinitionList.size() == 1){
            return routeDefinitionList.get(0);
        }
        Random r = new Random(System.currentTimeMillis());
        int ran = r.nextInt(routeDefinitionList.size());
        return routeDefinitionList.get(ran);
    }
}
