package com.sportlife.core.services;

import com.sportlife.core.models.User;

public interface AuthService {
    User register(String fullName, String email, String password);
    String login(String email, String password);
}

