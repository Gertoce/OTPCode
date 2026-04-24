package com.otp.dao;

public class User {
    private int id;
    private String login;
    private String passwordHash;
    private String role;

    // Это "конструктор" — он помогает создавать объект пользователя
    public User(int id, String login, String passwordHash, String role) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Эти методы (геттеры) позволяют другим частям программы "читать" данные
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}
