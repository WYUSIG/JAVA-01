package com.alibaba.www.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.www.exception.NoSuchFilterDefinitionException;
import com.alibaba.www.exception.NoSuchRouteDefinitionException;
import com.alibaba.www.exception.NoUniqueFilterDefinitionException;
import com.alibaba.www.exception.NoUniqueRouteDefinitionException;
import com.alibaba.www.filter.HttpRequestFilter;
import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import lombok.Data;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
public class GatewayProperties {

    public static final String HTTP_CLIENT_HANDLER = "httpclient4";
    public static final String OK_HTTP_HANDLER = "okhttp";
    public static final String NETTY = "netty";

    public static final String RANDOM_STRATEGY = "random";
    public static final String POLLING_STRATEGY = "polling";

    //网关监听端口
    private int port;

    private String handler;

    private String strategy;

    private Map<String, FilterDefinition> requestFilters = new HashMap<>();

    private Map<String, FilterDefinition> responseFilters = new HashMap<>();

    private Map<String, RouteDefinition> routes = new HashMap<>();

//    private Map<String,String> routePathMatcherCache = new ConcurrentHashMap<>();

    //NoUniqueFilterDefinitionException,NoUniqueRouteDefinitionException
    public void loadProperties(AnnotationConfigApplicationContext applicationContext) throws Exception {
        Map<String, Object> yamlMap = applicationContext.getBeanFactory().getBean("gatewayMap", Map.class);
        Map<String, Object> sign = (Map<String, Object>) yamlMap.get("sign");
        Map<String, Object> gateway = (Map<String, Object>) sign.get("gateway");
        this.port = (int) gateway.get("port");
        this.handler = (String) gateway.get("handler");
        this.strategy = (String) gateway.get("strategy");
        List<Map<String, String>> requestSourceFilters = (ArrayList<Map<String, String>>) gateway.get("requestFilters");
        List<FilterDefinition> requestFilterDefinitionList = requestSourceFilters.stream().map(this::parseFilter).collect(Collectors.toList());
        for (FilterDefinition filterDefinition : requestFilterDefinitionList) {
            if (this.requestFilters.containsKey(filterDefinition.getName())) {
                throw new NoUniqueFilterDefinitionException("requestFilters repeat");
            } else {
                this.requestFilters.put(filterDefinition.getName(), filterDefinition);
            }
        }
//        System.out.println(this.requestFilters);
        List<Map<String, String>> responseSourceFilters = (ArrayList<Map<String, String>>) gateway.get("responseFilters");
        List<FilterDefinition> responseFilterDefinitionList = responseSourceFilters.stream().map(this::parseFilter).collect(Collectors.toList());
        for (FilterDefinition filterDefinition : responseFilterDefinitionList) {
            if (this.responseFilters.containsKey(filterDefinition.getName())) {
                throw new NoUniqueFilterDefinitionException("responseFilters repeat");
            } else {
                this.responseFilters.put(filterDefinition.getName(), filterDefinition);
            }
        }
//        System.out.println(this.responseFilters);
        List<Map<String, Object>> sourceRoutes = (ArrayList<Map<String, Object>>) gateway.get("routes");
        List<RouteDefinition> routeList = sourceRoutes.stream().map(this::parseRoute).collect(Collectors.toList());
        for (RouteDefinition routeDefinition : routeList) {
            if (null == routeDefinition.getRequestFilter() || null == routeDefinition.getResponseFilter()) {
                throw new NoSuchFilterDefinitionException("The route is configured with a non-existent filter");
            }
            if (this.routes.containsKey(routeDefinition.getId())) {
                throw new NoUniqueRouteDefinitionException("route repeat");
            }
            this.routes.put(routeDefinition.getId(), routeDefinition);
        }
//        System.out.println(routeList);
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
        uri = uri.endsWith("/")?uri.substring(0,uri.length()-1):uri;
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
        return map.get("path");
    }


    public RouteDefinition getRouteByRequestUri(String uri){
        List<RouteDefinition> res = new ArrayList<>();
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
        if(strategy.equals(POLLING_STRATEGY)){
            return getRouteByPollingStrategy(res);
        }else {
            return getRouteByRandomStrategy(res);
        }
    }

    private RouteDefinition getRouteByRandomStrategy(List<RouteDefinition> routeDefinitionList){
        if(routeDefinitionList.size() == 0){
            return null;
        }
        Random r = new Random(System.currentTimeMillis());
        int ran = r.nextInt(routeDefinitionList.size());
        return routeDefinitionList.get(ran);
    }

    private RouteDefinition getRouteByPollingStrategy(List<RouteDefinition> routeDefinitionList){
        if(routeDefinitionList.size() == 0){
            return null;
        }
        routeDefinitionList.sort(Comparator.comparingInt(RouteDefinition::getCount));
        return routeDefinitionList.get(0);
    }
}
