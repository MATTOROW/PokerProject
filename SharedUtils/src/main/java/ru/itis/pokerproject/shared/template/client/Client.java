package ru.itis.pokerproject.shared.template.client;


import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public interface Client {
    public void connect() throws ClientException;
    public ClientServerMessage sendMessage(ClientServerMessage message) throws ClientException;
}
