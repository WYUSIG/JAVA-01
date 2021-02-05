package com.sign.www.concurrency;

public class Demo1_Thred_join {

    private static volatile int data = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(()->{
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            data = 1;
        });
        thread.start();
        thread.join();
        System.out.println(data);
    }
}
