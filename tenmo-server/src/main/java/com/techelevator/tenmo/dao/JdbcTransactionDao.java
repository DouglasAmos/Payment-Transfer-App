package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcTransactionDao implements TransactionDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public JdbcTransactionDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Transaction getTransactionById(int id){
        Transaction transaction = null;
        String sql = "SELECT transaction_id, sender_id, receiver_id, balance\n" +
                "FROM transaction\n" +
                "WHERE transaction_id = ?;";

            SqlRowSet idForSearch = jdbcTemplate.queryForRowSet(sql, id);
                if(idForSearch.next()) {
                    transaction = mapRowToUser(idForSearch);
                }
            return transaction;
    }

    //make method for request status
    public void updateDatabase(int senderId, int receiverId, BigDecimal balance, String status){
        if(status.contains("pending")){
            status = "pending";
        }
        String sql = "INSERT INTO transaction (sender_id, receiver_id, balance, transfer_status)\n" +
                    "VALUES (?,?,?,?);";
            jdbcTemplate.update(sql, senderId, receiverId, balance, status );
    }

    //make method to send request and have approve/deny
    public String transferStatus(){
        String status = "";




        return status;
    }


    //view all requests and approve/deny

    public Transaction mapRowToUser(SqlRowSet rs){
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setSenderId(rs.getInt("sender_id"));
        transaction.setReceiverId(rs.getInt("receiver_id"));
        transaction.setBalance(rs.getBigDecimal("balance"));
        return transaction;
    }


}
