package com.sign.www.concurrency;

import java.util.concurrent.Semaphore;

/**
 * @ClassName Demo16_Semaphore
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/3/16 0016
 * @Version V1.0
 **/
public class Demo16_Semaphore {

    private static volatile int i = 0;

    public static void main(String[] args) throws Exception {
        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            semaphore.release();
        });
        thread.start();
        semaphore.acquire();
        System.out.println(i);
    }
}
