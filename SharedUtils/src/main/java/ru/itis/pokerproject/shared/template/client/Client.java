package ru.itis.pokerproject.shared.template.client;


import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

public interface Client<E extends Enum<E>, M extends AbstractServerMessage<E>> {
    void connect() throws ClientException;

    M sendMessage(M message) throws ClientException;
}
