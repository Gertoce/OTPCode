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
    public void saveCodeToFile(String login, String code) {
        // Используем FileWriter в режиме "append" (true), чтобы новые коды не стирали старые
        try (java.io.FileWriter writer = new java.io.FileWriter("otp_codes.txt", true)) {
            String entry = String.format("[%s] Юзер: %s | Код: %s%n",
                    java.time.LocalDateTime.now(), login, code);
            writer.write(entry);
            System.out.println("LOG: Код сохранен в файл otp_codes.txt");
        } catch (java.io.IOException e) {
            System.err.println("ОШИБКА записи в файл: " + e.getMessage());
        }
    }
}