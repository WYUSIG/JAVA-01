package com.alibaba.www.pojo;

import java.util.List;

public class RouteDefinition {

    private String id;

    private String uri;

    private FilterDefinition requestFilter;

    private FilterDefinition responseFilter;

    private Predicates predicates;

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

    public FilterDefinition getRequestFilter() {
        return requestFilter;
    }

    public void setRequestFilter(FilterDefinition requestFilter) {
        this.requestFilter = requestFilter;
    }

    public FilterDefinition getResponseFilter() {
        return responseFilter;
    }

    public void setResponseFilter(FilterDefinition responseFilter) {
        this.responseFilter = responseFilter;
    }

    public Predicates getPredicates() {
        return predicates;
    }

    public void setPredicates(Predicates predicates) {
        this.predicates = predicates;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RouteDefinition{");
        sb.append("id='").append(id).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", requestFilter=").append(requestFilter);
        sb.append(", responseFilter=").append(responseFilter);
        sb.append(", predicates=").append(predicates);
        sb.append('}');
        return sb.toString();
    }
}
