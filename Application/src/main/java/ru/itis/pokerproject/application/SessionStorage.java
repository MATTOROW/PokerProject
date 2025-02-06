package ru.itis.pokerproject.application;

public class SessionStorage {
    private static String username;
    private static long money;
    private static String token;

    public static void setSessionData(String username, long money, String token) {
        SessionStorage.username = username;
        SessionStorage.money = money;
        SessionStorage.token = token;
    }

    public static String getUsername() {
        return username;
    }

    public static long getMoney() {
        return money;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {
        username = null;
        money = 0;
        token = null;
    }
}
