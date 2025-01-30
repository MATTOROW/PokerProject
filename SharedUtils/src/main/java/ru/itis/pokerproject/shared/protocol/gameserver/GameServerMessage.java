package ru.itis.pokerproject.shared.protocol.gameserver;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public class GameServerMessage {
    public static final int MAX_LENGTH = 1000; // Максимальная длина данных
    protected static final byte[] START_BYTES = new byte[]{0xC, 0xD};

    private final MessageType type;
    private final byte[] data;

    public GameServerMessage(MessageType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public enum MessageType {
        CONNECT_TO_ROOM_REQUEST,
        CONNECT_TO_ROOM_RESPONSE,
        GET_ROOMS_REQUEST,
        GET_ROOMS_RESPONSE,
        READY,
        RAISE,
        FOLD,
        CALL,
        CHECK
    }
}
