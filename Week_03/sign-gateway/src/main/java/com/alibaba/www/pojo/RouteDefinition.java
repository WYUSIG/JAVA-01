package com.alibaba.www.pojo;

import org.springframework.stereotype.Component;

@Component
public class RouteDefinition {

    private String id;

    private String uri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "RouteDefinition{" +
                "id='" + id + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
