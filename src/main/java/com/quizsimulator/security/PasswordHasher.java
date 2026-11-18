package com.quizsimulator.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {

    private static final int ITERATIONS = 600_000;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {

        validate(password);

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] passwordHash = derive(password, salt, ITERATIONS);

        return "pbkdf2-sha256$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(passwordHash);
    }

    public static boolean verify(String password, String encoded) {

        if (password == null
                || password.length() > 1024
                || encoded == null) {
            return false;
        }

        try {
            String[] parts = encoded.split("\\$", -1);

            if (parts.length != 4
                    || !parts[0].equals("pbkdf2-sha256")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations < ITERATIONS || iterations > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length != 16 || expectedHash.length != 32) {
                return false;
            }

            byte[] actualHash = derive(password, salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void validate(String password) {

        if (password == null
                || password.isBlank()
                || password.length() < 8
                || password.length() > 1024) {

            throw new IllegalArgumentException(
                    "Password must contain 8 to 1024 characters."
            );
        }
    }

    private static byte[] derive(
            String password,
            byte[] salt,
            int iterations
    ) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                256
        );

        try {
            return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

        } catch (GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Password hashing is unavailable.",
                    exception
            );

        } finally {
            spec.clearPassword();
        }
    }
}