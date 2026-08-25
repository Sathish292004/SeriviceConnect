package com.serviceconnect.user.mapper;

import com.serviceconnect.user.dto.request.UserRequest;
import com.serviceconnect.user.dto.response.UserResponse;
import com.serviceconnect.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(Long userId, UserRequest request) {

        return User.builder()
                .id(userId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();
    }

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .build();
    }

    public void updateEntity(
            User user,
            UserRequest request) {

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
    }
}