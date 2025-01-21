package ru.itis.pokerproject.application;


import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public interface ClientExample {
    public void connect() throws ClientException;
    public ClientServerMessage sendMessage(ClientServerMessage message) throws ClientException;
}
