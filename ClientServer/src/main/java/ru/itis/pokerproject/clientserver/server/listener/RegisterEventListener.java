package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.RegisterService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RegisterEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {
    @Override
    public ClientServerMessage handle(Socket socket, ClientServerMessage message) throws ServerEventListenerException {

        String data = new String(message.getData(), StandardCharsets.UTF_8);
        String[] parts = data.split(";", 2);
        if (parts.length != 2) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientMessageType.ERROR,
                    "Invalid message format. Expected username;password".getBytes()
            );
            return errorMessage;
        }
        String username = parts[0];
        String password = parts[1];

        byte[] registerData = RegisterService.register(username, password);
        if (registerData == null) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientMessageType.ERROR,
                    "Error while creating account. Probably, this username is already in use.".getBytes()
            );
            return errorMessage;
        }
        ClientServerMessage answer = ClientServerMessageUtils.createMessage(
                ClientMessageType.REGISTER_RESPONSE,
                registerData
        );

        return answer;
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.REGISTER_REQUEST;
    }
}
