package com.alibaba.www.pojo;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RouteDefinition {

    private String id;

    private String uri;

    private String serverName;

    private FilterDefinition requestFilter;

    private FilterDefinition responseFilter;

    private List<String> predicates = new ArrayList<>();

    private int count = 0;


}
