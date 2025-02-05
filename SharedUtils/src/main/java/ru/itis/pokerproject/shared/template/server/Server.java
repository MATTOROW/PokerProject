package ru.itis.pokerproject.shared.template.server;

import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

import java.net.Socket;

public interface Server<E extends Enum<E>, M extends AbstractServerMessage<E>> {
    void registerListener(ServerEventListener<E, M> listener) throws ServerException;

    void sendMessage(Socket socket, M message) throws ServerException;

    void start() throws ServerException;
}
