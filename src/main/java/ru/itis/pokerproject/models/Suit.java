package ru.itis.pokerproject.models;

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    @Override
    public String toString() {
        return name();
    }
}
