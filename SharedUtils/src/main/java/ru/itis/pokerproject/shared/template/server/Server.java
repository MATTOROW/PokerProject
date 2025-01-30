package ru.itis.pokerproject.shared.template.server;

import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

public interface Server<E extends Enum<E>, M extends AbstractServerMessage<E>> {
    void registerListener(ServerEventListener<E, M> listener) throws ServerException;

    void sendMessage(int connectionId, M message) throws ServerException;

    void sendBroadCastMessage(M message) throws ServerException;

    void start() throws ServerException;
}
