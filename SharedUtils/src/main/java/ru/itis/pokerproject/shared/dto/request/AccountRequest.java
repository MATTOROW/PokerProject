package ru.itis.pokerproject.shared.dto.request;

import java.io.Serializable;

public record AccountRequest(String username, String password) implements Serializable {
}
