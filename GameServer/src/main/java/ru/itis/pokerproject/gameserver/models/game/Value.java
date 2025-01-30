package ru.itis.pokerproject.gameserver.models.game;

public enum Value {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

    @Override
    public String toString() {
        return name();
    }
}
