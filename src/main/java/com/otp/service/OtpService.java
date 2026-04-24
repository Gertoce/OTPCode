package com.otp.service;

import java.security.SecureRandom;
import java.util.Random;

public class OtpService {
    // SecureRandom — это "улучшенный" генератор случайных чисел для безопасности
    private final Random random = new SecureRandom();

    // Метод, который создает случайную строку из цифр нужной длины
    public String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // Добавляем случайную цифру от 0 до 9
        }
        return code.toString();
    }
}