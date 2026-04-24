import com.otp.dao.UserDAO;
import com.otp.service.AuthService;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        AuthService authService = new AuthService(); // Наш новый шифровальщик

        try {
            // 1. Регистрируем админа
            String adminPassword = "boss_password";
            // Шифруем пароль ПЕРЕД сохранением
            String adminHash = authService.hashPassword(adminPassword);

            if (!userDAO.isAdminExists()) {
                userDAO.saveUser("admin", adminHash, "ADMIN");
                System.out.println("Админ зарегистрирован. Его хэш в базе: " + adminHash);
            }

            // 2. Регистрируем обычного пользователя
            String userPass = "my_password_123";
            String userHash = authService.hashPassword(userPass);

            userDAO.saveUser("danil", userHash, "USER");
            System.out.println("Пользователь danil зарегистрирован. Его хэш: " + userHash);

            // 3. ПРОВЕРКА: Как работает вход (Login)
            System.out.println("\n--- Проверка входа ---");
            String inputPass = "my_password_123"; // То, что ввел пользователь

            if (authService.checkPassword(inputPass, userHash)) {
                System.out.println("Доступ разрешен! Пароль подошел к хэшу.");
            } else {
                System.out.println("Ошибка! Пароль неверный.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}