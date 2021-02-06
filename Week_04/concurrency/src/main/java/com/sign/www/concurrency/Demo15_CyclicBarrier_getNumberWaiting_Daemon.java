package com.sign.www.concurrency;

import java.util.concurrent.CyclicBarrier;

public class Demo15_CyclicBarrier_getNumberWaiting_Daemon {

    private static volatile int i = 0;

    public static void main(String[] args) throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
        while (cyclicBarrier.getNumberWaiting() == 0){

        }
        System.out.println(i);
    }
}
