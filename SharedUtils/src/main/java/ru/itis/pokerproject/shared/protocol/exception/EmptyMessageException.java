package ru.itis.pokerproject.shared.protocol.exception;

public class EmptyMessageException extends MessageException {
    public EmptyMessageException() {
        super("Message is empty!");
    }
}
