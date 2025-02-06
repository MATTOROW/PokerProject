package ru.itis.pokerproject.service;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.Client;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class GetRoomsService {
    private final Client<ClientMessageType, ClientServerMessage> client;

    public GetRoomsService(Client<ClientMessageType, ClientServerMessage> client) {
        this.client = client;
    }

    public String[] getRoomsInfo() throws ClientException {
        ClientServerMessage message = ClientServerMessageUtils.createMessage(ClientMessageType.GET_ROOMS_REQUEST, new byte[0]);
        ClientServerMessage response = client.sendMessage(message);
        if (response.getType() == ClientMessageType.GET_ROOMS_RESPONSE) {
            String data = new String(response.getData());
            if (data.isEmpty()) {
                return new String[0];
            }
            String[] parts = data.split("\n");
            return parts;
        } else {
            throw new ClientException(new String(response.getData()));
        }
    }
}
