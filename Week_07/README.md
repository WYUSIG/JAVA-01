[TOC]



#### 使用Aop自定义主从库读写分离

##### 设计思路：

主要是通过继承AbstractRoutingDataSource类，在构造器中调用父类3个方法

```
super.setDefaultTargetDataSource(defaultTargetDataSource);
super.setTargetDataSources(targetDataSources);
super.afterPropertiesSet();
```

- setDefaultTargetDataSource(Object defaultTargetDataSource) ：设置默认数据源
- setTargetDataSources(Map<Object, Object> targetDataSources)：设置数据源路由，key-value形式
- afterPropertiesSet()设置完后进行一些初始化工作

然后实现方法public Object determineCurrentLookupKey()，由方法名可得此方法需要我们返回目标路由的key，但是这个方法被Spring什么时候调用我们不好掌握，有一个比较好的隐式传参方式就是使用ThreadLocal，拦截ServiceImpl类方法来确定使用主库数据源还是从库数据源，然后存进ThreadLocal，然后Spring调用determineCurrentLookupKey的工作就是从ThreadLocal中取出放进去该线程的路由key，ThreadLocal使用的一个注意点是要注意清理ThreadLocal。

##### Aop拦截面考虑：

因为现在我们的业务代码基本都使用mybatis-plus这种有“BaseService”的框架，如果我们使用标注注解来判断切换数据源对代码侵入性太大了，可能需要在我们的业务代码重写mybatis-plus ServiceImpl<M extends BaseMapper<T>, T>的方法。因此我直接拦截业务代码service包下面的所有方法(自定义Service)和mybatis-plus的ServiceImpl类方法，通过判断方法名是否以save、add、insert、update、edit、delete、remove、drop等为前缀，如果是，则选择主库，否则使用从库。

##### 测试效果：

查询命中从库：

![图片](https://uploader.shimo.im/f/JePQ4Yc1jjJBKarT.png!thumbnail?fileGuid=9xDPQphRrV3xJYXR)

插入命中主库：

![图片](https://uploader.shimo.im/f/kgJsWoOvpJsxF61s.png!thumbnail?fileGuid=9xDPQphRrV3xJYXR)

#### 使用ShardingSphere-JDBC实现读写分离

##### 核心配置：

```
spring:
  shardingsphere:
    datasource:
      names: ds-master, ds-slave-1, ds-slave-2
      ds-master:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://localhost:3316/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
        username: root
        password:
      ds-slave-1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://localhost:3326/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
        username: root
        password:
      ds-slave-2:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://localhost:3336/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
        username: root
        password:
    masterslave:
      name: ms
      master-data-source-name: ds-master
      slave-data-source-names: ds-slave-1, ds-slave-2
```

业务代码正常编写即可



#### 使用ShardingSphere-proxy实现读写分离

##### ShardingSphere-proxy 中间件config-replica-query.yaml配置

```
schemaName: db

dataSourceCommon:
 username: root
 password:
 connectionTimeoutMilliseconds: 30000
 idleTimeoutMilliseconds: 60000
 maxLifetimeMilliseconds: 1800000
 maxPoolSize: 10
 minPoolSize: 1
 maintenanceIntervalMilliseconds: 30000

dataSources:
 primary_ds:
   url: jdbc:mysql://localhost:3316/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
 replica_ds_0:
   url: jdbc:mysql://localhost:3326/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
 replica_ds_1:
   url: jdbc:mysql://localhost:3336/db?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8

rules:
- !REPLICA_QUERY
 dataSources:
   pr_ds:
     name: pr_ds
     primaryDataSourceName: primary_ds
     replicaDataSourceNames:
       - replica_ds_0
       - replica_ds_1
```

##### 业务代码：

业务代码只需要连接ShardingSphere-proxy当作数据源，并使用ShardingSphere-proxy里面的虚拟数据库db，业务代码无需修改

##### ShardingSphere-proxy使用注意事项：

- 需要放到一个不含中文目录的路径下
- 需要把mysql jdbc驱动jar包放到ShardingSphere-proxy lib文件夹下

##### 测试效果：

根据ShardingSphere-proxy日志看到查询数据命中从库：

![图片](https://uploader.shimo.im/f/SL8wm8bDzasp1Y4y.png!thumbnail?fileGuid=9xDPQphRrV3xJYXR)

根据ShardingSphere-proxy日志看到插入数据命中主库：

![图片](https://uploader.shimo.im/f/eqKmubs8RVIKvJFJ.png!thumbnail?fileGuid=9xDPQphRrV3xJYXR)