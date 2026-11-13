package com.xinchao.FoodApp.auth_users.services;

import com.xinchao.FoodApp.auth_users.dtos.UserDTO;
import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.auth_users.repository.UserRepository;
import com.xinchao.FoodApp.aws.AWSS3Service;
import com.xinchao.FoodApp.email_notification.service.NotificationService;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;

    @Override
    public User getCurrentLogInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("no user found"));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        log.info("inside getAllUsers()");
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDtos = modelMapper.map(users, new TypeToken<List<UserDTO>>(){}.getType());
        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("all users retrieved ok")
                .data(userDtos)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {
        log.info("inside getOwnAccountDetails()");
        User user = getCurrentLogInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("user dto retrieved ok")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {
        log.info("inside updateOwnAccount()");
        // fetch current login user
        User user = getCurrentLogInUser();
        String profileUrl = user.getProfileUrl();
        MultipartFile imageFile = userDTO.getImageFile();
        // check if new imagefile was provided
        if(imageFile != null && !imageFile.isEmpty()){
            // delete old image in cloud if it exists
            if(profileUrl != null && !profileUrl.isEmpty()){
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("profile/" + keyName);
                log.info("delete old profile image from s3");
            }
            // upload new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.loadFile("profile/" + imageName, imageFile);
            user.setProfileUrl(newImageUrl.toString());
        }

        return Response.<?>builder()
                .statusCode(HttpStatus.OK.value())
                .message("user updated ok")
                .build();
    }

    @Override
    public Response<?> deactivateOwnAccount() {
        return null;
    }
}
