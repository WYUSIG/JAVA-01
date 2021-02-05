package com.sign.www.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName Demo5
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/5 0005
 * @Version V1.0
 **/
public class Demo5_CountDownLatch {

    private static volatile int i = 0;

    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();
        System.out.println(i);
    }
}
