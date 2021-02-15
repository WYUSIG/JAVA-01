package io.sign.www.spring.singleton;

/**
 * 双重锁校验--线程安全
 *
 * @author 钟显东
 */
public class Singleton_05 {

    private static Singleton_05 instance;

    private Singleton_05() {
    }

    public static Singleton_05 getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (Singleton_05.class) {
            if (instance == null) {
                instance = new Singleton_05();
            }
        }
        return instance;
    }
}
