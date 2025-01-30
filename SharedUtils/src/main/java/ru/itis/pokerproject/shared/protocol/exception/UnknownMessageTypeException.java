package ru.itis.pokerproject.shared.protocol.exception;

public class UnknownMessageTypeException extends MessageException {
    public UnknownMessageTypeException(int messageTypeSerialNumber) {
        super("Unknown message type with serial number: %s.".formatted(messageTypeSerialNumber));
    }
}
