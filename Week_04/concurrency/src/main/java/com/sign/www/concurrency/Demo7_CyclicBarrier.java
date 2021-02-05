package com.sign.www.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @ClassName Demo7
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/5 0005
 * @Version V1.0
 **/
public class Demo7_CyclicBarrier {

    private static volatile int i = 0;

    public static void main(String[] args) throws Exception{
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        new Thread(()->{
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
        }).start();
        cyclicBarrier.await();
        System.out.println(i);
    }
}
