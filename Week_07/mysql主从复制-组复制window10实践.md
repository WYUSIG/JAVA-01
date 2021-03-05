### Window10 Mysql5.7.33压缩包主从复制--组复制实践



#### 主从复制--组复制

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
>gtid_mode=ON
>enforce_gtid_consistency=ON
>master_info_repository=TABLE
>relay_log_info_repository=TABLE
>binlog_checksum=NONE
>log_slave_updates=ON
>
>transaction_write_set_extraction=XXHASH64
>loose-group_replication_group_name="3db33b36-0e51-409f-a61d-c99756e90155"
>loose-group_replication_start_on_boot=off
>loose-group_replication_local_address= "192.168.1.91:3316"
>loose-group_replication_group_seeds= "192.168.1.91:3326,192.168.1.91:3336"
>loose-group_replication_bootstrap_group= off
>
>loose-group_replication_single_primary_mode=off
>loose-group_replication_enforce_update_everywhere_checks=on

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
>
>gtid_mode=ON
>enforce_gtid_consistency=ON
>master_info_repository=TABLE
>relay_log_info_repository=TABLE
>binlog_checksum=NONE
>log_slave_updates=ON
>
>transaction_write_set_extraction=XXHASH64
>loose-group_replication_group_name="3db33b36-0e51-409f-a61d-c99756e90155"
>loose-group_replication_start_on_boot=off
>loose-group_replication_local_address= "192.168.1.91:3326"
>loose-group_replication_group_seeds= "192.168.1.91:3316,192.168.1.91:3336"
>loose-group_replication_bootstrap_group= off
>
>loose-group_replication_single_primary_mode=off
>loose-group_replication_enforce_update_everywhere_checks=on

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
>
>gtid_mode=ON
>enforce_gtid_consistency=ON
>master_info_repository=TABLE
>relay_log_info_repository=TABLE
>binlog_checksum=NONE
>log_slave_updates=ON
>
>transaction_write_set_extraction=XXHASH64
>loose-group_replication_group_name="3db33b36-0e51-409f-a61d-c99756e90155"
>loose-group_replication_start_on_boot=off
>loose-group_replication_local_address= "192.168.1.91:3336"
>loose-group_replication_group_seeds= "192.168.1.91:3316,192.168.1.91:3326"
>loose-group_replication_bootstrap_group= off
>
>loose-group_replication_single_primary_mode=off
>loose-group_replication_enforce_update_everywhere_checks=on

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

##### 配置主节点mysql-5.7.33-winx64-1

>set sql_log_bin = 0;
>
>//select user,host from mysql.user;查看已创建用户，已创建则不需要再创建
>
>CREATE USER 'repl'@'%' IDENTIFIED BY '123456';
>
>grant replication slave on *.* to repl@'%';
>
>flush privileges;
>
>set sql_log_bin = 1;
>
>change master to master_user='repl', master_password='123456' for channel 'group_replication_recovery';
>
>install plugin group_replication soname 'group_replication.dll';
>
>set global group_replication_bootstrap_group = on;
>
>start group_replication;
>
>select * from performance_schema.replication_group_members;
>
>set global group_replication_bootstrap_group = off;

##### 配置从节点mysql-5.7.33-winx64-2

>set sql_log_bin = 0;
>
>//select user,host from mysql.user;查看已创建用户，已创建则不需要再创建
>
>CREATE USER 'repl'@'%' IDENTIFIED BY '123456';
>
>grant replication slave on *.* to repl@'%';
>
>install plugin group_replication soname 'group_replication.dll';
>
>set sql_log_bin =1;
>
>change master to master_user='repl', master_password='123456' for channel 'group_replication_recovery';
>
>start group_replication;
>
>select * from performance_schema.replication_group_members;

##### 配置从节点mysql-5.7.33-winx64-3

>set sql_log_bin = 0;
>
>//select user,host from mysql.user;查看已创建用户，已创建则不需要再创建
>
>CREATE USER 'repl'@'%' IDENTIFIED BY '123456';
>
>grant replication slave on *.* to repl@'%';
>
>install plugin group_replication soname 'group_replication.dll';
>
>set sql_log_bin =1;
>
>change master to master_user='repl', master_password='123456' for channel 'group_replication_recovery';
>
>start group_replication;
>
>select * from performance_schema.replication_group_members;

##### 验证

>//在主节点mysql-5.7.33-winx64-1上执行
>
>create schema db;
>
>//组复制如果一个表没有主键，那么就插入不了数据
>
>create table t1(id int primary key);
>
>insert into t1 values(1),(2);
>
>//在其他节点执行，看看数据有没有同步
>
>use db;
>
>select * from t1;