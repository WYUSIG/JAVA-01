package com.sign.www.concurrency;

public class Demo2 {

    private static volatile int data = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyRunable());
        thread.start();
        synchronized (thread){
            thread.wait();
            System.out.println(data);
        }
    }

    static class MyRunable implements Runnable{

        @Override
        public void run() {
            synchronized (this){
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data = 1;
            }
        }
    }
}
