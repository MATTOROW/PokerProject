package ru.itis.pokerproject.shared.protocol.gameserver;

import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

public class GameServerMessage extends AbstractServerMessage<GameMessageType> {
    public static final int MAX_LENGTH = 1000; // Максимальная длина данных
    protected static final byte[] START_BYTES = new byte[]{0xC, 0xD};

    public GameServerMessage(GameMessageType type, byte[] data) {
        super(type, data);
    }

    public static int getMaxLength() {
        return MAX_LENGTH;
    }

    public static byte[] getStartBytes() {
        return START_BYTES;
    }
}
