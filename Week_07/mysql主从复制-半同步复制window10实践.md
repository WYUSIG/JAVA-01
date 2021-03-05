### Window10 Mysql5.7.33压缩包主从复制--半同步复制实践



#### 主从复制--半同步复制

##### 准备Mysql压缩包

>windows上可以用压缩版本，下载链接：https://dev.mysql.com/downloads/mysql/5.7.html
>
>选择64位**ZIP Archive**
>
>解压文件夹再复制两份，一个目录名为mysql-5.7.33-winx64-1，一个名为mysql-5.7.33-winx64-2，一个名为mysql-5.7.33-winx64-3，都添加my.ini配置文件

##### 修改mysql-5.7.33-winx64-1的my.ini

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
>
>rpl_semi_sync_master_enabled = 1
>rpl_semi_sync_master_timeout = 10000

##### 修改mysql-5.7.33-winx64-2的my.ini

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
>rpl_semi_sync_slave_enabled = 1

##### 修改mysql-5.7.33-winx64-3的my.ini

>[mysql]
>default-character-set=utf8 
>[mysqld]
>basedir = ../
>datadir = ../data
>port = 3336
>server_id = 3
>
>sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES 
>log_bin=mysql-bin
>binlog-format=Row
>rpl_semi_sync_slave_enabled = 1

##### 初始化并启动mysql-5.7.33-winx64-1

>//建议一个数据库实例一个命令行窗口
>
>cd 进入mysql-5.7.33-winx64-1的bin目录，如果使用powershell需要./mysqld --defaults-file=../my.ini --initialize-insecure
>
>
>
>mysqld --defaults-file=../my.ini --initialize-insecure
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3316

##### 初始化并启动mysql-5.7.33-winx64-2

>//建议一个数据库实例一个命令行窗口
>
>cd 进入mysql-5.7.33-winx64-2的bin目录
>
>mysqld --defaults-file=../my.ini --initialize-insecure
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3326

##### 初始化并启动mysql-5.7.33-winx64-3

>//建议一个数据库实例一个命令行窗口
>
>cd 进入mysql-5.7.33-winx64-3的bin目录
>
>mysqld --defaults-file=../my.ini --initialize-insecure
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3336

##### 在mysql-5.7.33-winx64-1上安装半同步复制插件semisync_master.dll

> install plugin rpl_semi_sync_master soname 'semisync_master.dll';
>
> exit;
>
> 重启mysql server,关闭掉start mysqld自动起的窗口再start mysqld
>
> start mysqld
>
> mysql -uroot -hlocalhost -P3316
>
> //验证是否安装成功
>
> show variables like '%semi%'

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

##### 在mysql-5.7.33-winx64-2上安装半同步复制插件semisync_slave.dll

>install plugin rpl_semi_sync_slave soname 'semisync_slave.dll';
>
>exit;
>
>./mysqld restart
>
>//重启mysql server,关闭掉start mysqld自动起的窗口再start mysqld
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3326
>
>//查看是否安装成功
>
>show variables like '%semi%';

##### 把mysql-5.7.33-winx64-2配置成从节点

>//在mysql-5.7.33-winx64-2上登录
>
>mysql -uroot  -hlocalhost -P3326
>
>//747是主库执行show master status查看的binlog位置
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

##### 在mysql-5.7.33-winx64-3上安装半同步复制插件semisync_slave.dll

>install plugin rpl_semi_sync_slave soname 'semisync_slave.dll';
>
>exit;
>
>./mysqld restart
>
>//重启mysql server,关闭掉start mysqld自动起的窗口再start mysqld
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3336
>
>//查看是否安装成功
>
>show variables like '%semi%';

##### 把mysql-5.7.33-winx64-3配置成从节点

>//在mysql-5.7.33-winx64-3上登录
>
>mysql -uroot  -hlocalhost -P3336
>
>//747是主库执行show master status查看的binlog位置
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

> //在mysql-5.7.33-winx64-1上执行
>
> create schema db1;
>
> //查看其他数据库有没有
>
> //在mysql-5.7.33-winx64-2/mysql-5.7.33-winx64-3上执行
>
> show schemas;

##### 断开从库以及卸载插件

>stop slave;
>
>reset slave all;
>
>
>
>uninstall plugin rpl_semi_sync_master;
>
>uninstall plugin rpl_semi_sync_slave;

##### 注意事项

> //start slave;后可以通过以下命令查看从库状态,确保Slave_IO_Running和Slave_SQL_Running都为Yes
>
> show slave status\G;
>
> //注意my.ini里面的server_id必须3个库都不一样
>
> //只有slave的my.ini配置了rpl_semi_sync_slave_enabled = 1重启，然后show variables like '%semi%'开会变成ON

##### 结果截图：

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/image-20210304165655872.png)

