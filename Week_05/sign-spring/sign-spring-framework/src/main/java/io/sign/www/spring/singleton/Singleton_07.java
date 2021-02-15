package io.sign.www.spring.singleton;

/**
 * 枚举单例--线程安全
 * (Effective Java作者推荐,但是继承场景不可用)
 *
 * @author 钟显东
 */
public enum  Singleton_07 {

    INSTANCE;

    public void test(){
        System.out.println("hello world");
    }

    public static void main(String[] args) {
        Singleton_07.INSTANCE.test();
    }
}
