package com.sign.www.concurrency;

import java.util.concurrent.Semaphore;

/**
 * @ClassName Demo6
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/5 0005
 * @Version V1.0
 **/
public class Demo6_Thread_isAlive {

    private static volatile int i = 0;

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
        });
        thread.start();
        while (thread.isAlive()) {
        }
        System.out.println(i);
    }
}
