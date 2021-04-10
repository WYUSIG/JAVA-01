package io.sign.www;

import org.apache.activemq.broker.BrokerService;

public class ActiveMQServer {

    public static void main(String[] args) throws Exception {
        // 尝试用java代码启动一个ActiveMQ broker server
        // 然后用前面的测试demo代码，连接这个嵌入式的server
        // 创建 broker 服务器
        BrokerService brokerService = new BrokerService();
        // 设置使用 Jmx
        brokerService.setUseJmx(true);
        // 绑定服务地址
        brokerService.addConnector("tcp://localhost:61616");
        // 启动服务器
        brokerService.start();
        //维持ActiveMq持续运行
        synchronized (ActiveMQServer.class){
            ActiveMQServer.class.wait();
        }
    }
}
