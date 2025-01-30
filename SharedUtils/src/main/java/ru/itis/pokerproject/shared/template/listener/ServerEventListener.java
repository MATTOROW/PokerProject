package ru.itis.pokerproject.shared.template.listener;

import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

public interface ServerEventListener<E extends Enum<E>, M extends AbstractServerMessage<E>> {
    M handle(int connectionId, M message) throws ServerEventListenerException;

    E getType();
}
