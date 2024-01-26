package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public interface AccountDao {

    BigDecimal getBalance(int userId);

    public void transferApproved(int transactionId);

    public String transfer(int senderId, int receiverId, BigDecimal amountToSend, String status);

    public List<Transaction> transactionHistory(Long senderId, Long receiverId);

   public Transaction transactionByTransactionId(int transactionId);

   public String getTransferStatusByTransactionId(int transactionId);

}
