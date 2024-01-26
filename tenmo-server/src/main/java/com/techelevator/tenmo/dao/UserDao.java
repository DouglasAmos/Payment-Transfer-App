package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll();
    List<String> listAllUserNames();

    User findByUsername(String username);

    //can find id by token
    int findIdByUsername(String username);

    boolean create(String username, String password);

}
