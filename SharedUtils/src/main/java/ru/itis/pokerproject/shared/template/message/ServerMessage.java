package ru.itis.pokerproject.shared.template.message;

public interface ServerMessage<E extends Enum<E>> {
    E getType();

    byte[] getData();
}
