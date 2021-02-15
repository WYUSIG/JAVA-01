package io.sign.www.spring.singleton;

import java.util.concurrent.atomic.AtomicReference;

/**
 * CAS(AtomicReference)--线程安全
 *
 * @author 钟显东
 */
public class Singleton_06 {

    private static final AtomicReference<Singleton_06> INSTANCE = new AtomicReference<>();

    private Singleton_06() {
    }

    public static Singleton_06 getInstance() {
        do{
            Singleton_06 instance = INSTANCE.get();
            if(instance != null){
                return instance;
            }
        }while (!INSTANCE.compareAndSet(null,new Singleton_06()));
        return INSTANCE.get();
    }
}
