package com.otp.dao;

import java.sql.*;

public class UserDAO {

    // Метод для сохранения нового пользователя
    public void saveUser(String login, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, role);

            pstmt.executeUpdate();
        }
    }

    // Метод для проверки: есть ли уже в базе администратор?
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
    public static int getUserIdByLogin(String login) throws SQLException {
        String sql = "SELECT id FROM users WHERE login = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1; // Если пользователь не найден
    }
}
