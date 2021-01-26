package com.alibaba.www.bean;

import org.springframework.stereotype.Component;

@Component
public class RouteDefinition {

    private String id;

    private String uri;

    private String serverName;

    private String prefix;

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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RouteDefinition{");
        sb.append("id='").append(id).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", serverName='").append(serverName).append('\'');
        sb.append(", prefix='").append(prefix).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
