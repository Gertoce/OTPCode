package com.otp.dao;

import java.sql.*;

public class UserDAO {

    // ГЛАВНЫЙ МЕТОД: Регистрация с проверкой роли
    public void registerUser(String login, String passwordHash, String role) throws SQLException {
        // 1. Если пытаются зарегистрировать админа, проверяем, нет ли его уже в базе
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (isAdminExists()) {
                throw new SQLException("Ошибка: Администратор уже существует. Регистрация второго админа запрещена.");
            }
        }

        // 2. Если проверка пройдена (или это обычный USER), сохраняем
        String sql = "INSERT INTO users (login, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, role);

            pstmt.executeUpdate();
            System.out.println("LOG: Пользователь " + login + " успешно зарегистрирован как " + role);
        }
    }

    // Вспомогательный метод для проверки наличия админа
    public boolean isAdminExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int getUserIdByLogin(String login) throws SQLException {
        String sql = "SELECT id FROM users WHERE login = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }
    public String getAllNonAdminUsers() throws SQLException {
        StringBuilder sb = new StringBuilder("Список пользователей:\n");
        String sql = "SELECT id, login, role FROM users WHERE role != 'ADMIN'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id"))
                        .append(" | Логин: ").append(rs.getString("login"))
                        .append("\n");
            }
        }
        return sb.toString();
    }
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}