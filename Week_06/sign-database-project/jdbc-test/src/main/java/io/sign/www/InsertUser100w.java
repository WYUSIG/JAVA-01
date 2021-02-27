package io.sign.www;

import io.sign.www.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class InsertUser100w {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
//        insertUseStatement();
        insertUsePrepareStatement();
//        insertUsePrepareStatementMutipleThread();
    }


    public static void insertUseStatement() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection = JDBCUtil.getConnection();
        Statement statement = connection.createStatement();
        for (int i = 0; i < 10000; i++) {
            String sql = String.format("INSERT INTO shop_user(id,phone,user_name,avatar,passWord,create_time,update_time) VALUES(%d,%s,%s,%s,%s,%d,%d)", i, i, i, i, i, i, i);
            statement.execute(sql);
        }
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
        System.out.println("单线程用户表插入10000条数据所需时间：" + costTime + " ms");
    }

    public static void insertUsePrepareStatement() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection connection = JDBCUtil.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO shop_user(id,phone,user_name,avatar,passWord,create_time,update_time) VALUES(?,?,?,?,?,?,?)");
        for (int i = 0; i < 10000; i++) {
            String str = String.valueOf(i);
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
        long costTime = endTime - startTime;
        System.out.println("单线程用户表插入10000条数据所需时间：" + costTime + " ms");
    }


    public static void insertUsePrepareStatementMutipleThread() {
        long startTime = System.currentTimeMillis();
        System.out.println("开始时间："+startTime);
        for(int i = 0;i<30;i++){
            MyThread thread = new MyThread();
            thread.setAtomicInteger(atomicInteger);
            thread.start();
        }
    }



}
