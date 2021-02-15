package io.sign.www.spring.singleton;

/**
 * 懒汉模式--线程不安全
 *
 * @author 钟显东
 */
public class Singleton_01 {

    private static Singleton_01 instance;

    private Singleton_01() {
    }

    public static Singleton_01 getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new Singleton_01();
        return instance;
    }
}
