package com.otp;

import com.otp.service.OtpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        // Создаем сервер на порту 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Назначаем "адрес" для нашего OTP сервиса
        server.createContext("/api/otp", new OtpHandler());

        server.setExecutor(null); // создаем стандартный исполнитель
        System.out.println("Сервер запущен на http://localhost:8080/api/otp");
        server.start();
    }
}