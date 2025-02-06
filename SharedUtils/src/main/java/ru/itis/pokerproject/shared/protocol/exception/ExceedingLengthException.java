package ru.itis.pokerproject.shared.protocol.exception;

public class ExceedingLengthException extends MessageException {
    public ExceedingLengthException(int messageLength, int maxLength) {
        super("Message can't be %s bytes length. Maximum is %s.".formatted(messageLength, maxLength));
    }
}
