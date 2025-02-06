package ru.itis.pokerproject.shared.template.message;

public abstract class AbstractServerMessage<E extends Enum<E>> implements ServerMessage<E> {
    protected final E type;
    protected final byte[] data;

    public AbstractServerMessage(E type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public static int getMaxLength() {
        return -1;
    }

    public static byte[] getStartBytes() {
        return new byte[0];
    }

    public E getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}
