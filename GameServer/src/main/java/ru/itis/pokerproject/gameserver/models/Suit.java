package ru.itis.pokerproject.gameserver.models;

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    @Override
    public String toString() {
        return name();
    }
}
