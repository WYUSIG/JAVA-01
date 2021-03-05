### Window10 Mysql5.7.33压缩包主从复制--异步复制实践



#### 主从复制--异步复制

##### 准备Mysql压缩包

>windows上可以用压缩版本，下载链接：https://dev.mysql.com/downloads/mysql/5.7.html
>
>选择64位**ZIP Archive**
>
>解压文件夹再复制一份，一个目录名为mysql-5.7.33-winx64-1，另一个名为mysql-5.7.33-winx64-2，都添加my.ini配置文件

##### 修改mysql-5.7.33-winx64-1 my.ini

>[mysql]
>default-character-set=utf8 
>[mysqld]
>basedir = ../
>datadir = ../data
>port = 3316
>server_id = 1
>
>sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES 
>log_bin=mysql-bin
>binlog-format=Row

##### 修改mysql-5.7.33-winx64-2 my.ini

>[mysql]
>default-character-set=utf8 
>[mysqld]
>basedir = ../
>datadir = ../data
>port = 3326
>server_id = 2
>
>sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES 
>log_bin=mysql-bin
>binlog-format=Row

##### 初始化和启动数据库mysql-5.7.33-winx64-1

>cd 进入mysql-5.7.33-winx64-1的bin目录，如果使用powershell需要./mysqld --defaults-file=../my.ini --initialize-insecure
>
>
>
>mysqld --defaults-file=../my.ini --initialize-insecure
>
>start mysqld

##### 初始化和启动数据库mysql-5.7.33-winx64-2

>cd 进入mysql-5.7.33-winx64-2的bin目录
>
>mysqld --defaults-file=../my.ini --initialize-insecure
>
>start mysqld

##### 把mysql-5.7.33-winx64-1配置成主节点

>//在mysql-5.7.33-winx64-1上登录
>
>mysql -uroot -hlocalhost -P3316
>
>//创建主从复制账号
>
>CREATE USER 'repl'@'%' IDENTIFIED BY '123456';
>
>//授权
>
>GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
>
>//刷新权限
>
>flush privileges;
>
>//查看binlog现在的位置
>
>show master status;

##### 把mysql-5.7.33-winx64-2配置成从节点

>//在mysql-5.7.33-winx64-2上登录
>
>mysql -uroot  -hlocalhost -P3326
>
>//747是上一步show master status查看的binlog位置
>
>```
>CHANGE MASTER TO
>    MASTER_HOST='localhost',  
>    MASTER_PORT = 3316,
>    MASTER_USER='repl',      
>    MASTER_PASSWORD='123456',   
>    MASTER_LOG_FILE='mysql-bin.000002',
>    MASTER_LOG_POS=747;
>```
>
>//启动从库
>
>start slave;

##### 验证

>//在mysql-5.7.33-winx64-1创建数据库db
>
>create schema db;
>
>use db;
>
>create table t1(id int) charset = utf8mb4;
>
>insert into t1 values(1),(2);
>
>//然后在mysql-5.7.33-winx64-2上看是否已经创建了db和t1以及数据

##### 断开从库

>//在mysql-5.7.33-winx64-2上执行
>
>stop slave;
>
>reset slave all;