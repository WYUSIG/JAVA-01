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
>binlog_format=ROW
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

##### 初始化并启动mysql-5.7.33-winx64-1

>//建议一个数据库实例一个命令行窗口
>
>cd 进入mysql-5.7.33-winx64-1的bin目录
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

##### 分别在3个数据库实例上安装组复制插件group_replication.dll

>install plugin group_replication soname 'group_replication.dll';
>
>exit;
>
>重启mysql server,关闭掉start mysqld自动起的窗口再start mysqld
>
>start mysqld
>
>mysql -uroot -hlocalhost -P3316
>
>//验证是否安装成功
>
>show variables like '%group%';

##### 准备组复制第一个节点mysql-5.7.33-winx64-1

>
>
>