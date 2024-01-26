package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AccountController {
    private User sender;
    private User receiver;

    private TokenProvider tokenProvider;
    private AuthenticationManagerBuilder authenticationManagerBuilder;


    @Autowired
    private UserDao userDao;

   @Autowired
    private AccountDao accountDao;

   @Autowired
   private JdbcTransactionDao transactionDao;

    /* public AccountController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
    }

     */

    public AccountController(){

    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/showUsers", method = RequestMethod.GET)
    public List<String> listUsers() {
        return userDao.listAllUserNames();
    }


 /*  @RequestMapping(path = "/transfer/{senderId}/{receiverId}/{amountToSend}", method = RequestMethod.PUT)
    public String transfer(@PathVariable int senderId,
                               @PathVariable int receiverId,
                               @PathVariable @Valid BigDecimal amountToSend,
                                String status){
        try {
            accountDao.transfer(senderId, receiverId, amountToSend, status);
        } catch (Exception e){
            System.out.println("unable to transfer");
        }
        String approved = "*Approved*";
        return approved;

}


  */


    @RequestMapping (path = "requestTransfer/{requesterId}/{requestedId}/{amountRequested}", method = RequestMethod.POST)
    public String requestTransfer(@PathVariable int requesterId, @PathVariable int requestedId, @PathVariable BigDecimal amountRequested){
        String status = "pending";

        transactionDao.updateDatabase(requesterId, requestedId, amountRequested, status);

        status = "*Transfer request: Approved*\n" +
                "Transfer Status: Pending";
        return status;
    }

    //path to approve or deny from requestee with transactionID
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @RequestMapping (path = "makeTransfer/{transactionId}/approve", method = RequestMethod.POST)
    public String approveTransfer(@PathVariable int transactionId, Principal principal){
        String ownUsername = principal.getName();
        User self = userDao.findByUsername(ownUsername);
        Long selfId = self.getId();

        String status = "*Approved*";
        Transaction approvedTransaction = new Transaction();

        if(approvedTransaction.getSenderId() == selfId) {
            String currentStatus = (accountDao.getTransferStatusByTransactionId(transactionId));
            if (currentStatus.equals("*Approved*")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to proceed with transfer, please try again");
            }
            //SQL to get info based on transfer_id
            approvedTransaction = accountDao.transactionByTransactionId(transactionId);
            if (approvedTransaction == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to find Transfer. Please Try Again");
            }
            approvedTransaction.setTransferStatus(status);
            accountDao.transfer(approvedTransaction.getSenderId(), approvedTransaction.getReceiverId(),
                    approvedTransaction.getBalance(), approvedTransaction.getTransferStatus());
            accountDao.transferApproved(transactionId);
        } else {
            return "You can only receive transfers that have been approved. Please try again.";
        }


        return status;

    }



@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/viewTransferHistory", method = RequestMethod.GET)
    public List<Transaction> transferHistoryBy(Principal principal) {
        String ownUsername = principal.getName();
        User self = userDao.findByUsername(ownUsername);
        Long ownId = self.getId();
        List<Transaction> transactionList = accountDao.transactionHistory(ownId, ownId);
        return transactionList;
}


//transfer status = null; need to fix
    @ResponseStatus(HttpStatus.BAD_REQUEST)
@PreAuthorize("isAuthenticated()")
    @RequestMapping(path = "/viewTransferHistory/{id}", method = RequestMethod.GET)
    public Transaction transferHistoryById(@PathVariable("id") int transferID, Principal principal) {
        Transaction transaction = transactionDao.getTransactionById(transferID);
        String ownUsername = principal.getName();
        User self = userDao.findByUsername(ownUsername);
        Long ownId = self.getId();
        if (ownId != transaction.getReceiverId() || ownId != transaction.getSenderId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only view your own transfers");
        }
        return transaction;
    }

  /*  public Long currentLoggedIn(Principal principal) {
        String ownUsername = principal.getName();
        User self = userDao.findByUsername(ownUsername);
        Long ownId = self.getId();
        return ownId;

   */






/*@RequestMapping (path = "/transfer" , method = RequestMethod.PUT)
    public void transfer(@RequestBody int senderID,
                         int recieverId,
                        BigDecimal amountToSend){

}

 */


}



