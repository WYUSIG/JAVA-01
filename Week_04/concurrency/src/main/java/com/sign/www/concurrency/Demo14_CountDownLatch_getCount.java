package com.sign.www.concurrency;

import java.util.concurrent.CountDownLatch;

public class Demo14_CountDownLatch_getCount {

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
        while (countDownLatch.getCount() != 0){

        }
        System.out.println(i);
    }
}
