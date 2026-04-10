package com.sportlife.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utilidad para hashear y verificar contraseñas usando SHA-256.
 *
 * NOTA MVP: Para producción se recomienda BCrypt (spring-security-crypto)
 * que incluye salt automático y es resistente a ataques de fuerza bruta.
 * SHA-256 se usa aquí para evitar añadir dependencias extra al MVP.
 */
public final class PasswordUtils {

    private PasswordUtils() {}

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }
}
