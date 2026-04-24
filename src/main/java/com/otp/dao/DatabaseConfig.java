package com.otp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Данные для подключения (проверь свой пароль!)
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1";

    public static Connection getConnection() throws SQLException {
        // Эта команда открывает "дверь" в базу данных
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
