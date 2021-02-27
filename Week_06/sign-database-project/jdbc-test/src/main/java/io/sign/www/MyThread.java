package io.sign.www;

import io.sign.www.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThread extends Thread {

    private AtomicInteger atomicInteger;

    public void setAtomicInteger(AtomicInteger atomicInteger) {
        this.atomicInteger = atomicInteger;
    }

    @Override
    public void run() {
        Connection connection = JDBCUtil.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO shop_user(id,phone,user_name,avatar,pass_word,create_time,update_time) VALUES(?,?,?,?,?,?,?)");
            int i;
            while ((i = atomicInteger.getAndAdd(1)) < 10000) {
                String str = String.valueOf(atomicInteger.get());
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, str);
                preparedStatement.setString(3, str);
                preparedStatement.setString(4, str);
                preparedStatement.setString(5, str);
                preparedStatement.setInt(6, i);
                preparedStatement.setInt(7, i);
                preparedStatement.executeUpdate();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("结束时间："+endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
