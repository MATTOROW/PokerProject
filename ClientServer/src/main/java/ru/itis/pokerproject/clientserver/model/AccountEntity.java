package ru.itis.pokerproject.clientserver.model;

public class AccountEntity {
    private String username;
    private String password;
    private long money;

    public AccountEntity() {
    }

    public AccountEntity(String username, String password, long money) {
        this.username = username;
        this.password = password;
        this.money = money;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }
}
