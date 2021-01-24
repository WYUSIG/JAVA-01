package com.alibaba.www;

public class Application {

    public final static String GATEWAY_NAME = "sign-gateway";

    public final static String GATEWAY_VERSION = "1.0.0";

    public static void main(String[] args) {
        String proxyPort = System.getProperty("proxyPort","8888");
        String proxyServers = System.getProperty("proxyServers","http://localhost:8801,http://localhost:8802");

    }
}
