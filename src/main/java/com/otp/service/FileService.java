package com.otp.service;

public class FileService {
    public void saveCodeToFile(String login, String code) {
        try (java.io.FileWriter writer = new java.io.FileWriter("otp_codes.txt", true)) {
            writer.write("Логин: " + login + " | Код: " + code + " | Время: " + java.time.LocalDateTime.now() + "\n");
        } catch (java.io.IOException e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }
    }
}
