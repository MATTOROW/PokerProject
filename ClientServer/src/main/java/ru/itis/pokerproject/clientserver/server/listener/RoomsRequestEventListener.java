package ru.itis.pokerproject.clientserver.server.listener;

import ru.itis.pokerproject.clientserver.service.GetRoomsService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

public class RoomsRequestEventListener implements ServerEventListener<ClientMessageType, ClientServerMessage> {
    @Override
    public ClientServerMessage handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException {
        byte[] data = GetRoomsService.getRooms();
        System.out.println("Меня тоже вызвали!");
        return ClientServerMessageUtils.createMessage(ClientMessageType.GET_ROOMS_RESPONSE, data);
    }

    @Override
    public ClientMessageType getType() {
        return ClientMessageType.GET_ROOMS_REQUEST;
    }
}
