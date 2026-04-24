package com.otp.service;

import com.otp.dao.TokenDAO;
import com.otp.dao.UserDAO;
import com.otp.dao.OtpDAO;
import com.otp.dao.ConfigDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class OtpHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();
    private final OtpDAO otpDAO = new OtpDAO();
    private final OtpService otpService = new OtpService();
    private final ConfigDAO configDAO = new ConfigDAO();
    private final EmailService emailService = new EmailService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        String path = exchange.getRequestURI().getPath(); // Получаем путь (например, /api/otp/verify)
        String query = exchange.getRequestURI().getQuery();
        String response = "";
        int statusCode = 200;

        try {
            // ПУТЬ 1: ГЕНЕРАЦИЯ ( http://localhost:8080/api/otp/generate?login=danil&role=admin )
            if (path.contains("/register")) {
                java.util.Map<String, String> params = parseQuery(query);
                String login = params.getOrDefault("login", "");
                String role = params.getOrDefault("role", "USER").toUpperCase();

                try {
                    userDAO.registerUser(login, "password_placeholder", role);
                    sendResponse(exchange, "УСПЕХ: Пользователь " + login + " зарегистрирован.", 200);
                } catch (SQLException e) {
                    sendResponse(exchange, "ОШИБКА: " + e.getMessage(), 400);
                }
            }
            // ПУТЬ 2: ПРОВЕРКА ( http://localhost:8080/api/otp/verify?code=123456 )
            else if (path.endsWith("/verify")) {
                // 1. Извлекаем параметры из query (нужны и code, и login)
                java.util.Map<String, String> params = parseQuery(query);
                String inputCode = params.getOrDefault("code", "");
                String login = params.getOrDefault("login", "danil"); // Берем логин из запроса или хардкодим для теста

                // 2. Ищем userId в базе по этому логину (РЕШАЕМ ОШИБКУ ТУТ)
                int userId = userDAO.getUserIdByLogin(login);

                if (userId != -1 && otpDAO.validateOtp(userId, inputCode)) {
                    TokenDAO tokenDAO = new TokenDAO();
                    String token = tokenDAO.createToken(userId);

                    response = "АВТОРИЗАЦИЯ УСПЕШНА! Ваш токен доступа: " + token;
                    System.out.println("LOG: Выдан токен для пользователя: " + login);
                } else {
                    statusCode = 401;
                    response = "ОШИБКА: Неверный код, логин или время истекло.";
                }
            }
            else if (path.endsWith("/admin/users")) {
                java.util.Map<String, String> params = parseQuery(query);
                String token = params.getOrDefault("token", "");

                TokenDAO tokenDAO = new TokenDAO();
                String role = tokenDAO.getRoleByToken(token);

                // ПРОВЕРКА РОЛИ (Пункт ТЗ: API Администратора)
                if ("ADMIN".equalsIgnoreCase(role)) {
                    response = userDAO.getAllNonAdminUsers();
                    System.out.println("LOG: Админ просмотрел список пользователей.");
                } else {
                    statusCode = 403;
                    response = "ОШИБКА 403: Доступ только для администраторов (нужен валидный токен).";
                    System.out.println("LOG: Попытка несанкционированного доступа к списку пользователей!");
                }
            }
            else {
                response = "Используйте /generate или /verify";
            }
        } catch (Exception e) {
            statusCode = 500;
            response = "Ошибка: " + e.getMessage();
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        if (path.endsWith("/register")) {
            // Параметры из ссылки: ?login=petya&pass=123&role=USER
            // В реальном проекте параметры берутся через парсинг query
            String login = "petya"; // Вытяни из query
            String pass = "hash_тут"; // В идеале прогнать через BCrypt
            String role = "ADMIN"; // Например, пытаемся создать второго админа

            try {
                userDAO.registerUser(login, pass, role);
                response = "Пользователь " + login + " зарегистрирован!";
            } catch (SQLException e) {
                statusCode = 400; // Ошибка клиента
                response = e.getMessage(); // Вернет "Администратор уже существует..."
            }
        }
        // Внутри метода handle класса OtpHandler
        if (path.endsWith("/register")) {
            // Разбиваем query string (например: login=danil&role=ADMIN)
            java.util.Map<String, String> params = new java.util.HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] entry = param.split("=");
                    if (entry.length > 1) params.put(entry[0], entry[1]);
                }
            }

            String login = params.getOrDefault("login", "guest");
            String role = params.getOrDefault("role", "USER");
            String passHash = "dummy_hash"; // В реале тут будет BCrypt.hashpw(...)

            try {
                userDAO.registerUser(login, passHash, role);
                response = "УСПЕХ: Пользователь " + login + " сохранен как " + role;
            } catch (SQLException e) {
                statusCode = 400; // Bad Request
                response = "ОШИБКА РЕГИСТРАЦИИ: " + e.getMessage();
                System.out.println("LOG: Ошибка при регистрации: " + e.getMessage());
            }
        }
    }
    private java.util.Map<String, String> parseQuery(String query) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    params.put(entry[0], entry[1]);
                }
            }
        }
        return params;
    }
    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        // Устанавливаем кодировку UTF-8
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (java.io.OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
