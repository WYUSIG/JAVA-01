### ShardingSphere-Proxy分库分表实践

>本文不要求mysql主从复制集群，主要是测试分库分表，多个库可以在同一个mysql实例上，方便测试，但是最佳实践还是分的不同库在不同数据库实例上，本文讲分为2库2表，即原先一个表会分成4份，分库分表规则按2取模



#### 实践前数据准备

>//创建两个数据库 demo_ds_0和demo_ds_1，然后分别都创建表t_order_0、t_order_1、t_order_item_0、t_order_item_1
>
>```
>create schema demo_ds_0;
>create schema demo_ds_1;
>
>CREATE TABLE IF NOT EXISTS demo_ds_0.t_order_0 (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
>CREATE TABLE IF NOT EXISTS demo_ds_0.t_order_1 (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
>CREATE TABLE IF NOT EXISTS demo_ds_1.t_order_0 (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
>CREATE TABLE IF NOT EXISTS demo_ds_1.t_order_1 (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
>
>CREATE TABLE IF NOT EXISTS demo_ds_0.t_order_item_0 (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id));
>CREATE TABLE IF NOT EXISTS demo_ds_0.t_order_item_1 (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id));
>CREATE TABLE IF NOT EXISTS demo_ds_1.t_order_item_0 (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id));
>CREATE TABLE IF NOT EXISTS demo_ds_1.t_order_item_1 (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id));
>```

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
>清空config-sharding.yaml文件的内容
>
>清空server.yaml文件的内容
>
>其他文件移走或删除

#### server.yaml文件配置

>```
>authentication:
>users:
>root:
>password: root
>
>props:
>max-connections-size-per-query: 1
>acceptor-size: 16  # The default value is available processors count * 2.
>executor-size: 16  # Infinite by default.
>proxy-frontend-flush-threshold: 128  # The default value is 128.
># LOCAL: Proxy will run with LOCAL transaction.
># XA: Proxy will run with XA transaction.
># BASE: Proxy will run with B.A.S.E transaction.
>proxy-transaction-type: LOCAL
>proxy-opentracing-enabled: false
>proxy-hint-enabled: false
>query-with-cipher-column: false
>sql-show: true
>check-table-metadata-enabled: false
>```

#### config-sharding.yaml文件配置

>```
>schemaName: sharding_db
>
>dataSourceCommon:
> username: root
> password: kIo9u7Oi0eg
> connectionTimeoutMilliseconds: 30000
> idleTimeoutMilliseconds: 60000
> maxLifetimeMilliseconds: 1800000
> maxPoolSize: 5
> minPoolSize: 1
> maintenanceIntervalMilliseconds: 30000
>
>dataSources:
> ds_0:
>   url: jdbc:mysql://127.0.0.1:6657/demo_ds_0?serverTimezone=UTC&useSSL=false
> ds_1:
>   url: jdbc:mysql://127.0.0.1:6657/demo_ds_1?serverTimezone=UTC&useSSL=false
>
>rules:
>- !SHARDING
> tables:
>   t_order:
>     actualDataNodes: ds_${0..1}.t_order_${0..1}
>     tableStrategy:
>       standard:
>         shardingColumn: order_id
>         shardingAlgorithmName: t_order_inline
>     keyGenerateStrategy:
>       column: order_id
>       keyGeneratorName: snowflake
>   t_order_item:
>     actualDataNodes: ds_${0..1}.t_order_item_${0..1}
>     tableStrategy:
>       standard:
>         shardingColumn: order_id
>         shardingAlgorithmName: t_order_item_inline
>     keyGenerateStrategy:
>       column: order_item_id
>       keyGeneratorName: snowflake
> bindingTables:
>   - t_order,t_order_item
> defaultDatabaseStrategy:
>   standard:
>     shardingColumn: user_id
>     shardingAlgorithmName: database_inline
> defaultTableStrategy:
>   none:
> 
> shardingAlgorithms:
>   database_inline:
>     type: INLINE
>     props:
>       algorithm-expression: ds_${user_id % 2}
>   t_order_inline:
>     type: INLINE
>     props:
>       algorithm-expression: t_order_${order_id % 2}
>   t_order_item_inline:
>     type: INLINE
>     props:
>       algorithm-expression: t_order_item_${order_id % 2}
> 
> keyGenerators:
>   snowflake:
>     type: SNOWFLAKE
>     props:
>       worker-id: 123
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

#### 测试分库分表规则

>use sharding_db;
>
>//tb_order是config-sharding.yaml配置的bindingTables
>
>//执行下方插入语句，因为库是按user_id取2的模，t_order_表按order_id取2的模，查看该条记录是否被插入demo_ds_1.t_order_1
>
>insert into t_order(order_id,user_id,status) values(1,1,'OK');
>
>//查看该条记录是否被插入demo_ds_1.t_order_0
>
>insert into t_order(order_id,user_id,status) values(2,1,'FAIL');
>
>//查看该条记录是否被插入demo_ds_0.t_order_1
>
>insert into t_order(order_id,user_id,status) values(3,2,'OK');
>
>//查看该条记录是否被插入demo_ds_0.t_order_0
>
>insert into t_order(order_id,user_id,status) values(4,2,'OK');
>
>//最后执行查询所有，查看ShardingSphere-Proxy控制台可以看出它去每个库每个表都查了一遍最后整合给你
>
>select * from t_order;



