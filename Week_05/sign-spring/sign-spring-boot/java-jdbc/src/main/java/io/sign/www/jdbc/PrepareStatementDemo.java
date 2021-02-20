package io.sign.www.jdbc;

import io.sign.www.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @ClassName PrepareStatementDemo
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/20 0020
 * @Version V1.0
 **/
public class PrepareStatementDemo {

    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = JDBCUtil.getConnection();
            System.out.println("批量处理前:");
            query();
            insert();
            System.out.println("批量insert后:");
            query();
            delete();
            System.out.println("批量delete后:");
            query();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.closeResource(connection);
        }
    }

    public static void insert() throws Exception {
        String sql = "INSERT INTO student(name) VALUES(?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < 10; i++) {
                preparedStatement.setString(1, String.valueOf(i));
                preparedStatement.executeUpdate();
            }
        }finally {
            preparedStatement.close();
        }
    }

    public static void delete() throws Exception{
        String sql = "DELETE FROM student WHERE name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < 5; i++) {
                preparedStatement.setString(1, String.valueOf(i));
                preparedStatement.executeUpdate();
            }
        }finally {
            preparedStatement.close();
        }
    }

    public static void query() throws Exception {
        String sql = "SELECT * FROM student";
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("id:" + id + ",name:" + name);
            }
        }finally {
            statement.close();
        }

    }
}
