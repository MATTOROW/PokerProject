package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.repository.AccountRepository;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;

public class UpdateUserDataEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {
    @Override
    public ClientServerMessage handle(Socket socket, ClientServerMessage message) throws ServerEventListenerException {
        String[] data = new String(message.getData()).split(";");
        String username = data[0];
        long money = Long.parseLong(data[1]);
        long newValue = new AccountRepository().updateMoney(username, money);
        return ClientServerMessageUtils.createMessage(ClientMessageType.UPDATE_USER_DATA_RESPONSE, "%d".formatted(newValue).getBytes());
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.UPDATE_USER_DATA_REQUEST;
    }
}
