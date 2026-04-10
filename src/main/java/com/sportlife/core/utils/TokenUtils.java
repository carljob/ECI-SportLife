package com.sportlife.core.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Utilidad de generación de tokens de sesión (simulación de JWT para MVP).
 *
 * Formato: Base64( email + ":" + epochMillis )
 *
 * NOTA: En producción reemplazar por JJWT con firma HS256 y expiración configurable.
 * Dependencia: io.jsonwebtoken:jjwt-api:0.11.5
 */
public final class TokenUtils {

    private static final String SEPARATOR = ":";

    private TokenUtils() {}

    /**
     * Genera un token de sesión para el email dado.
     *
     * @param email correo del usuario autenticado
     * @return token Base64 que identifica la sesión
     */
    public static String issueToken(String email) {
        String payload = email + SEPARATOR + Instant.now().toEpochMilli();
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extrae el email del token (para futuro uso en filtros de seguridad).
     *
     * @param token token generado por {@link #issueToken}
     * @return email contenido en el token
     */
    public static String extractEmail(String token) {
        String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        return decoded.split(SEPARATOR)[0];
    }
}
