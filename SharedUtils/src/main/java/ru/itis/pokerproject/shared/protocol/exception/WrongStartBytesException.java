package ru.itis.pokerproject.shared.protocol.exception;


import java.util.Arrays;

public class WrongStartBytesException extends MessageException {
    public WrongStartBytesException(byte[] startBytes) {
        super("Message first bytes must be %s".formatted(Arrays.toString(startBytes)));
    }
}
