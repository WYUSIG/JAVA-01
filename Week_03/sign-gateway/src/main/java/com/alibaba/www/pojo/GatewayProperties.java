package com.alibaba.www.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.www.exception.NoUniqueFilterDefinitionException;
import lombok.Data;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class GatewayProperties {

    //网关监听端口
    private int port;

    private Map<String,FilterDefinition> requestFilters = new HashMap<>();

    private Map<String,FilterDefinition> responseFilters = new HashMap<>();

    private Map<String,RouteDefinition> routes = new HashMap<>();

    public void loadProperties(AnnotationConfigApplicationContext applicationContext) throws NoUniqueFilterDefinitionException {
        Map<String, Object> yamlMap = applicationContext.getBeanFactory().getBean("gatewayMap", Map.class);
        Map<String,Object> sign = (Map<String,Object>)yamlMap.get("sign");
        Map<String,Object> gateway = (Map<String,Object>)sign.get("gateway");
        this.port = (int)gateway.get("port");
        List<Map<String,String>> requestSourceFilters = (ArrayList<Map<String,String>>)gateway.get("requestFilters");
        List<FilterDefinition> filterDefinitionList = requestSourceFilters.stream().map(this::parseFilter).collect(Collectors.toList());
        for(FilterDefinition filterDefinition : filterDefinitionList){
            if(this.requestFilters.containsKey(filterDefinition.getName())){
                throw new NoUniqueFilterDefinitionException("requestFilters repeat");
            }else {
                this.requestFilters.put(filterDefinition.getName(),filterDefinition);
            }
        }
        System.out.println(this.requestFilters);
        List<FilterDefinition> responseSourceFilters = (List<FilterDefinition>)gateway.get("responseFilters");
        for(FilterDefinition filterDefinition : responseSourceFilters){
            if(this.responseFilters.containsKey(filterDefinition.getName())){
                throw new NoUniqueFilterDefinitionException("responseFilters repeat");
            }else {
                this.responseFilters.put(filterDefinition.getName(),filterDefinition);
            }
        }
        List<RouteDefinition> sourceRoutes = (List<RouteDefinition>)sign.get("routes");
        System.out.println(sourceRoutes);
    }

    private FilterDefinition parseFilter(Map<String,String> map){
        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setName(map.get("name"));
        filterDefinition.setSig(map.get("sig"));
        return filterDefinition;
    }
}
