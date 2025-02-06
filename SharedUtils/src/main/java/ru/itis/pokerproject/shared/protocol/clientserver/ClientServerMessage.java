package ru.itis.pokerproject.shared.protocol.clientserver;

import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

public class ClientServerMessage extends AbstractServerMessage<ClientMessageType> {
    protected static final int MAX_LENGTH = 1000; // Максимальная длина данных
    protected static final byte[] START_BYTES = new byte[]{0xA, 0xB};

    public ClientServerMessage(ClientMessageType type, byte[] data) {
        super(type, data);
    }

    public static int getMaxLength() {
        return MAX_LENGTH;
    }

    public static byte[] getStartBytes() {
        return START_BYTES;
    }
}
