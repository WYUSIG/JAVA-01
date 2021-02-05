package com.sign.www.concurrency;

public class Demo2_synchronized_wait_notifyAll {

    private static volatile int data = 0;

    private static final Integer oo = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyRunable());
        thread.start();
        synchronized (oo){
            oo.wait();
            System.out.println(data);
        }
    }

    static class MyRunable implements Runnable{

        @Override
        public void run() {
            synchronized (oo){
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data = 1;
                oo.notifyAll();
            }
        }
    }
}
