package ru.itis.pokerproject.gameserver.models.game;

import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;

import java.io.IOException;
import java.net.Socket;

public class Player {
    private Socket socket;
    private String username;
    private long money;

    public Player(Socket socket, String username, long money) {
        this.socket = socket;
        this.username = username;
        this.money = money;
    }

    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getMoney() {
        return money;
    }

    public void addMoney(long money) {
        this.money += money;
    }

    public void subtractMoney(long money) {
        this.money -= money;
    }

    public void sendMessage(GameServerMessage message) {
        try {
            socket.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInfo() {
        return "%s;%s".formatted(username, money);
    }
}
