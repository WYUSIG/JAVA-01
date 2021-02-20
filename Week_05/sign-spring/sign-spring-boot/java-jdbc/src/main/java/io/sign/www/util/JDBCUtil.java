package io.sign.www.util;

import java.sql.*;

/**
 * @ClassName JDBCUtil
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/20 0020
 * @Version V1.0
 **/
public class JDBCUtil {

    private static String url = "jdbc:mysql://localhost:6657/kk_data_base?serverTimezone=CTT&useUnicode=true&characterEncoding=utf-8";
    private static String user = "root";
    private static String password = "kIo9u7Oi0eg";
    private static String driver = "com.mysql.cj.jdbc.Driver";
    static{
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取connetion对象
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;

    }

    // 关闭资源
    public static void closeResource(Statement state, ResultSet re, Connection conn) {
        if (state != null) {
            try {
                state.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        closeResultSet(re);
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    // 关闭资源
    public static void closeResource(Statement state, Connection conn) {
        if (state != null) {
            try {
                state.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 回滚操作
    public static void rollBack(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 关闭资源
    public static void closeResultSet(ResultSet re) {
        if (re != null) {
            try {
                re.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
