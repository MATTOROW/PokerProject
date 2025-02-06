package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.LoginService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class LoginEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {

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

        byte[] loginData = LoginService.login(username, password);
        if (loginData.length == 0) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientMessageType.ERROR,
                    "Invalid username or password.".getBytes()
            );

            return errorMessage;
        }
        ClientServerMessage answer = ClientServerMessageUtils.createMessage(
                ClientMessageType.LOGIN_RESPONSE,
                loginData
        );

        return answer;
    }


    @Override
    public ClientMessageType getType() {
        return ClientMessageType.LOGIN_REQUEST;
    }
}
