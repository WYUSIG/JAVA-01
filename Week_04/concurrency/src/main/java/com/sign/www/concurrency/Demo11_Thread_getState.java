package com.sign.www.concurrency;


/**
 * @ClassName Demo11
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/6 0006
 * @Version V1.0
 **/
public class Demo11_Thread_getState {

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
        while (!thread.getState().toString().equals("TERMINATED")) {

        }
        System.out.println(i);
    }
}
