package ru.itis.pokerproject.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itis.pokerproject.shared.template.client.Client;
import ru.itis.pokerproject.shared.template.client.ClientException;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;

public class AuthService {
    private final Client client;
    private final static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(Client client) {
        this.client = client;
    }

    public AccountResponse login(String username, String password) throws ClientException {
        String messageData = username + ";" + password;
            ClientServerMessage message = ClientServerMessageUtils.createMessage(
                    ClientServerMessage.MessageType.LOGIN_REQUEST,
                    messageData.getBytes()
            );
            ClientServerMessage response = client.sendMessage(message);
            if (response.getType() == ClientServerMessage.MessageType.LOGIN_RESPONSE) {
                String data = new String(response.getData());
                String[] parts = data.split(";");
                return new AccountResponse(parts[0], Long.parseLong(parts[1]));
            } else {
                throw new ClientException(new String(response.getData()));
            }
    }

    public AccountResponse register(String username, String password) throws ClientException {
        String messageData = username + ";" + encoder.encode(password);
        ClientServerMessage message = ClientServerMessageUtils.createMessage(
                ClientServerMessage.MessageType.REGISTER_REQUEST,
                    messageData.getBytes()
        );
        ClientServerMessage response = client.sendMessage(message);
        if (response.getType() == ClientServerMessage.MessageType.REGISTER_RESPONSE) {
            String data = new String(response.getData());
            String[] parts = data.split(";");
            return new AccountResponse(parts[0], Long.parseLong(parts[1]));
        } else {
            throw new ClientException(new String(response.getData()));
        }
    }
}
