### ShardingSphere-Proxy读写分离实践

>本文前提是已经装好mysql主从复制集群，当然测试读ShardingSphere-Proxy写分离也不一定非要搭主从复制集群，通过给不同的jdbc连接和观看ShardingSphere-Proxy日志，看它名字哪个库也可以测试读写分离功能



#### 实践前数据准备

>主节点执行
>
>create schema replica-query_db;
>
>use replica-query_db;
>
>create table users(id int primary key, name varchar(30));
>
>insert into users values(1, 'KK01'),(2, 'KK02'),(2, 'KK02');
>
>检查从节点是否已经users表数据
>
>//集群环境已经可以下一步，单机数据库看下方
>
>//另：假如想在一个数据库实例上验证读写分离，分别创建3个database，然后分别创建一样的表，插入相同的数据，在config-replica-query.yaml文件配置替换成3个数据库的连接

#### 安装ShardingSphere-Proxy

>下载Sharding-Sphere链接：https://mirrors.bfsu.edu.cn/apache/shardingsphere/5.0.0-alpha/apache-shardingsphere-5.0.0-alpha-shardingsphere-proxy-bin.tar.gz
>
>下载完解压，并放到一个不含中文的路径下
>
>下载mysql驱动jar包：https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.22
>
>下载完放到Sharding-Sphere解压文件的lib文件夹下

#### 修改ShardingSphere-Proxy配置文件

>进入Sharding-Sphere解压文件的conf目录，
>
>包留logback.xml文件及其内容
>
>清空config-replica-query.yaml文件的内容
>
>清空server.yaml文件的内容
>
>其他文件移走或删除

#### server.yaml文件配置

>```
>authentication:
> users:
>   root:
>     password: root
>
>props:
> max-connections-size-per-query: 1
> acceptor-size: 16  # The default value is available processors count * 2.
> executor-size: 16  # Infinite by default.
> proxy-frontend-flush-threshold: 128  # The default value is 128.
>   # LOCAL: Proxy will run with LOCAL transaction.
>   # XA: Proxy will run with XA transaction.
>   # BASE: Proxy will run with B.A.S.E transaction.
> proxy-transaction-type: LOCAL
> proxy-opentracing-enabled: false
> proxy-hint-enabled: false
> query-with-cipher-column: false
> sql-show: true
> check-table-metadata-enabled: false
>```

#### config-replica-query.yaml文件配置

>```
>schemaName: replica_query_db
>
>dataSourceCommon:
> username: root
> password: kIo9u7Oi0eg
> connectionTimeoutMilliseconds: 30000
> idleTimeoutMilliseconds: 60000
> maxLifetimeMilliseconds: 1800000
> maxPoolSize: 10
> minPoolSize: 1
> maintenanceIntervalMilliseconds: 30000
>
>dataSources:
> primary_ds:
>   url: jdbc:mysql://127.0.0.1:3316/replica-query_db?serverTimezone=UTC&useSSL=false
> replica_ds_0:
>   url: jdbc:mysql://127.0.0.1:3326/replica-query_db?serverTimezone=UTC&useSSL=false
> replica_ds_1:
>   url: jdbc:mysql://127.0.0.1:3336/replica-query_db?serverTimezone=UTC&useSSL=false
>
>rules:
>- !REPLICA_QUERY
> dataSources:
>   pr_ds:
>     name: pr_ds
>     primaryDataSourceName: primary_ds
>     replicaDataSourceNames:
>       - replica_ds_0
>       - replica_ds_1       
>```
>
>注意修改dataSourceCommon.password，dataSources的数据库链接

#### 启动ShardingSphere-Proxy

>cd 进入ShardingSphere-Proxy解压文件的bin目录
>
>./start.bat 3309

#### 连接ShardingSphere-Proxy

>//如果识别不了mysql那就进入一个数据库压缩包解压的bin目录执行
>
>mysql -uroot -hlocalhost -P3309 -proot

#### 测试读写分离

>执行下方命令查看ShardingSphere-Proxy启动窗口日志
>
>use replica-query_db;
>
>//注意日志是否出现ShardingSphere-SQL - Actual SQL: replica_ds_0 ::: select * from users，表明命中 replica_ds_0 从库
>
>select * from users where id = 1;
>
>//再次执行查询，看日志是否命中另一个从库replica_ds_1
>
>select * from users where id = 1;
>
>//插入一条数据，看日志是否命中主库primary_ds
>
>insert into users(name) values('KK04');