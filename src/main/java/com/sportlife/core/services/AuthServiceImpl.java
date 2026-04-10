package com.sportlife.core.services;

import com.sportlife.core.models.User;
import com.sportlife.core.utils.TokenUtils;
import com.sportlife.handlers.BusinessException;
import com.sportlife.persistence.entities.UserEntity;
import com.sportlife.persistence.mappers.UserPersistenceMapper;
import com.sportlife.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User register(String fullName, String email, String password) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BusinessException("El email ya esta registrado");
        });

        UserEntity entity = new UserEntity();
        entity.setFullName(fullName);
        entity.setEmail(email);
        entity.setPassword(password);
        return UserPersistenceMapper.toModel(userRepository.save(entity));
    }

    @Override
    public String login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!user.getPassword().equals(password)) {
            throw new BusinessException("Credenciales invalidas");
        }

        return TokenUtils.issueToken(user.getEmail());
    }
}

