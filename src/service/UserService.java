package service;

import java.sql.*;
import java.util.Scanner;

public class UserService {
    private static int addToUser(Connection con,String name, String email) throws SQLException {
        String insertUserQuery="INSERT INTO USER (name,email) values(?,?)";
        PreparedStatement userPS= con.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);
        userPS.setString(1,name);
        userPS.setString(2,email);
        int count = userPS.executeUpdate();
        if(count>0){
            ResultSet temp=userPS.getGeneratedKeys();
            if(temp.next()){
                int uid= temp.getInt(1);
                WalletService.createWallet(con,uid);
                return uid;
            }
        }
        return -1;
    }
    private static void addToPh(Connection con, int uid, String phoneNo) throws SQLException {
        String insertQuery="INSERT INTO user_phoneno values(?,?)";
        PreparedStatement ps=con.prepareStatement(insertQuery);
        ps.setInt(1,uid);
        ps.setString(2,phoneNo);
        ps.executeUpdate();
    }
    public static int createUser(Connection con) throws SQLException {
        Scanner sc=new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name=sc.nextLine();
        System.out.print("Enter your email: ");
        String email=sc.nextLine();

        int uid=addToUser(con,name,email);
        if(uid!=-1){
            char choice='y';
            while(choice=='y' || choice=='Y'){
                System.out.print("Enter your phoneNo: ");
                String phoneNo=sc.next();
                addToPh(con,uid,phoneNo);
                System.out.print("Want to enter more phoneNo?(y,n): ");
                choice=sc.next().charAt(0);
            }
        }
        return uid;
    }
    public static void addDefaultAcc(Connection con,int uid,int upi_id) throws SQLException {
        String query="UPDATE user SET default_upi_id=? where uid=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,upi_id);
        ps.setInt(2,uid);
        ps.executeUpdate();
    }
    public static int getDefaultUPI(Connection con, int uid) throws SQLException {
        String query="SELECT default_upi_id from user where uid=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,uid);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            int upi= rs.getInt("default_upi_id");
            if (rs.wasNull()) {
                return -1;
            }
            return upi;
        }
        return -1;
    }
    public static boolean exists(Connection con,int uid) throws SQLException {
        String query="SELECT * from user where uid=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,uid);
        ResultSet rs=ps.executeQuery();
        return rs.next();
    }
}
