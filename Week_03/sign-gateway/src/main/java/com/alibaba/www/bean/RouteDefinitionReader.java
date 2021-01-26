package com.alibaba.www.bean;

import java.util.List;

/**
 * @ClassName GatewayDefinitionReader
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/26 0026
 * @Version V1.0
 **/
public interface RouteDefinitionReader {

    default int loadRoutes(List<RouteDefinition> routeDefinitionList){
        return 0;
    }
}
