package com.sportlife.core.services;

import com.sportlife.core.models.User;
import com.sportlife.core.utils.PasswordUtils;
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
        // Regla de negocio: email único en registro.
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new BusinessException("El email ya esta registrado");
        });

        // Regla de negocio: contraseña mínima para creación de cuenta.
        if (password == null || password.trim().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }

        UserEntity entity = new UserEntity();
        entity.setFullName(fullName);
        entity.setEmail(email.toLowerCase().trim());
        entity.setPassword(PasswordUtils.hash(password)); // nunca texto plano
        return UserPersistenceMapper.toModel(userRepository.save(entity));
    }

    @Override
    public String login(String email, String password) {
        // Mensaje genérico para no revelar si falló email o password.
        UserEntity user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!PasswordUtils.matches(password, user.getPassword())) {
            throw new BusinessException("Credenciales invalidas");
        }

        return TokenUtils.issueToken(user.getEmail());
    }
}
