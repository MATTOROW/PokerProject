package ru.itis.pokerproject.shared.protocol.gameserver;

import ru.itis.pokerproject.shared.protocol.exception.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage.MAX_LENGTH;
import static ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage.START_BYTES;

public class GameServerMessageUtils {
    // Фабричный метод для создания сообщения с типом и данными
    public static GameServerMessage createMessage(
            GameMessageType messageType,
            byte[] data
    ) throws MessageException {
        if (data.length > MAX_LENGTH) {
            throw new ExceedingLengthException(data.length, MAX_LENGTH);
        }
        return new GameServerMessage(messageType, data);
    }

    // Сериализация сообщения в байтовый массив
    public static byte[] getBytes(GameServerMessage message) {
        int rawMessageLength = START_BYTES.length + 4 + 4 + message.getData().length;
        byte[] rawMessage = new byte[rawMessageLength];
        int index = 0;

        System.arraycopy(START_BYTES, 0, rawMessage, index, START_BYTES.length);
        index += START_BYTES.length;

        byte[] typeBytes = ByteBuffer.allocate(4).putInt(message.getType().ordinal()).array();
        System.arraycopy(typeBytes, 0, rawMessage, index, typeBytes.length);
        index += typeBytes.length;

        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(message.getData().length).array();
        System.arraycopy(lengthBytes, 0, rawMessage, index, lengthBytes.length);
        index += lengthBytes.length;

        System.arraycopy(message.getData(), 0, rawMessage, index, message.getData().length);

        return rawMessage;
    }

    // Десериализация сообщения из потока
    public static GameServerMessage readMessage(InputStream in)
            throws EmptyMessageException,
            ExceedingLengthException,
            MessageReadingException,
            UnknownMessageTypeException,
            WrongStartBytesException {
        byte[] buffer = new byte[MAX_LENGTH];
        synchronized (in) {
            try {
                int read = in.read(buffer, 0, START_BYTES.length);
                if (read == -1) {
                    throw new EmptyMessageException();
                }
                if (!Arrays.equals(Arrays.copyOfRange(buffer, 0, START_BYTES.length), START_BYTES)) {
                    throw new WrongStartBytesException(START_BYTES);
                }

                // Читаем тип сообщения
                in.read(buffer, 0, 4);
                int messageTypeOrdinal = ByteBuffer.wrap(buffer, 0, 4).getInt();
                GameMessageType messageType = GameMessageType.values()[messageTypeOrdinal];
                if (messageType == null) {
                    throw new UnknownMessageTypeException(messageTypeOrdinal);
                }

                // Читаем длину данных
                in.read(buffer, 0, 4);
                int messageLength = ByteBuffer.wrap(buffer, 0, 4).getInt();
                if (messageLength > MAX_LENGTH) {
                    throw new ExceedingLengthException(messageLength, MAX_LENGTH);
                }

                // Читаем сами данные
                in.read(buffer, 0, messageLength);
                return new GameServerMessage(messageType, Arrays.copyOfRange(buffer, 0, messageLength));
            } catch (IOException e) {
                throw new MessageReadingException();
            }
        }
    }

    public static String toString(GameServerMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("First bytes: ").append(Arrays.toString(START_BYTES)).append("\n");
        sb.append("Type: ").append(message.getType()).append("\n");
        sb.append("Length: ").append(message.getData().length).append("\n");
        sb.append("Data: ").append(Arrays.toString(message.getData())).append("\n");
        return sb.toString();
    }
}
