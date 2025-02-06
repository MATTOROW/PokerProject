package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.CreateRoomService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CreateRoomEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {
    @Override
    public ClientServerMessage handle(Socket socket, ClientServerMessage message) throws ServerEventListenerException {
        String data = new String(message.getData(), StandardCharsets.UTF_8);
        String[] parts = data.split(";");
        if (parts.length != 2) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientMessageType.ERROR,
                    "Invalid message format. Expected maxPlayers;minBet".getBytes()
            );
            return errorMessage;
        }
        int maxPlayers, minBet;
        try {
            maxPlayers = Integer.parseInt(parts[0]);
            minBet = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientMessageType.ERROR,
                    "Invalid message format. Expected maxPlayers;minBet".getBytes()
            );
            return errorMessage;
        }

        byte[] code = CreateRoomService.createRoom(maxPlayers, minBet);

        ClientServerMessage answer;
        if (code.length == 0) {
            answer = ClientServerMessageUtils.createMessage(ClientMessageType.ERROR, "There is no servers now. Try later.".getBytes());
        } else {
            answer = ClientServerMessageUtils.createMessage(ClientMessageType.CREATE_ROOM_RESPONSE, code);
        }

        return answer;
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.CREATE_ROOM_REQUEST;
    }
}
