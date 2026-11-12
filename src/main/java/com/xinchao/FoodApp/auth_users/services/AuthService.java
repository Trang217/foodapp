package com.xinchao.FoodApp.auth_users.services;

import com.xinchao.FoodApp.auth_users.dtos.LoginRequest;
import com.xinchao.FoodApp.auth_users.dtos.RegistrationRequest;
import com.xinchao.FoodApp.response.Response;

public interface AuthService {
    Response<?> register(RegistrationRequest registrationRequest);

    Response<?> login(LoginRequest loginRequest);
}

