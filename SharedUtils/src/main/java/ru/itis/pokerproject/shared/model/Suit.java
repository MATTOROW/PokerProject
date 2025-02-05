package ru.itis.pokerproject.shared.model;

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    @Override
    public String toString() {
        return name();
    }

    public String getSuitSymbol() {
        switch (this) {
            case HEARTS:
                return "♥";
            case SPADES:
                return "♠";
            case CLUBS:
                return "♣";
            case DIAMONDS:
                return "♦";
            default:
                return "";
        }
    }
}
