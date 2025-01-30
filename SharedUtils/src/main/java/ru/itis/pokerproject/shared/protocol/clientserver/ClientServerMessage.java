package ru.itis.pokerproject.shared.protocol.clientserver;

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

    public enum MessageType {
        PING,                           // Проверка соединения
        PONG,                           // Ответ на проверку соединения
        LOGIN_REQUEST,                  // Запрос на вход
        LOGIN_RESPONSE,                 // Ответ на запрос входа
        REGISTER_REQUEST,               // Запрос на регистрацию
        REGISTER_RESPONSE,              // Ответ на запрос регистрации
        GET_USER_DATA_REQUEST,          // Запрос на получение данных пользователя
        GET_USER_DATA_RESPONSE,         // Ответ на запрос получения данных пользователя
        UPDATE_USER_DATA_REQUEST,       // Запрос на обновление данных пользователя
        UPDATE_USER_DATA_RESPONSE,      // Ответ на запрос обновления данных пользователя
        CONNECT_GAME_SERVER_REQUEST,    // Запрос на подключение нового игрового сервера
        CONNECT_GAME_SERVER_RESPONSE,   // Ответ на запрос подключения нового игрового сервера
        ERROR                           // Ошибка
    }
}
