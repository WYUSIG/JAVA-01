package io.sign.www.spring.singleton;

/**
 * 使用类的内部类--线程安全
 * (既保证了线程安全又保证了懒加载，使用了JVM类加载机制)
 *
 * @author 钟显东
 */
public class Singleton_04 {

    private static class SingletonHolder {
        private static Singleton_04 instance = new Singleton_04();
    }

    private Singleton_04() {
    }

    public static synchronized Singleton_04 getInstance() {
        return SingletonHolder.instance;
    }
}
