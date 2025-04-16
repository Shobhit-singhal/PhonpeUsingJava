package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WalletService {
    public static void createWallet(Connection con, int uid) throws SQLException {
        String query="INSERT INTO wallet(uid) values(?)";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,uid);
        ps.executeUpdate();
    }
    public static int getWalletIdByUid(Connection con, int uid) throws SQLException {
        String query = "SELECT wallet_id FROM wallet WHERE uid=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, uid);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("wallet_id");
        }
        return -1;
    }
    public static double getBalance(Connection con,int w_id) throws SQLException {
        String query="SELECT balance FROM wallet where wallet_id=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,w_id);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            return rs.getDouble("balance");
        }
        return -1;
    }
    public static void deposit(Connection con,int w_id,double amt) throws SQLException {
        String query="UPDATE wallet set balance=balance+? where wallet_id=?";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setDouble(1,amt);
        ps.setInt(2,w_id);
        ps.executeUpdate();
    }
    public static boolean withdraw(Connection con,int w_id,double amt) throws SQLException {
        String query="UPDATE wallet set balance=balance-? where wallet_id=?";
        double balance=getBalance(con,w_id);
        if(amt>0 && balance>=amt){
            PreparedStatement ps=con.prepareStatement(query);
            ps.setDouble(1,amt);
            ps.setInt(2,w_id);
            ps.executeUpdate();
            return true;
        }else{
            System.out.println("Insufficient Balance");
            retu