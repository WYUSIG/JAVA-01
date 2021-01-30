## 网关作业 ##
- 整合了httpclient
- outbound使用netty做客户端
- 使用了Spring framework的YamlMapFactoryBean进行yaml配置文件读取
- 允许用户自定义拦截器
- 使用了Spring的ProxyFactory进行动态代理
- 实现了路由及简单的负载均衡

...
sign:
	  gateway:
	    port: 8888  #netty监听端口
	    handler: httpclient
	    strategy: polling
	    requestFilters:
	      - name: requestFilter1
	        sig: com.alibaba.www.filter.HeaderHttpRequestFilter
	    routes:
	      - id: route1
	        uri: http://localhost:8804/test
	        requestFilter: requestFilter1
	        responseFilter: responseFilter1
	        predicates:
	          - path: /test/*
	      - id: route2
	        uri: http://localhost:8801
	        requestFilter: requestFilter1
	        responseFilter: responseFilter1
	        predicates:
	          - path: /server1/*
	    responseFilters:
	      - name: responseFilter1
	        sig: com.alibaba.www.filter.HeaderHttpResponseFilter
...
