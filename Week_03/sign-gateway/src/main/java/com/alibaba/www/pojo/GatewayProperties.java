package com.alibaba.www.pojo;

import com.alibaba.www.exception.NoSuchFilterDefinitionException;
import com.alibaba.www.exception.NoUniqueFilterDefinitionException;
import com.alibaba.www.exception.NoUniqueRouteDefinitionException;
import lombok.Data;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GatewayProperties {

    public static final String HTTP_CLIENT_HANDLER = "httpclient";
//    public static final String OK_HTTP_HANDLER = "okhttp";   //未实现
    public static final String NETTY_HANDLER = "netty";    //已实现，但是压测rps只有65左右,hahaha

    public static final String RANDOM_STRATEGY = "random";
    public static final String POLLING_STRATEGY = "polling";

    //网关监听端口
    private int port;

    private String handler;

    private String strategy;

    private Map<String, FilterDefinition> requestFilters = new HashMap<>();

    private Map<String, FilterDefinition> responseFilters = new HashMap<>();

    private Map<String, RouteDefinition> routes = new HashMap<>();


    public void loadProperties(AnnotationConfigApplicationContext applicationContext) throws Exception {
        Map<String, Object> yamlMap = applicationContext.getBeanFactory().getBean("gatewayMap", Map.class);
        Map<String, Object> sign = (Map<String, Object>) yamlMap.get("sign");
        Map<String, Object> gateway = (Map<String, Object>) sign.get("gateway");
        this.port = (int) gateway.get("port");
        this.handler = String.valueOf(gateway.get("handler"));
        System.out.println("outbound处理方式为："+this.handler);
        this.strategy = String.valueOf(gateway.get("strategy"));
        System.out.println("负载均衡策略为："+this.strategy);
        List<Map<String, String>> requestSourceFilters = (ArrayList<Map<String, String>>) gateway.get("requestFilters");
        List<FilterDefinition> requestFilterDefinitionList = requestSourceFilters.stream().map(this::parseFilter).collect(Collectors.toList());
        for (FilterDefinition filterDefinition : requestFilterDefinitionList) {
            if (this.requestFilters.containsKey(filterDefinition.getName())) {
                throw new NoUniqueFilterDefinitionException("requestFilters repeat");
            } else {
                this.requestFilters.put(filterDefinition.getName(), filterDefinition);
            }
        }
        List<Map<String, String>> responseSourceFilters = (ArrayList<Map<String, String>>) gateway.get("responseFilters");
        List<FilterDefinition> responseFilterDefinitionList = responseSourceFilters.stream().map(this::parseFilter).collect(Collectors.toList());
        for (FilterDefinition filterDefinition : responseFilterDefinitionList) {
            if (this.responseFilters.containsKey(filterDefinition.getName())) {
                throw new NoUniqueFilterDefinitionException("responseFilters repeat");
            } else {
                this.responseFilters.put(filterDefinition.getName(), filterDefinition);
            }
        }
        List<Map<String, Object>> sourceRoutes = (ArrayList<Map<String, Object>>) gateway.get("routes");
        List<RouteDefinition> routeList = sourceRoutes.stream().map(this::parseRoute).collect(Collectors.toList());
        System.out.println("简略路由表信息：");
        System.out.println("路由id\t\t链接\t\t匹配规则");
        for (RouteDefinition routeDefinition : routeList) {
            if (null == routeDefinition.getRequestFilter() || null == routeDefinition.getResponseFilter()) {
                throw new NoSuchFilterDefinitionException("The route is configured with a non-existent filter");
            }
            if (this.routes.containsKey(routeDefinition.getId())) {
                throw new NoUniqueRouteDefinitionException("route repeat");
            }
            System.out.println(routeDefinition.getId()+"\t\t"+routeDefinition.getUri()+"\t\t"+routeDefinition.getPredicates());
            this.routes.put(routeDefinition.getId(), routeDefinition);
        }
    }

    private FilterDefinition parseFilter(Map<String, String> map) {
        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setName(map.get("name"));
        filterDefinition.setSig(map.get("sig"));
        return filterDefinition;
    }

    private RouteDefinition parseRoute(Map<String, Object> map) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(String.valueOf(map.get("id")));
        routeDefinition.setUri(String.valueOf(map.get("uri")));
        String uri = String.valueOf(map.get("uri"));
        uri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        routeDefinition.setUri(uri);
        routeDefinition.setServerName(String.valueOf(map.get("serverName")));
        routeDefinition.setRequestFilter(this.requestFilters.get(String.valueOf(map.get("requestFilter"))));
        routeDefinition.setResponseFilter(this.responseFilters.get(String.valueOf(map.get("responseFilter"))));
        List<Map<String, String>> sourcePredicates = (ArrayList<Map<String, String>>) map.get("predicates");
        List<String> pathList = sourcePredicates.stream().map(this::parsePredicates).collect(Collectors.toList());
        routeDefinition.setPredicates(pathList);
        return routeDefinition;
    }


    private String parsePredicates(Map<String, String> map) {
        String path = map.get("path");
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return path;
    }
}
