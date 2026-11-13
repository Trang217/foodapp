package com.xinchao.FoodApp.auth_users.services;

import com.xinchao.FoodApp.auth_users.dtos.UserDTO;
import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.response.Response;

import java.util.List;

public interface UserService {
    User getCurrentLogInUser();// using the token to validate user
    Response<List<UserDTO>> getAllUsers();
    Response<UserDTO> getOwnAccountDetails();
    Response<?> updateOwnAccount(UserDTO userDTO);
    Response<?> deactivateOwnAccount();
}
