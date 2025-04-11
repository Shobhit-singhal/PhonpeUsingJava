package service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class TransactionService {
    public static int chooseAcc(Connection con,int uid) throws SQLException {
        Scanner sc=new Scanner(System.in);
        ArrayList<Integer> accounts=BankService.getBankAccountByUid(con,uid);
        if (accounts.isEmpty()) {
            System.out.println("No bank accounts linked to this user.");
            return -1;
        }
        System.out.println("Available bank Accounts:");
        for(int i=0;i<accounts.size();i++){
            System.out.println((i+1)+". "+accounts.get(i));
        }
        System.out.print("Which account do you want to choose: ");
        int ch=sc.nextInt();
        return accounts.get(ch-1);
    }

    private static int addToTransaction(Connection con,String method, String status, double amt, String type, int sender, int receiver) throws SQLException {
        String query="INSERT INTO transaction (method,status,amount,type,sender,receiver) values(?,?,?,?,?,?)";
        PreparedStatement ps=con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,method);
        ps.setString(2,status);
        ps.setDouble(3,amt);
        ps.setString(4,type);
        ps.setInt(5,sender);
        ps.setInt(6,receiver);
        int count=ps.executeUpdate();
        if(count>0){
            ResultSet rs=ps.getGeneratedKeys();
            if(rs.next()){
                return rs.getInt(1);
            }
        }
        return -1;
    }
    private static void addToUsesUpi(Connection con,int t_id,int upi_id,String type) throws SQLException {
        String query="INSERT INTO uses_upi VALUES (?,?,?)";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,t_id);
        ps.setInt(2,upi_id);
        ps.setString(3,type);
        ps.executeUpdate();
    }
    private static void addToUsesWallet(Connection con,int t_id,int w_id,String type) throws SQLException {
        String query="INSERT INTO uses_wallet VALUES (?,?,?)";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,t_id);
        ps.setInt(2,w_id);
        ps.setString(3,type);
        ps.executeUpdate();
    }
    public static void loadWallet(Connection con,int uid ,double amt) throws SQLException {
        int acc_no=chooseAcc(con,uid);
        int w_id=WalletService.getWalletIdByUid(con,uid);
        if(acc_no!=-1 && w_id!=-1){
            boolean success=BankService.withdraw(con,acc_no,amt);
            WalletService.deposit(con,w_id,amt);
            if(success){
                int t_id=addToTransaction(con,"UPI","SUCCESS",amt,"load_wallet",uid,uid);
                if(t_id!=-1){
                    int upi_id=BankService.getUpiFromBank(con,acc_no);
                    addToUsesUpi(con,t_id,upi_id,"debit");
                    addToUsesWallet(con,t_id,w_id,"credit");
                    System.out.println("Transaction ID: " + t_id + " created for loading wallet.");
                    System.out.println("₹" + amt + " loaded successfully!");
                    System.out.println("Wallet Balance: ₹" + WalletService.getBalance(con, w_id));

                }
            }
        }
    }
    public static void sendMoneyUsingBank(Connection con,int sender, int receiver ,double amt) throws SQLException{
        int receiverUPI=UserService.getDefaultUPI(con,receiver);
        if(receiverUPI!=-1){
            int receiverAcc=BankService.getBankFromUpi(con,receiverUPI);
            int senderAcc=chooseAcc(con,sender);
            int senderUPI=BankService.getUpiFromBank(con,senderAcc);
            if(receiverAcc!=-1 && senderAcc !=-1 && amt>0){
                boolean success=BankService.withdraw(con,senderAcc,amt);
                if(success){
                    BankService.deposit(con,receiverAcc,amt);
                    int t_id=addToTransaction(con,"UPI","SUCCESS",amt,"send_money",sender,receiver);
                    if(t_id!=-1){
                        addToUsesUpi(con,t_id,senderUPI,"debit");
                        addToUsesUpi(con,t_id,receiverUPI,"credit");
                        System.out.println("₹" + amt + " sent successfully to UID " + receiver);
                        System.out.println("Transaction ID: " + t_id);
                    }
                }
                else{
                    System.out.println("couldn't deduct");
                }
            }else{
                System.out.println("Invalid sender or receiver");
            }
        }else{
            System.out.println("Receiver doesn't have a default upi set up");
        }
    }
    public static void sendMoneyUsingWallet(Connection con,int sender, int receiver ,double amt) throws SQLException{
        int receiverUPI=UserService.getDefaultUPI(con,receiver);
        if(receiverUPI!=-1){
            int receiverAcc=BankService.getBankFromUpi(con,receiverUPI);
            int senderWallet=WalletService.getWalletIdByUid(con,sender);
            if(receiverAcc!=-1 && senderWallet !=-1 && amt>0){
                boolean success=WalletService.withdraw(con,senderWallet,amt);
                if(success){
                    BankService.deposit(con,receiverAcc,amt);
                    int t_id=addToTransaction(con,"WALLET","SUCCESS",amt,"send_money",sender,receiver);
                    if(t_id!=-1){
                        addToUsesWallet(con,t_id,senderWallet,"debit");
                        addToUsesUpi(con,t_id,receiverUPI,"credit");
                        System.out.println("₹" + amt + " sent successfully to UID " + receiver);
                        System.out.println("Transaction ID: " + t_id);
                    }
                }
            }
        }else{
            System.out.println("Receiver doesn't have a default upi set up");
        }
    }
    public static void transferMoney(Connection con,int sender,int receiver,double amt) throws SQLException {
        Scanner sc=new Scanner(System.in);
        System.out.println("Do you want to use wallet or upi (1.Wallet 2.UPI)");
        int choice=sc.nextInt();
        if(choice==1){
            sendMoneyUsingWallet(con,sender,receiver,amt);
        } else if (choice==2) {
            sendMoneyUsingBank(con,sender,receiver,amt);
        }else{
            System.out.println("Invalid choice");
        }
    }
    public static void viewTransaction(Connection con,int uid) throws SQLException{
        String query="SELECT * FROM transaction where sender = ? or receiver=? order by time DESC";
        PreparedStatement ps=con.prepareStatement(query);
        ps.setInt(1,uid);
        ps.setInt(2,uid);
        ResultSet rs=ps.executeQuery();
        while (rs.next()){
            int tid = rs.getInt("t_id");
            String type = rs.getString("type");
            String method = rs.getString("method");
            String status = rs.getString("status");
            double amt = rs.getDouble("amount");
            Timestamp time = rs.getTimestamp("time");
            int sender = rs.getInt("sender");
            int receiver = rs.getInt("receiver");

            String direction = (sender == uid)
                    ? "sent to UID " + receiver
                    : "received from UID " + sender;
            System.out.println("----- Your Recent Transactions -----");
            System.out.printf("[TXN#%d] ₹%.2f %s | Method: %s | Status: %s | Date: %s\n",
                    tid, amt, direction, method, status, time.toString());
        }
    }


}
