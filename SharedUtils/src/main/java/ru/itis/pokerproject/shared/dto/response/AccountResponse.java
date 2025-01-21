package ru.itis.pokerproject.shared.dto.response;

import java.io.Serializable;

public record AccountResponse(String username, long money) implements Serializable {
}
