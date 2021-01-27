package com.alibaba.www.pojo;

import java.util.List;

/**
 * @ClassName Predicates
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/27 0027
 * @Version V1.0
 **/
public class Predicates {

    private List<Path> paths;
}

class Path{
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Path{");
        sb.append("path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
