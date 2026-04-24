import com.otp.dao.ConfigDAO;
import com.otp.dao.OtpDAO;
import com.otp.dao.UserDAO;
import com.otp.service.OtpService;
import java.util.Scanner;

import java.sql.SQLException;

import com.otp.service.EmailService;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        OtpDAO otpDAO = new OtpDAO();
        OtpService otpService = new OtpService();
        ConfigDAO configDAO = new ConfigDAO();
        EmailService emailService = new EmailService(); // Наш новый почтальон

        try {

            String login = "danil";
            int userId = userDAO.getUserIdByLogin(login);
            String myRealEmail = "gertocelol@yandex.ru";

// 1. ГЕНЕРАЦИЯ
            String generatedCode = otpService.generateCode(configDAO.getCodeLength());
            otpDAO.saveOtp(userId, generatedCode, configDAO.getTtlSeconds());

// 2. ОТПРАВКА
            emailService.sendCode(myRealEmail, generatedCode);
            System.out.println("Письмо улетело! Жду код из почты...");

// 3. ВВОД ИЗ КОНСОЛИ (Остановка программы)
            System.out.print("Введите код подтверждения: ");
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine(); // Программа замрет и будет ждать тебя!

// 4. ПРОВЕРКА
            if (otpDAO.validateOtp(userId, userInput)) {
                System.out.println("Код верный. Доступ разрешен.");
            } else {
                System.out.println("ОШИБКА: Код неверный, использован или просрочен.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}