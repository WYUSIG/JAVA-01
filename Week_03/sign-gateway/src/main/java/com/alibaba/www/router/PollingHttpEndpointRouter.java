package com.alibaba.www.router;

import com.alibaba.www.pojo.GatewayProperties;
import com.alibaba.www.pojo.RouteDefinition;

import java.util.Comparator;
import java.util.List;

/**
 * @ClassName PollingHttpEndpointRouter
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/28 0028
 * @Version V1.0
 **/
public class PollingHttpEndpointRouter implements HttpEndpointRouter{

    private List<RouteDefinition> routeDefinitionList;

    public PollingHttpEndpointRouter(GatewayProperties gatewayProperties, String uri) {
        this.routeDefinitionList = pathMatcher(gatewayProperties, uri);
    }

    @Override
    public RouteDefinition route() {
        if(routeDefinitionList.size() == 0){
            return null;
        }
        routeDefinitionList.sort(Comparator.comparingInt(RouteDefinition::getCount));
        routeDefinitionList.get(0).setCount(routeDefinitionList.get(0).getCount()+1);
        return routeDefinitionList.get(0);
    }
}
