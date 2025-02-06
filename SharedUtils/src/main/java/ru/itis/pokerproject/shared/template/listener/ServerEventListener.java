package ru.itis.pokerproject.shared.template.listener;

import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

import java.net.Socket;

public interface ServerEventListener<E extends Enum<E>, M extends AbstractServerMessage<E>> {
    M handle(Socket socket, M message) throws ServerEventListenerException;

    E getType();
}
