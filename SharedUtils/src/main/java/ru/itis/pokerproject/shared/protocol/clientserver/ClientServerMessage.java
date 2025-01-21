package ru.itis.pokerproject.shared.protocol.clientserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
public class ClientServerMessage {
    public static final int MAX_LENGTH = 1000; // Максимальная длина данных
    protected static final byte[] START_BYTES = new byte[]{0xA, 0xB};

    private final MessageType type;
    private final byte[] data;

    public ClientServerMessage(MessageType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    // Фабричный метод для создания сообщения с типом и данными
    public static ClientServerMessage createMessage(MessageType messageType, byte[] data) throws IllegalArgumentException {
        if (data.length > MAX_LENGTH) {
            throw new IllegalArgumentException("Message can't be " + data.length
                    + " bytes length. Maximum is " + MAX_LENGTH + ".");
        }
        return new ClientServerMessage(messageType, data);
    }

    // Сериализация сообщения в байтовый массив
    public static byte[] getBytes(ClientServerMessage message) {
        int rawMessageLength = START_BYTES.length
                + 4 // длина типа сообщения
                + 4 // длина данных
                + message.getData().length;
        byte[] rawMessage = new byte[rawMessageLength];
        int index = 0;

        // Добавляем стартовые байты
        System.arraycopy(START_BYTES, 0, rawMessage, index, START_BYTES.length);
        index += START_BYTES.length;

        // Добавляем тип сообщения
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(message.getType().ordinal()).array();
        System.arraycopy(typeBytes, 0, rawMessage, index, typeBytes.length);
        index += typeBytes.length;

        // Добавляем длину данных
        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(message.getData().length).array();
        System.arraycopy(lengthBytes, 0, rawMessage, index, lengthBytes.length);
        index += lengthBytes.length;

        // Добавляем сами данные
        System.arraycopy(message.getData(), 0, rawMessage, index, message.getData().length);

        return rawMessage;
    }

    // Десериализация сообщения из потока
    public static ClientServerMessage readMessage(InputStream in) throws IllegalArgumentException {
        byte[] buffer = new byte[MAX_LENGTH];
        try {
            in.read(buffer, 0, START_BYTES.length);
            if (!Arrays.equals(Arrays.copyOfRange(buffer, 0, START_BYTES.length), START_BYTES)) {
                throw new IllegalArgumentException("Message first bytes must be " + Arrays.toString(START_BYTES));
            }

            // Читаем тип сообщения
            in.read(buffer, 0, 4);
            int messageTypeOrdinal = ByteBuffer.wrap(buffer, 0, 4).getInt();
            MessageType messageType = MessageType.values()[messageTypeOrdinal];
            if (messageType == null) {
                throw new IllegalArgumentException("Unknown message type: " + messageTypeOrdinal);
            }

            // Читаем длину данных
            in.read(buffer, 0, 4);
            int messageLength = ByteBuffer.wrap(buffer, 0, 4).getInt();
            if (messageLength > MAX_LENGTH) {
                throw new IllegalArgumentException(
                        "Message can't be " + messageLength
                                + " bytes length. Maximum is " + MAX_LENGTH + ".");
            }

            // Читаем сами данные
            in.read(buffer, 0, messageLength);
            return new ClientServerMessage(messageType, Arrays.copyOfRange(buffer, 0, messageLength));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Can't read message", ex);
        }
    }

    public static String toString(ClientServerMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("First bytes: ").append(Arrays.toString(START_BYTES)).append("\n");
        sb.append("Type: ").append(message.getType()).append("\n");
        sb.append("Length: ").append(message.getData().length).append("\n");
        sb.append("Data: ").append(Arrays.toString(message.getData())).append("\n");
        sb.append("Decoded data: ").append(new String(message.getData()));
        return sb.toString();
    }

    public enum MessageType {
        LOGIN_REQUEST,     // Запрос на вход
        LOGIN_RESPONSE,    // Ответ на запрос входа
        DATA_REQUEST,      // Запрос данных
        DATA_RESPONSE,     // Ответ с данными
        ERROR              // Ошибка
    }
}
