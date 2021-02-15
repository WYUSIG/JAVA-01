package io.sign.www.spring.singleton;

/**
 * 饿汉模式--线程安全
 *
 * @author 钟显东
 */
public class Singleton_03 {

    private static Singleton_03 instance = new Singleton_03();

    private Singleton_03() {
    }

    public static synchronized Singleton_03 getInstance() {
        return instance;
    }
}
