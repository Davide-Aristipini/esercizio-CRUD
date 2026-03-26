package com.colloquio.usermanagement.mapper;

import com.colloquio.usermanagement.dto.UserRequest;
import com.colloquio.usermanagement.dto.UserResponse;
import com.colloquio.usermanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        User user = new User();
        updateEntity(user, request);
        return user;
    }

    public void updateEntity(User user, UserRequest request) {
        user.setNome(request.nome().trim());
        user.setCognome(request.cognome().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setIndirizzo(request.indirizzo().trim());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getIndirizzo()
        );
    }
}
