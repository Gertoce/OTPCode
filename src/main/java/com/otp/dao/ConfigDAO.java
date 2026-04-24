package com.otp.dao;

import java.sql.*;

public class ConfigDAO {
    public int getCodeLength() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT code_length FROM otp_config LIMIT 1")) {
            if (rs.next()) return rs.getInt("code_length");
        }
        return 6; // если что-то пошло не так, вернем стандартные 6 цифр
    }

    public int getTtlSeconds() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ttl_seconds FROM otp_config LIMIT 1")) {
            if (rs.next()) return rs.getInt("ttl_seconds");
        }
        return 300; // стандарт — 5 минут
    }
}
