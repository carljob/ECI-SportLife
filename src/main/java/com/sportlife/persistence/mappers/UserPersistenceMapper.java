package com.sportlife.persistence.mappers;

import com.sportlife.core.models.User;
import com.sportlife.persistence.entities.UserEntity;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toModel(UserEntity entity) {
        return User.builder()
            .id(entity.getId())
            .fullName(entity.getFullName())
            .email(entity.getEmail())
            .password(entity.getPassword())
            .build();
    }

    public static UserEntity toEntity(User model) {
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setFullName(model.getFullName());
        entity.setEmail(model.getEmail());
        entity.setPassword(model.getPassword());
        return entity;
    }
}

