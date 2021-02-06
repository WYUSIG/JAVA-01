package com.sign.www.concurrency;

/**
 * @ClassName Demo12
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/6 0006
 * @Version V1.0
 **/
public class Demo12_Thread_interrupt {

    private static volatile int i = 0;

    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            mainThread.interrupt();
        }).start();
        while (!mainThread.isInterrupted()) {

        }
        System.out.println(i);
    }
}
