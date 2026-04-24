package com.otp.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AuthService {

    // Этот метод превращает любой пароль в уникальную "абракадабру"
    public String hashPassword(String password) {
        try {
            // Используем алгоритм SHA-256 (золотой стандарт)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());

            // Превращаем байты в читаемую строку
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка: алгоритм шифрования не найден!");
        }
    }

    // Метод для сравнения: введенный пароль совпадает с тем, что в базе?
    public boolean checkPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }
}
