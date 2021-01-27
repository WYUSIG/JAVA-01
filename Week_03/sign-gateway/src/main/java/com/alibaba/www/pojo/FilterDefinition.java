package com.alibaba.www.pojo;

import java.util.Objects;

public class FilterDefinition {

    //拦截器名称
    private String name;

    //自定义实现类全限定名
    private String sig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterDefinition that = (FilterDefinition) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilterDefinition{");
        sb.append("name='").append(name).append('\'');
        sb.append(", sig='").append(sig).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
