### 自定义RPC：sign-rpc

#### 介绍

一个简洁人性化的rpc框架，基于Spring-boot-starter，Nacos服务注册发现，Netty网络通信。服务注册与服务注入均使用注解即可(@SignRpcService和@SignRpcRenference)，拦截器支持java SPI 扩展

#### 软件架构

Springboot + Netty + Httpclient + Nacos + java动态代理 + java SPI


#### 安装教程

1. 解压nacos：nacos在本项目的tool文件夹下，win系统选择nacos-server-2.0.0-BETA.zip，linux/mac系统选择nacos-server-2.0.0-BETA.tar.gz，然后移动到合适路径，解压

2. cd 进入解压好的nacos bin目录

3. 单机模式启动Nacos：win系统：startup.cmd  -m standalone；linux/mac系统：startup.sh  -m standalone

4. idea打开项目(需要idea安装lombok插件)，sign-rpc-example模块是使用示例，springboot-sign-rpc-starter模块是sign-rpc框架的spring-boot-starter

5. 启动服务消费者：demo-consumer

6. 启动服务提供者：demo-provider

7. 测试rpc调用：

   http://localhost:8888/user/getUser?id=1

   http://localhost:8888/order/getOder?id=1

#### 简单压测

启动单个服务提供者，20并发连续20秒，rps：2185.8

![](https://sign-pic-1.oss-cn-shenzhen.aliyuncs.com/img/1616141905(1).jpg)

### hmily-tcc

主要是参照官方demo来写的，在学习官方demo过程中发现项目必须路径不能有中文，否则读取hmily.yal会读取失败，其实发现官方demo余额可以减为负数，且payment方法无论是否够余额都会confirm，因此修改提交了个pr，嘿嘿~