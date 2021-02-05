package com.sign.www.concurrency;

import java.util.concurrent.locks.LockSupport;

/**
 * @ClassName Demo9
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/5 0005
 * @Version V1.0
 **/
public class Demo9_LockSupport {

    private static volatile int i = 0;

    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();
        new Thread(()->{
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            LockSupport.unpark(mainThread);
        }).start();
        LockSupport.park();
        System.out.println(i);
    }
}
