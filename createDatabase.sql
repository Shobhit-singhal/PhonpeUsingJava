drop database phonpe_final;
create database if not exists phonpe_final;
use phonpe_final;
-- 1. BANK ACCOUNT
CREATE TABLE bank_account (
    acc_no INT AUTO_INCREMENT PRIMARY KEY,
    bankname VARCHAR(100) NOT NULL,
    IFSC_Code VARCHAR(20) NOT NULL,
    balance DECIMAL(12, 2) default 0 NOT NULL,
    uid INT NOT NULL
);

-- 2. UPI
CREATE TABLE upi (
    upi_id int AUTO_INCREMENT PRIMARY KEY,
    acc_no BIGINT NOT NULL,
    FOREIGN KEY (acc_no) REFERENCES bank_account(acc_no)
);

-- 3. USER
CREATE TABLE user (
    uid INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address TEXT,
    default_upi_id VARCHAR(100),
    FOREIGN KEY (default_upi_id) REFERENCES upi(upi_id)
);

-- 4. USER PHONE NUMBERS
CREATE TABLE user_phoneno (
    uid INT NOT NULL,
    phone_no VARCHAR(15) NOT NULL,
    PRIMARY KEY (uid, phone_no),
    FOREIGN KEY (uid) REFERENCES user(uid)
);

-- 5. WALLET
CREATE TABLE wallet (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(12, 2) NOT NULL,
    uid INT UNIQUE NOT NULL,
    FOREIGN KEY (uid) REFERENCES user(uid)
);

-- 6. TRANSACTION
CREATE TABLE transaction (
    t_id INT AUTO_INCREMENT PRIMARY KEY,
    method VARCHAR(50) NOT NULL,
    time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type ENUM('load_wallet', 'send_money', 'withdraw_wallet') NOT NULL,
    sender INT NOT NULL,
    receiver INT NOT NULL,
    FOREIGN KEY (sender) REFERENCES user(uid),
    FOREIGN KEY (receiver) REFERENCES user(uid)
);

-- 7. USES_UPI
CREATE TABLE uses_upi (
    t_id INT NOT NULL,
    upi_id VARCHAR(100) NOT NULL,
    type ENUM('credit', 'debit') NOT NULL,
    PRIMARY KEY (t_id, upi_id),
    FOREIGN KEY (t_id) REFERENCES transaction(t_id),
    FOREIGN KEY (upi_id) REFERENCES upi(upi_id)
);

-- 8. USES_WALLET
CREATE TABLE uses_wallet (
    t_id INT NOT NULL,
    wallet_id INT NOT NULL,
    type ENUM('credit', 'debit') NOT NULL,
    PRIMARY KEY (t_id, wallet_id),
    FOREIGN KEY (t_id) REFERENCES transaction(t_id),
    FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id)
);

