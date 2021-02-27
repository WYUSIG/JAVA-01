[TOC]



### 作业相关

#### 简易电商系统数据库DDL：

```
CREATE DATABASE `java01_shop_test`
USE `java01_shop_test`;

# 用户表
create table if not exists java01_shop_test.shop_user
(
	id int primary key comment '用户雪花id',
	phone varchar(15) null comment '电话',
	user_name varchar(15) null comment '用户姓名',
	avatar varchar(30) null comment '头像',
	pass_word varchar(20) null comment '密码',
	create_time long null comment '创建时间',
	update_time long null comment '最近修改时间'
)
charset=utf8mb4;

# 店铺表
create table if not exists java01_shop_test.shop_store
(
	id int primary key comment '店铺雪花id',
	store_name varchar(30) null comment '店铺名称',
	store_logo varchar(100) null comment '店铺logo',
	background varchar(30) null comment '店铺背景',
	phone int not null comment '电话',
	pass_word int null default 0 comment '密码',
	create_time long null comment '创建时间',
	update_time long null comment '最近修改时间'
)
charset=utf8mb4;

# 商品表
create table if not exists java01_shop_test.shop_goods
(
	id int primary key comment '商品雪花id',
	goods_name varchar(30) null comment '商品名称',
	goods_desc varchar(100) null comment '商品简单描述',
	goods_cover varchar(30) null comment '商品封面',
	store_id int not null comment '店铺id',
	visits int null default 0 comment '访问量',
	sale_sum int null default 0 comment '成交量',
	create_time long null comment '创建时间',
	update_time long null comment '最近修改时间'
)
charset=utf8mb4;

# sku表
create table if not exists java01_shop_test.shop_sku
(
	id int primary key comment 'sku雪花id',
	goods_id varchar(15) not null comment '商品id',
	name varchar(30) null comment 'sku名称',
	price varchar(100) null comment '价格',
	count varchar(30) null comment '库存',
	create_time long null comment '创建时间',
	update_time long null comment '最近修改时间'
)
charset=utf8mb4;

# 订单表
create table if not exists java01_shop_test.shop_order
(
	id int primary key comment '订单雪花id',
	order_code varchar(15) not null comment '订单编号',
	store_id varchar(30) null comment '店铺id',
	goods_id varchar(100) null comment '商品id',
	sku_id varchar(30) null comment 'sku id',
	goods_sum varchar(30) null comment '购买商品数',
	unit_price decimal(30) null comment '单价',
	sum_price decimal(30) null comment '总价',
	pay_price decimal(30) null comment '实际支付价格',
	pay_time varchar(30) null comment '支付时间',
	pay_type varchar(30) null comment '支付类型',
	order_status int null comment '订单状态',
	create_time long null comment '创建时间',
	update_time long null comment '最近修改时间'
)
charset=utf8mb4;
```

#### 插入100万订单数据测试结果

##### 单线程jdbc Statement

单线程jdbc Statement往订单表插入1万条数据(因为该方式实在太慢只能测少数推算)

实验结果：插入10000条数据花费44735ms，因而推算出单线程jdbc Statement插入100万条数据需要花费74.5分钟

##### 单线程jdbc PreparedStatement

实验结果：插入10000条数据花费33637ms，因而推算出单线程jdbc Statement插入100万条数据需要花费56分钟

##### 多线程jdbc PreparedStatement

10个线程：

实验结果：花费872.4s

20个线程：

实验结果：总共花费606s

30个线程：

实验结果：总共花费546s

40个线程：

实验结果：总共花费676.6s

##### CVS文件导入

实验结果：总共花费264s



##### 总结

前面的jdbc使用java代码编程测试例子，代码获取时间，转成String，时间相减等也有一定的时间损耗，为了测试我已经把数据和表设计得比较理想化了，但是总体感觉都不快，老师说能做到10s以下我觉得也太不可思议了~

### JDBC源码学习

#### java.sql.DriverManager

Driver管理器，提供注册Driver、获取Driver、获取Connection等功能

##### 问题

问：为什么我们一般Class.forName("数据库驱动包全限定类目")即可注册驱动？

- 因为数据库驱动 Driver 实现会显示地调用java.sql.DriverManager#registerDriver 方法

问：除了Class.forName显示加载和注册Driver外，还有什么方式？

- 通过 Java SPI ServiceLoader 获取 Driver 实现 加载顺序与 Class Path 的顺序有关系 

- 通过 “jdbc.drivers” 系统属性

##### 重要方法：

- getDriver:

```
@CallerSensitive
    public static Driver getDriver(String url)
        throws SQLException {

        println("DriverManager.getDriver(\"" + url + "\")");

        Class<?> callerClass = Reflection.getCallerClass();

        // Walk through the loaded registeredDrivers attempting to locate someone
        // who understands the given URL.
        for (DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            if(isDriverAllowed(aDriver.driver, callerClass)) {
                try {
                    if(aDriver.driver.acceptsURL(url)) {
                        // Success!
                        println("getDriver returning " + aDriver.driver.getClass().getName());
                    return (aDriver.driver);
                    }

                } catch(SQLException sqe) {
                    // Drop through and try the next driver.
                }
            } else {
                println("    skipping: " + aDriver.driver.getClass().getName());
            }

        }

        println("getDriver: no suitable driver");
        throw new SQLException("No suitable driver", "08001");
    }
```

总结：由源码可以看出，他是返回Driver注册列表(CopyOnWriteArrayList线程安全)里面第一个可联通的Driver，测试联通方法为：Driver#acceptsURL(url)

- getConnection

```
private static Connection getConnection(
        String url, java.util.Properties info, Class<?> caller) throws SQLException {
        /*
         * When callerCl is null, we should check the application's
         * (which is invoking this class indirectly)
         * classloader, so that the JDBC driver class outside rt.jar
         * can be loaded from here.
         */
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        synchronized(DriverManager.class) {
            // synchronize loading of the correct classloader.
            if (callerCL == null) {
                callerCL = Thread.currentThread().getContextClassLoader();
            }
        }

        if(url == null) {
            throw new SQLException("The url cannot be null", "08001");
        }

        println("DriverManager.getConnection(\"" + url + "\")");

        // Walk through the loaded registeredDrivers attempting to make a connection.
        // Remember the first exception that gets raised so we can reraise it.
        SQLException reason = null;

        for(DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            if(isDriverAllowed(aDriver.driver, callerCL)) {
                try {
                    println("    trying " + aDriver.driver.getClass().getName());
                    Connection con = aDriver.driver.connect(url, info);
                    if (con != null) {
                        // Success!
                        println("getConnection returning " + aDriver.driver.getClass().getName());
                        return (con);
                    }
                } catch (SQLException ex) {
                    if (reason == null) {
                        reason = ex;
                    }
                }

            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }

        }

        // if we got here nobody could connect.
        if (reason != null)    {
            println("getConnection failed: " + reason);
            throw reason;
        }

        println("getConnection: no suitable driver found for "+ url);
        throw new SQLException("No suitable driver found for "+ url, "08001");
    }
```

总结：对注册列表里面的逐个Driver尝试，如果可联通，则通过Driver#connect获取Connection，如果不为空就返回，即返回第一个可联通Driver可连接进去的Connection

#### java.sql.Connection

数据库连接接口，定义了创建Statement、PreparedStatement(预处理，防止sql注入)、CallableStatement(存储过程)以及事务的commit、roolback方法

#### java.sql.Statement

SQL命令接口

##### 最佳实践

- DML 语句 ：CRUD 

  R：java.sql.Statement#executeQuery

  CUD： java.sql.Statement#executeUpdate(java.lang.String) 

- DDL 语句

   java.sql.Statement#execute(java.lang.String)

  ​	-成功的话，不需要返回值（返回值 false） 

  ​	-失败的话，SQLException

#### java.sql.ResultSet

SQL执行结果接口

##### 重要方法：

- next()：即是把指针移到下一位，也会返回是否到结尾的boolean值

- getInt(int columnIndex)：通过下标(从1开始)获取int类型该行的指定列数据

- getInt(String columnLabel)：通过列名称获取列数据

- getString...（其他类型）

#### java.sql.ResultSetMetaData

ResultSet 元数据接口

##### 重要方法：

- getColumnCount：返回总列数

- getColumnLabel(int column)：返回SQL语句中AS的列表，如果sql中没有AS，那么返回实际列名

- getColumnName(int column)：返回实际列名

- getTableName：获取表名

#### java.sql.SQLException

##### 基本特点：

- 几乎所有的 JDBC API 操作都需要 try catch java.sql.SQLException 

- java.sql.SQLException 属于检查类型异常，继承 Exception

##### 推荐实践：

- 建议捕捉SQLExecption然后包装成RuntimeException，那么上层调用就不要写很多try catch
- 使用lamda编程，让调用者传入Function或Consumer进行处理异常如打印日志

### Mysql

#### Mysql锁测试总结

- 上S锁(共享锁)：

  ```
  select * from student lock in share mode;
  ```

- 上X锁(排他锁)

  ```
  select * from student for update;
  或者在事务里面
  update语句也会上排他锁
  ```

- 上锁的范围测试

  例如：select * from student where score>90 for update;

  - 如果score有索引，那么就会锁一部分数据(间隙锁)
  - 如果score没有索引，那么就会对表的所有数据上锁

- 间隙锁的非精确性
  - ​	间隙锁的范围由索引b+数的块决定，因此并没完全精确，比如89和90在同一个块上，那么score=90改行也会被锁住

#### Mysql事务

Mysql事务隔离级别

* 通过show variables可以查看当前数据库事务隔离级别，通过设置transaction-isolation的值改变事务隔离级别
* 读未提交(READ UNCOMMITTED)：一个事务还没提交时，它做的数据变更就能被其他的事务看到。
* 读提交(READ COMMITTED)：一个事务提交之后，它做的变更才会被其他事务看到。
* 可重复读(REPEATABLE READ)：一个事务执行过程中看到的数据，总是跟这个事务在启动时看到的数据是一致的。(事务隔离是通过回滚日志实现的，系统会判断没有事务在使用这些回滚日志时，回滚日志就会被删除，如果长事务可能导致回滚日志文件过大。)
* 串行化(SERIALIZABLE)：顾名思义就是对于同一行记录，写会加写锁，读会加读锁，当出现读写锁冲突时，后访问的事务必须等前一个事务执行完成，才能继续执行。

不同事务隔离级别出现的问题

* 脏读：读到了其他事务还没提交的数据。(读未提交)
* 不可重复读：对某数据进行读取，发现两次读取的结果不同。注意是多次读同一行数据。(读未提交、读已提交)
* 幻读：如果在事务A执行过程中，先查询全部数据或数据总数等，事务B新增了数据，然后如果事务A再次查询全部数据或数据总数等，就会发现数据条数不一样，产生了幻读。(读未提交、读已提交、可重复读)

#### 数据库执行查询sql流程

* 客户端->连接器->(查询缓存)/分析器->优化器->执行器->存储引擎
* 连接器：管理连接，权限验证
  * 连接命令：

```plain
mysql -h$ip -P$port -u$user -p
//然后在交互窗口输入命令
//如果密码错误会收到Access denied for user
//密码通过后连接器就会到权限表里面查出你拥有的权限
```

    * 查看连接列表命令：

```plain
show processlist
```

    * 默认没有动静8小时后自动断开，可通过wait_timeout来指定

* 查询缓存：命中直接返回结果
  * 查询缓存往往弊大于利，默认不开启，只要对一个表的更新，那么这个表的所有查询缓存都会被清空，除非是一张静态表才推荐使用，但是在Mysql8.0版本讲查询缓存功能去掉了
  * 可以将参数query_cachw_type设置成DEMAND这样就默认不使用查询缓存，可通过在查询sql加SQL_CACHE显示指定使用

```plain
select SQL_CACHE * from T where ID=10
```

* 分析器：此法分析，语法分析
  * 词法分析：需要把关键字识别出来
  * 语法分析：根据词法分析的结果根据语法规则来判断是否满足mysql语法比如select少打一个s
* 优化器：执行计划生成，索引选择

```plain
select * from t1 join t2 using(ID) where t1.c=10 and t2.d=20;
//既可以选择从t1取出c=10再关联t2判断d值是否等于20
//也可以选择从t2取出d=20在关联t1判断c的值是否等于10
//两种执行方法逻辑结果一样但是执行效率会有不同，优化器会决定选择哪一种方案
```

* 执行器：操作引擎，返回结果
  * 开始执行的时候，会先判断一下你对这个表有没有查询权限
* 存储引擎：存储数据，提供读写接口，默认InnoDB，可以在create table语句中使用engine=存储引擎名称来指定

#### 数据库执行更新sql流程

* 更新流程依旧需要经过连接器->分析器->优化器->执行器->存储引擎，但是涉及三个重要的日志模块：undo log(回滚日志)、redo log(重做日志)和binlog(归档日志)
* redo log：
  * redo log是InnoDB独有的，大小是固定的，循环写，写到末尾又回到开头循环写，redo log是物理日志，记录的是在某个数据页上做了什么修改。
  * redo log有两个指针：write pos(当前记录的位置)，一边写一边后移，checkpoint(当前要擦除的位置)，一边擦除一边后移，write pos和checkpoint之间的是空闲部分，如果空闲部分已经用完，又有新的记录需要写就会停下来，将一些操作记录更新到磁盘(真正的数据库文件)，把checkpoint推进一下。
  * 有了redo log，InnoDB就可以保证即使数据库发生异常重启，之前提交的记录也不会丢失，即crash-safe
  * 建议把innodb_flush_log_at _trx_commit这个参数设置成1，表示每次事务的redo log都持久化到磁盘，这样可以保证redo log日志不丢失，进而mysql异常重启之后数据不丢失。
* binlog：
  * binlog属于Server层，binlog只能用于归档，没有crash-safe能力，binlog是逻辑日志，binlog有两种模式，statement格式的话是记录sql语句，row格式的话记录行的内容，两条，更新前和更新后，一般使用row格式。
  * binlog是追加写，写到一定大小后会切换到写一个，不会覆盖以前的日志。
  * 建议把sync_binlog这个参数设置成1，表示每次事务的的binlog都持久化到磁盘，这样可以保证mysql异常重启之后binlog不丢失。
* 更新语句在执行器后的流程：

```plain
update T set c=c+1 where ID=2;
```

    * 执行器先找引擎取ID=2这一行，如果ID有索引会使用索引树进行搜索找到这一行，如果ID=2这一行所在的数据页本来就在内存中，那么直接返回给执行器；否则，需要先从磁盘读到内存再返回。
    * 执行器拿到引擎给的行数据，把这个值加1，得到新的行数据，再调用引擎写入这行新数据。
    * 引擎将这行新数据更新到内存中，同时将这个更新操作记录在redo log里面，此时redo log处于prepare状态，然后告知执行器完成了，随时可以提交事务。
    * 执行器生成这个操作的binlog，并把binlog写入磁盘。
    * 执行器调用引擎的提交事务接口，引擎把刚刚写入的redo log改成提交(commit)状态，更新完成。