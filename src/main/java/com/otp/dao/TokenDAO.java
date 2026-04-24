package com.otp.dao;

import java.sql.*;
import java.util.UUID;
import java.time.LocalDateTime;

public class TokenDAO {

    public String createToken(int userId) throws SQLException {
        // Генерируем случайную уникальную строку
        String token = UUID.randomUUID().toString();
        // Токен живет, например, 1 час
        Timestamp expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(1));

        String sql = "INSERT INTO user_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, expiresAt);
            pstmt.executeUpdate();
        }
        return token;
    }
    public String getRoleByToken(String token) throws SQLException {
        String sql = "SELECT u.role FROM users u JOIN user_tokens t ON u.id = t.user_id " +
                "WHERE t.token = ? AND t.expires_at > NOW()";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }
        }
        return null; // Токен невалиден или просрочен
    }
}
