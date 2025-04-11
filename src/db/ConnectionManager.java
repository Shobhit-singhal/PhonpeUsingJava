package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String driver="com.mysql.cj.jdbc.Driver";
    private static final String url ="jdbc:mysql://localhost:3306/phonpe_final";
    private static final String user="root";
    private static final String pwd="admin";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        return DriverManager.getConnection(url,user,pwd);
    }

}
