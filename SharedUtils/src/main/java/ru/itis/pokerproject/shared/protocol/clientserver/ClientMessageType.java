package ru.itis.pokerproject.shared.protocol.clientserver;

public enum ClientMessageType {
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
    REGISTER_GAME_SERVER_REQUEST,   // Запрос на подключение нового игрового сервера
    REGISTER_GAME_SERVER_RESPONSE,  // Ответ на запрос подключения нового игрового сервера
    GET_ROOMS_REQUEST,              // Запрос на получение списка комнат
    GET_ROOMS_RESPONSE,             // Ответ на запрос получения списка комнат
    CREATE_ROOM_REQUEST,            // Запрос на создание комнаты
    CREATE_ROOM_RESPONSE,           // Ответ на запрос на создание комнаты
    CONNECT_TO_ROOM_REQUEST,        // Запрос на подключение к комнате
    CONNECT_TO_ROOM_RESPONSE,       // Ответ на запрос на подключение к комнате
    ERROR                           // Ошибка
}
