package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.RegisterService;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.AbstractServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.nio.charset.StandardCharsets;

public class RegisterEventListener extends AbstractServerEventListener {
    @Override
    public void handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException {

        if (!this.init) {
            throw new ServerEventListenerException("Listener has not been initiated yet.");
        }
        // Could be more optimized and safer
        String data = new String(message.getData(), StandardCharsets.UTF_8);
        String[] parts = data.split(";", 2);
        if (parts.length != 2) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientServerMessage.MessageType.ERROR,
                    "Invalid message format. Expected username;hashedPassword".getBytes()
            );
            this.server.sendMessage(connectionId, errorMessage);
            return;
        }
        String username = parts[0];
        String password = parts[1];

        AccountResponse account = RegisterService.register(username, password);
        if (account == null) {
            ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                    ClientServerMessage.MessageType.ERROR,
                    "Error while creating account. Probably, this username is already in use.".getBytes()
            );
            this.server.sendMessage(connectionId, errorMessage);
            return;
        }
        ClientServerMessage answer = ClientServerMessageUtils.createMessage(
                ClientServerMessage.MessageType.REGISTER_RESPONSE,
                "%s;%s".formatted(account.username(), account.money()).getBytes()
        );

        this.server.sendMessage(connectionId, answer);
    }

    @Override
    public ClientServerMessage.MessageType getType() {
        return ClientServerMessage.MessageType.REGISTER_REQUEST;
    }
}
