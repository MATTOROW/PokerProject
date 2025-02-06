package ru.itis.pokerproject.service;

import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.ClientException;


public class ConnectToRoomService {
    private final SocketClient client;

    public ConnectToRoomService(SocketClient client) {
        this.client = client;
    }

    public void connectToRoom(String roomId, String token) throws ClientException {
        ClientServerMessage message = ClientServerMessageUtils.createMessage(ClientMessageType.CONNECT_TO_ROOM_REQUEST, roomId.getBytes());
        ClientServerMessage response = client.sendMessage(message);
        if (response.getType() == ClientMessageType.CONNECT_TO_ROOM_RESPONSE) {
            String[] data = new String(response.getData()).split(":");
            client.connectToGameServer(data[0], Integer.parseInt(data[1]));
            client.sendMessageToGameServer(GameServerMessageUtils.createMessage(
                            GameMessageType.CONNECT_TO_ROOM_REQUEST,
                            "%s;%s".formatted(roomId, token).getBytes()
                    )
            );
        } else {
            throw new ClientException(new String(response.getData()));
        }
    }
}
