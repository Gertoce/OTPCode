package com.otp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailService() {
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        System.out.println("DEBUG: Загружен логин из файла: " + this.username);

        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            // Загружаем файл из папки resources
            props.load(getClass().getClassLoader().getResourceAsStream("email.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить email.properties", e);
        }
    }

    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Защитный код");
            message.setText("Ваш проверочный код: " + code);

            Transport.send(message);
            System.out.println("Письмо успешно отправлено!");
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка при отправке почты", e);
        }
    }
}
