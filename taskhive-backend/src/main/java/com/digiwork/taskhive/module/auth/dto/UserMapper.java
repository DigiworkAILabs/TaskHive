package com.digiwork.taskhive.module.auth.dto;

import com.digiwork.taskhive.module.auth.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserInfoResponse toUserInfoResponse(User user, List<String> roles) {
        return UserInfoResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(roles.isEmpty() ? null : roles.get(0))
                .build();
    }
}
