package com.alibaba.www.pojo;

import lombok.Data;


@Data
public class FilterDefinition {

    //拦截器名称
    private String name;

    //自定义实现类全限定名
    private String sig;

}
