package ru.itis.pokerproject.shared.protocol.gameserver;

public enum GameMessageType {
    CONNECT_TO_ROOM_REQUEST,
    CONNECT_TO_ROOM_RESPONSE,
    GET_ROOMS_COUNT_REQUEST,
    GET_ROOMS_COUNT_RESPONSE,
    GET_ROOMS_REQUEST,
    GET_ROOMS_RESPONSE,
    CREATE_ROOM_REQUEST,
    CREATE_ROOM_RESPONSE,
    READY,
    RAISE,
    FOLD,
    CALL,
    CHECK,
    ERROR
}
