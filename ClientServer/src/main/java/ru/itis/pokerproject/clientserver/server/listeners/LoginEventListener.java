package ru.itis.pokerproject.clientserver.server.listeners;

import ru.itis.pokerproject.clientserver.server.ServerException;
import ru.itis.pokerproject.clientserver.service.LoginService;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public class LoginEventListener extends AbstractServerEventListener {

    @Override
    public void handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException {
        try {
            if(!this.init){
                throw new ServerEventListenerException("Listener has not been initiated yet.");
            }
            // Could be more optimized and safer
            String data = new String(message.getData(), StandardCharsets.UTF_8);
            String[] parts = data.split(";");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid message format. Expected 'username;password'.");
            }
            String username = parts[0];
            String password = parts[1];

            AccountResponse account = LoginService.login(username, password);
            if (account == null) {
                throw new Exception("Account not found");
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(account);
            objectOutputStream.flush();
            ClientServerMessage answer = ClientServerMessage.createMessage(
                    ClientServerMessage.MessageType.LOGIN_RESPONSE,
                    byteArrayOutputStream.toByteArray()
            );

            this.server.sendMessage(connectionId, answer);
        } catch (Exception e) {
            // Обработка ошибок
            e.printStackTrace();
            try {
                String errorMessage = "Error processing message: " + e.getMessage();
                ClientServerMessage errorResponse = ClientServerMessage.createMessage(
                        ClientServerMessage.MessageType.ERROR,
                        errorMessage.getBytes(StandardCharsets.UTF_8)
                );
                this.server.sendMessage(connectionId, errorResponse);
            } catch (ServerException ee) {
                ee.printStackTrace();
            }
        }
//        IntBuffer buffer = ByteBuffer.wrap(message.getData()).asIntBuffer();
//        int summ = buffer.get(0) + buffer.get(1);
//        Message answer = Message.createMessage(Message.TYPE1, ByteBuffer.allocate(4).putInt(summ).array());
//        try{
//            this.server.sendMessage(connectionId, answer);
//        } catch (ServerException ex) {
//            //Add some catch implementation
//        }
    }

    @Override
    public ClientServerMessage.MessageType getType() {
        return ClientServerMessage.MessageType.LOGIN_REQUEST;
    }
}
