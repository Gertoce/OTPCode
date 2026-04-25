package com.otp.service;

import com.otp.dao.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

public class OtpHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();
    private final OtpDAO otpDAO = new OtpDAO();
    private final OtpService otpService = new OtpService();
    private final ConfigDAO configDAO = new ConfigDAO();
    private final EmailService emailService = new EmailService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);

        System.out.println("LOG: Запрос на путь: " + path);

        try {
            // 1. РЕГИСТРАЦИЯ
            if (path.contains("/register")) {
                String login = params.getOrDefault("login", "");
                String role = params.getOrDefault("role", "USER").toUpperCase();

                if (login.isEmpty()) {
                    sendResponse(exchange, "ОШИБКА: Логин пуст", 400);
                    return;
                }
                userDAO.registerUser(login, "pass_hash", role);
                sendResponse(exchange, "УСПЕХ: Пользователь " + login + " зарегистрирован как " + role, 200);
            }

            // 2. ГЕНЕРАЦИЯ OTP
            else if (path.contains("/generate")) {
                String login = params.getOrDefault("login", "danil");
                int userId = userDAO.getUserIdByLogin(login);

                if (userId == -1) {
                    sendResponse(exchange, "ОШИБКА: Пользователь не найден", 404);
                    return;
                }

                String code = otpService.generateCode(configDAO.getCodeLength());
                otpDAO.saveOtp(userId, code, configDAO.getTtlSeconds());

                // Рассылка (Почта + Файл)
                emailService.sendCode("ваша_почта@yandex.ru", code); // Поставь свою почту
                otpService.saveCodeToFile(login, code);

                sendResponse(exchange, "КОД ОТПРАВЛЕН на почту и сохранен в файл!", 200);
            }

            // 3. ПРОВЕРКА OTP И ВЫДАЧА ТОКЕНА
            else if (path.contains("/verify")) {
                String inputCode = params.getOrDefault("code", "");
                String login = params.getOrDefault("login", "danil");
                int userId = userDAO.getUserIdByLogin(login);

                if (userId != -1 && otpDAO.validateOtp(userId, inputCode)) {
                    TokenDAO tokenDAO = new TokenDAO();
                    String token = tokenDAO.createToken(userId);
                    sendResponse(exchange, "АВТОРИЗАЦИЯ УСПЕШНА! Ваш токен: " + token, 200);
                } else {
                    sendResponse(exchange, "ОШИБКА: Неверный код или логин", 401);
                }
            }

            // 4. СПИСОК ПОЛЬЗОВАТЕЛЕЙ (ТОЛЬКО ДЛЯ АДМИНА)
            else if (path.contains("/admin/users")) {
                String token = params.getOrDefault("token", "");
                TokenDAO tokenDAO = new TokenDAO();
                String role = tokenDAO.getRoleByToken(token);

                if ("ADMIN".equalsIgnoreCase(role)) {
                    String users = userDAO.getAllNonAdminUsers();
                    sendResponse(exchange, users, 200);
                } else {
                    sendResponse(exchange, "ОШИБКА 403: Доступ запрещен", 403);
                }
            }

            // ЕСЛИ ПУТЬ НЕ НАЙДЕН
            else {
                sendResponse(exchange, "Используйте /register, /generate, /verify или /admin/users", 404);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "ВНУТРЕННЯЯ ОШИБКА: " + e.getMessage(), 500);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) params.put(entry[0], entry[1]);
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}