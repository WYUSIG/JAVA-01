## 网关作业 ##
- 整合了httpclient
- outbound使用netty做客户端
- 使用了Spring framework的YamlMapFactoryBean进行yaml配置文件读取
- 允许用户自定义拦截器
- 使用了Spring的ProxyFactory进行动态代理
- 实现了路由及简单的负载均衡，路由正则匹配使用的是spring的AntPathMatcher

```
sign:
  gateway:
    port: 8888  #netty监听端口
    handler: httpclient  #设置outbound使用哪种，httpclient/netty
    strategy: polling  #负载平衡,如果通过断言匹配上多个route就会触发 random/polling
    requestFilters:  #定义request拦截器，路由直接使用其name即可
      - name: requestFilter1
        sig: com.alibaba.www.filter.HeaderHttpRequestFilter  #拦截器用户自定义实现类，需要继承HttpRequestFilter接口实现filter接口
    routes:
      - id: route1  
        uri: http://localhost:8804/test  #目标uri
        requestFilter: requestFilter1 #该路由使用的拦截器
        responseFilter: responseFilter1
        predicates:  #断言，可写多个
          - path: /test/*
      - id: route2
        uri: http://localhost:8801
        requestFilter: requestFilter1
        responseFilter: responseFilter1
        predicates:
          - path: /server1/*
    responseFilters:  #定义response拦截器，路由直接使用其name即可
      - name: responseFilter1
        sig: com.alibaba.www.filter.HeaderHttpResponseFilter
```

运行截图：
![图片](https://uploader.shimo.im/f/Q8NSdHqmwxVHv0gR.png!thumbnail?fileGuid=xWPjgHrGjYGTc9Cx)

![图片](https://uploader.shimo.im/f/F1Xu1Ubi2TDTJzhz.png!thumbnail?fileGuid=xWPjgHrGjYGTc9Cx)
不足之处：


- 技术不足，写得比较慢，注释基本没写
- httpclient未实现post及参数传递
- netty作为outbound qps我测试只有70，可能与我使用了.sync和错误的线程池使用姿势有关，但是不写.sync直接前端收不到结果
