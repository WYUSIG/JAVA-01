package io.sign.www.spring.singleton;

/**
 * 懒汉模式--线程安全
 *
 * @author 钟显东
 */
public class Singleton_02 {

    private static Singleton_02 instance;

    private Singleton_02() {
    }

    public static synchronized Singleton_02 getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new Singleton_02();
        return instance;
    }
}
