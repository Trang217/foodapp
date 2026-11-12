package com.xinchao.FoodApp.auth_users.services;

import com.xinchao.FoodApp.auth_users.dtos.LoginRequest;
import com.xinchao.FoodApp.auth_users.dtos.LoginResponse;
import com.xinchao.FoodApp.auth_users.dtos.RegistrationRequest;
import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.auth_users.repository.UserRepository;
import com.xinchao.FoodApp.exceptions.BadRequestException;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.response.Response;
import com.xinchao.FoodApp.role.entity.Role;
import com.xinchao.FoodApp.role.repository.RoleRepository;
import com.xinchao.FoodApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    @Override
    public Response<Void> register(RegistrationRequest registrationRequest) {
        log.info("inside register()");
        // if user is already exist
        if(userRepository.existsByEmail(registrationRequest.getEmail())){
            throw new BadRequestException("Email is already registered!");
        }
        // collect all role from request
        List<Role> userRoles;

        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role with name" + roleName + "can not found")))
                    .toList();
        } else {
            // if no role provided, default to customer
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("Default CUSTOMER role not found"));

            userRoles= List.of(defaultRole);
        }

        // build the user object
        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .address(registrationRequest.getAddress())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
         // save the user
        userRepository.save(userToSave);

        log.info("user registered successfully");

        return Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User is registered successfully")
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        log.info("inside login()");
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("user is not found"));

        if(!user.isActive()){
            throw new NotFoundException("user is inactive, please contact customer support");
        }
        // verify password
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid password");
        }

        // Generate token
        String token = jwtUtils.generateToken(user.getEmail());

        // Extract role name as a list
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roles);

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User is login successfully")
                .data(loginResponse)
                .build();
    }
}
