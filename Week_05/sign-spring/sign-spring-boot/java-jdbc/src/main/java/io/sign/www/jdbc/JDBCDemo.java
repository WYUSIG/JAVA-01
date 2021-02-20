package io.sign.www.jdbc;

import io.sign.www.util.JDBCUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @ClassName JDBCDemo
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/20 0020
 * @Version V1.0
 **/
public class JDBCDemo {

    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) {
        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();

            insert();
            System.out.println("第一次查询");
            query();
            update();
            delete();
            System.out.println("第二次查询");
            query();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.closeResource(statement, connection);
        }
    }

    public static void insert() throws Exception {
        String sql = "INSERT INTO student(name) VALUES('钟显东')";
        statement.execute(sql);
    }

    public static void query() throws Exception {
        String sql = "SELECT * FROM student";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            Integer id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            System.out.println("id:" + id + ",name:" + name);
        }
    }

    public static void update() throws Exception {
        String sql = "UPDATE student SET name = 'zxd' WHERE id = 5";
        statement.execute(sql);
    }

    public static void delete() throws Exception {
        String sql = "DELETE FROM student WHERE id = 6";
        statement.execute(sql);
    }
}
