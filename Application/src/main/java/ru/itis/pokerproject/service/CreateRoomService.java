package ru.itis.pokerproject.service;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.Client;
import ru.itis.pokerproject.shared.template.client.ClientException;

import java.util.UUID;

public class CreateRoomService {
    private final Client<ClientMessageType, ClientServerMessage> client;

    public CreateRoomService(Client<ClientMessageType, ClientServerMessage> client) {
        this.client = client;
    }

    public UUID createRoom(int maxPlayers, long maxBet) throws ClientException {
        ClientServerMessage message = ClientServerMessageUtils.createMessage(
                ClientMessageType.CREATE_ROOM_REQUEST, "%d;%d".formatted(maxPlayers, maxBet).getBytes()
        );
        ClientServerMessage response = client.sendMessage(message);
        if (response.getType() == ClientMessageType.CREATE_ROOM_RESPONSE) {
            String data = new String(response.getData());
            UUID code = UUID.fromString(new String(data));
            return code;
        } else {
            throw new ClientException(new String(response.getData()));
        }
    }
}
