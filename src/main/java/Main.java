import com.otp.dao.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("Проверка связи с базой данных...");

        // Пробуем подключиться и выполнить простой запрос
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Спрашиваем у базы длину кода из таблицы настроек
            ResultSet rs = stmt.executeQuery("SELECT code_length FROM otp_config");

            if (rs.next()) {
                int length = rs.getInt("code_length");
                System.out.println("Успех! База ответила. Длина кода в настройках: " + length);
            }

        } catch (Exception e) {
            System.out.println("Ошибка подключения! Проверь пароль или запущена ли база.");
            e.printStackTrace();
        }
    }
}