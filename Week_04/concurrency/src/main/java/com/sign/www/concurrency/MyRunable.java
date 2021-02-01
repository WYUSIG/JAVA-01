package com.sign.www.concurrency;

public class MyRunable implements Runnable{

    private int data;

    public MyRunable(int data) {
        this.data = data;
    }

    @Override
    public void run() {
        data = 1;
    }
}
