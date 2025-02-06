package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.ConnectToRoomService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.util.UUID;

public class ConnectToRoomEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {
    @Override
    public ClientServerMessage handle(Socket socket, ClientServerMessage message) throws ServerEventListenerException {
        UUID roomCode = UUID.fromString(new String(message.getData()));
        byte[] data = ConnectToRoomService.getServerAddress(roomCode);
        ClientServerMessage answer;
        if (data == null) {
            answer = ClientServerMessageUtils.createMessage(ClientMessageType.ERROR, "There is no such room.".getBytes());
        } else {
            answer = ClientServerMessageUtils.createMessage(ClientMessageType.CONNECT_TO_ROOM_RESPONSE, data);
        }
        return answer;
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CONNECT_TO_ROOM_REQUEST;
    }
}
