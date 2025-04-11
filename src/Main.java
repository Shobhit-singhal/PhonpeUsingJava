import db.ConnectionManager;
import service.BankService;
import service.TransactionService;
import service.UserService;
import service.WalletService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    private static Connection con=null;
    public static void main(String[] args) throws SQLException {

        try{
            con= ConnectionManager.getConnection();
            System.out.println("Connected Successfully");
            con.setAutoCommit(false);
            Scanner sc=new Scanner(System.in);
            int uid=-1;
            boolean cont=true;
            int ch=printMainMenu(sc);
            switch (ch){
                case 1->{
                    System.out.print("Enter your uid: ");
                    int temp=sc.nextInt();
                    if(UserService.exists(con,temp)){
                        uid=temp;
                        System.out.println("Logged in as UID: " + uid);
                    }else{
                        System.out.println("Invalid user");
                    }
                }
                case 2->{
                    uid=UserService.createUser(con);
                    System.out.println("UID: "+uid);
                    con.commit();
                }
                case 3->{
                    System.out.println("BYE!");
                    System.exit(0);
                }
            }


            while(cont){
                int choice= printUserMenu(sc);
                switch (choice){
                    case 1->{
                        boolean cont1=true;
                        while(cont1){
                            int choice1=printUserOpt(sc);
                            switch (choice1){
                                case  1->{
                                    System.out.print("Enter the upi id: ");
                                    int upi_id=sc.nextInt();
                                    UserService.addDefaultAcc(con,uid,upi_id);
                                    con.commit();
                                }
                                case 2->{
                                    cont1=false;
                                }
                            }
                        }
                    }
                    case 2->{
                        boolean cont1=true;
                        while(cont1){
                            int acc_no=TransactionService.chooseAcc(con,uid);
                            int choice1=printBankMenu(sc);
                            switch (choice1) {
                                case 1 -> {
                                    BankService.createBankAccount(con, uid);
                                    con.commit();
                                }
                                case 2 -> {
                                    System.out.print("Enter the amount to deposit: ");
                                    double amt = sc.nextDouble();
                                    BankService.deposit(con, acc_no, amt);
                                    con.commit();
                                }
                                case 3 -> {
                                    System.out.print("Enter the amount to withdraw: ");
                                    double amt = sc.nextDouble();
                                    BankService.withdraw(con, acc_no, amt);
                                    con.commit();
                                }
                                case 4 -> {
                                    double balance = BankService.getBalance(con, acc_no);
                                    System.out.println(balance);
                                }
                                case 5-> {
                                    cont1=false;
                                }
                            }
                        }

                    }
                    case 3->{
                        boolean cont1=true;
                        while (cont1){
                            int choice1;
                            int w_id=WalletService.getWalletIdByUid(con,uid);
                            choice1=printWalletMenu(sc);
                            switch (choice1){
                                case 1->{
                                    System.out.print("Enter the amount to deposit: ");
                                    double amt=sc.nextDouble();
                                    WalletService.deposit(con,w_id,amt);
                                    con.commit();
                                }
                                case 2->{
                                    System.out.print("Enter the amount to withdraw: ");
                                    double amt=sc.nextDouble();
                                    WalletService.withdraw(con,w_id,amt);
                                    con.commit();
                                }
                                case 3->{
                                    double balance=WalletService.getBalance(con,w_id);
                                    System.out.println(balance);
                                }
                                case 4->{
                                    cont1=false;
                                }
                            }
                        }

                    }
                    case 4->{
                        boolean cont1=true;
                        while (cont1) {
                            int choice1=printTransactionMenu(sc);
                            switch (choice1){
                                case 1->{
                                    System.out.print("Enter the amount: ");
                                    double amt=sc.nextDouble();
                                    TransactionService.loadWallet(con,uid,amt);
                                    con.commit();
                                }
                                case 2->{
                                    System.out.print("Enter the receiver uid: ");
                                    int receiver=sc.nextInt();
                                    System.out.print("Enter the amount: ");
                                    double amt=sc.nextDouble();
                                    TransactionService.transferMoney(con,uid,receiver,amt);
                                    con.commit();
                                }
                                case 3->{
                                    TransactionService.viewTransaction(con,uid);
                                }
                                case 4->{
                                    cont1=false;
                                }
                            }
                        }

                    }
                    case 9-> {
                        System.out.println("BYE!");
                        uid=-1;
                        cont=false;
                    }
                }
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
            con.rollback();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static int printMainMenu(Scanner sc){

        System.out.println("Main Menu:");
        System.out.println("1. Login");
        System.out.println("2. Create user");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
        return sc.nextInt();
    }
    public static int printUserMenu(Scanner sc){

        System.out.println("Menu:");
        System.out.println("1.user");
        System.out.println("2.Bank");
        System.out.println("3.Wallet");
        System.out.println("4.Transaction");
        System.out.println("9.Exit");
        return sc.nextInt();
    }
    public static int printUserOpt(Scanner sc){

        System.out.println("Menu");
        System.out.println("1. Set default upi");
        System.out.println("2. Back");
        return sc.nextInt();
    }
    public static int printBankMenu(Scanner sc){
        System.out.println("Menu");
        System.out.println("1. Create BankAccount");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Check Balance");
        System.out.println("5. Back");
        return sc.nextInt();
    }
    public static int printWalletMenu(Scanner sc){
        System.out.println("Menu");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Check Balance");
        System.out.println("4. Back");
        return sc.nextInt();
    }
    public static int printTransactionMenu(Scanner sc){
        System.out.println("Menu");
        System.out.println("1. Load Wallet");
        System.out.println("2. Fund Transfer");
        System.out.println("3. View Transactions");
        System.out.println("4. Back");
        return sc.nextInt();
    }

}