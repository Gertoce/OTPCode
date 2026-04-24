package com.otp.dao;

import java.sql.*;
import java.time.LocalDateTime;

public class OtpDAO {
    public void saveOtp(int userId, String code, int ttlSeconds) throws SQLException {
        String sql = "INSERT INTO otp_operations (user_id, otp_code, status, expires_at) VALUES (?, ?, 'ACTIVE', ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, code);
            // Вычисляем время: СЕЙЧАС + количество секунд из настроек
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusSeconds(ttlSeconds)));

            pstmt.executeUpdate();
        }
    }
    public boolean validateOtp(int userId, String inputCode) throws SQLException {
        // Ищем последний АКТИВНЫЙ код для этого пользователя
        String sql = "SELECT id, otp_code, expires_at FROM otp_operations " +
                "WHERE user_id = ? AND status = 'ACTIVE' " +
                "ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String actualCode = rs.getString("otp_code");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    int operationId = rs.getInt("id");

                    // 1. Проверяем, совпадает ли код
                    if (actualCode.equals(inputCode)) {
                        // 2. Проверяем, не просрочен ли он
                        if (expiresAt.after(new Timestamp(System.currentTimeMillis()))) {
                            // Всё ок! Помечаем код как ИСПОЛЬЗОВАННЫЙ
                            updateStatus(operationId, "USED");
                            return true;
                        } else {
                            updateStatus(operationId, "EXPIRED");
                            System.out.println("Код просрочен!");
                        }
                    }
                }
            }
        }
        return false;
    }

    // Вспомогательный метод для обновления статуса
    private void updateStatus(int id, String newStatus) throws SQLException {
        String sql = "UPDATE otp_operations SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
}
