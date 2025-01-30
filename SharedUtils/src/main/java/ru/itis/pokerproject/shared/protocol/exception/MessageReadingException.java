package ru.itis.pokerproject.shared.protocol.exception;

public class MessageReadingException extends MessageException {
    public MessageReadingException() {
        super("Error while reading message.");
    }
}
