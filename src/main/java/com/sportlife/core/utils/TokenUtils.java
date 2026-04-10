package com.sportlife.core.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String issueToken(String subject) {
        String payload = subject + ":" + Instant.now().toEpochMilli();
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}

