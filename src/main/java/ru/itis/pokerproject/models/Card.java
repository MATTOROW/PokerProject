package ru.itis.pokerproject.models;

public record Card(Suit suit, Value value) implements Comparable<Card> {

    @Override
    public String toString() {
        return "%s %s".formatted(this.suit.toString(), this.value.toString());
    }

    @Override
    public int compareTo(Card o) {
        return this.value.compareTo(o.value);
    }
}
