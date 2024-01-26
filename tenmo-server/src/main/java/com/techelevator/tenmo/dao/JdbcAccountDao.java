package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Component
public class JdbcAccountDao implements AccountDao {

    private JdbcTemplate jdbcTemplate;


    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");


    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalance(int userId){
        BigDecimal balance = STARTING_BALANCE;
        //if transactions have occured, deduct from balance
        String sql = "SELECT balance\n" +
                "FROM account\n" +
                "WHERE user_id = ?;";

        balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
        //user.setBalance(balance);
        return balance;
    }

    @Override
    public String getTransferStatusByTransactionId(int transactionId){
        String currentStatus = "";
        String sql = "SELECT transfer_status\n" +
                "FROM transaction\n" +
                "WHERE transaction_id = ?;";
        SqlRowSet currentStat = jdbcTemplate.queryForRowSet(sql, transactionId);
        while(currentStat.next()){
            Transaction transaction = new Transaction();
            currentStatus = currentStat.getString("transfer_status");
        }
        return currentStatus;
    }

    @Override
    public void transferApproved(int transactionId){
        String transactionSql = "UPDATE transaction\n" +
                "SET transfer_status = '*Approved*'\n" +
                "WHERE transaction_id = ?;";
        jdbcTemplate.update(transactionSql, transactionId);
    }

   @Override
    public String transfer(int senderId, int receiverId, BigDecimal amountToSend, String status){
        String statusResponse = "";
    if(receiverId != senderId) {
        BigDecimal value = new BigDecimal("0.00");
        if (amountToSend.compareTo(value) >= 0) {
            if (getBalance(senderId).compareTo(amountToSend) >= 0) {

                if(status == "*Approved*") {

                    BigDecimal subtractAmount = getBalance(senderId).subtract(amountToSend);

                    String sendSql = "UPDATE account\n" +
                            "SET balance = ?\n" +
                            "WHERE user_id = ?;";
                    jdbcTemplate.update(sendSql, subtractAmount, senderId);

                    BigDecimal addAmount = getBalance(receiverId).add(amountToSend);

                    String receiveSql = "UPDATE account\n" +
                            "SET balance = ?\n" +
                            "WHERE user_id = ?;";
                    jdbcTemplate.update(receiveSql, addAmount, receiverId);
                    statusResponse = "*Approved*";


                } else if (status == "*Rejected*"){
                    String updateStatusSql = "INSERT INTO transaction (transfer_status)\n" +
                            "VALUES (?)" +
                            "WHERE (SELECT transaction_id\n" +
                            "FROM transaction\n" +
                            "WHERE sender_id = ? AND receiver_id = ? AND balance = ?;);";
                    SqlRowSet result = jdbcTemplate.queryForRowSet(updateStatusSql, "*Rejected*", senderId, receiverId, amountToSend);
                    while(result.next()){
                        mapRowToUser(result);
                        statusResponse = "*Rejected*";

                    }
                }
            }
            else return "Insufficient Funds";
        }
    }else {
        System.out.println("Please don't send money to yourself");
    }
    return statusResponse;

    }


    @Override
    public List<Transaction> transactionHistory(Long senderId, Long receiverId){
        List<Transaction> history = new ArrayList<>();
        String sql = "SELECT * \n" +
                "FROM transaction\n" +
                "WHERE sender_id = ? OR receiver_id = ?;\n";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, senderId, receiverId);
        while(results.next()){
            int transactionId = results.getInt("transaction_id");
            int sender = results.getInt("sender_id");
            int receiver = results.getInt("receiver_id");
            BigDecimal balance = results.getBigDecimal("balance");
            String transferStatus = results.getString("transfer_status");

            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionId);
            transaction.setSenderId(sender);
            transaction.setReceiverId(receiver);
            transaction.setBalance(balance);
            transaction.setTransferStatus(transferStatus);

            history.add(transaction);
        }
        return history;
    }


    @Override
    public Transaction transactionByTransactionId(int transactionId){
        Transaction transaction = null;
        String sql = "SELECT sender_id, receiver_id, balance, transfer_status\n" +
                "FROM transaction\n" +
                "WHERE transaction_id = ?;";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transactionId);
            while (result.next()) {
                transaction = new Transaction();
                transaction.setTransactionId(transactionId);
                transaction.setSenderId(result.getInt("sender_id"));
                transaction.setReceiverId(result.getInt("receiver_id"));
                transaction.setBalance(result.getBigDecimal("balance"));
                transaction.setTransferStatus("transfer_status");

            }
        } catch (Exception ex){

            System.out.println("Something went wrong");
        }
        return transaction;

    }



    public Transaction mapRowToUser(SqlRowSet rs){
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setSenderId(rs.getInt("sender_id"));
        transaction.setReceiverId(rs.getInt("receiver_id"));
        transaction.setBalance(rs.getBigDecimal("balance"));
        transaction.setTransferStatus("transfer_status");

        return transaction;
    }


}
