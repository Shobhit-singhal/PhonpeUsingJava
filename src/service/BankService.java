package service;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class BankService {
    public static int createBankAccount(Connection con, int uid) throws SQLException {
        Scanner sc=new Scanner(System.in);
        System.out.print("Enter your bankName: ");
        String bankName=sc.next();
        System.out.print("Enter your IFSC_code: ");
        String ifsc_code=sc.next();
        int acc_no=insertBank(con,bankName,ifsc_code,uid);
        if(acc_no!=-1){
            return linkBankToUpi(con,acc_no);
        }
        return -1;
    }
    public static ArrayList<Integer> getBankAccountByUid(Connection con, int uid) throws SQLException{
        String query="SELECT acc_no FROM bank_account where uid=?";
        ArrayList<Integer> acc=new ArrayList<>();
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,uid);
        ResultSet rs=ps.executeQuery();
        while(rs.next()){
            acc.add(rs.getInt("acc_no"));
        }
        return acc;
    }
    private static int insertBank(Connection con, String bankName,String ifsc_code, int uid) throws SQLException {
        String insertQuery="INSERT INTO bank_account(bankname,ifsc_code,uid) values(?,?,?)";
        PreparedStatement ps=con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,bankName);
        ps.setString(2,ifsc_code);
        ps.setInt(3,uid);
        int count=ps.executeUpdate();
        if(count>0){
            ResultSet rs=ps.getGeneratedKeys();
            if(rs.next()){
                return rs.getInt(1);
            }
        }
        return -1;
    }
    private static int linkBankToUpi(Connection con,int acc_no) throws SQLException {
        String insertQuery="INSERT INTO UPI(acc_no) VALUES (?)";
        PreparedStatement ps=con.prepareStatement(insertQuery,Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,acc_no);
        int count=ps.executeUpdate();
        if(count>0){
            ResultSet rs=ps.getGeneratedKeys();
            if(rs.next()){
                return rs.getInt(1);
            }
        }
        return -1;
    }
    public static double getBalance(Connection con,int acc_no) throws SQLException {
        String query="SELECT balance FROM bank_account where acc_no=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,acc_no);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            return rs.getDouble("balance");
        }
        return -1;
    }
    public static void deposit(Connection con,int acc_no,double amt) throws SQLException {
        String query="UPDATE bank_account set balance=balance+? where acc_no=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setDouble(1,amt);
        ps.setInt(2,acc_no);
        ps.executeUpdate();
    }
    public static boolean withdraw(Connection con,int acc_no,double amt) throws SQLException {
        String query="UPDATE bank_account set balance=balance-? where acc_no=?";
        double balance=getBalance(con,acc_no);
        if(amt>0 && balance>=amt){
            PreparedStatement ps=con.prepareStatement(query);
            ps.setDouble(1,amt);
            ps.setInt(2,acc_no);
            ps.executeUpdate();
            return true;
        }else{
            System.out.println("Insufficient Balance");
            return false;
        }
    }
    public static int getUpiFromBank(Connection con,int acc_no) throws SQLException {
        String query="SELECT upi_id from upi where acc_no=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,acc_no);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            return rs.getInt("upi_id");
        }
        return -1;
    }
    public static int getBankFromUpi(Connection con,int upi_id) throws SQLException {
        String query="SELECT acc_no from upi where upi_id=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,upi_id);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            return rs.getInt("acc_no");
        }
        return -1;
    }
}
