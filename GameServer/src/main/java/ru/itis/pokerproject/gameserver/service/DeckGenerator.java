package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.models.game.Card;
import ru.itis.pokerproject.gameserver.models.game.Suit;
import ru.itis.pokerproject.gameserver.models.game.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeckGenerator {
    private static final List<Card> deck = new CopyOnWriteArrayList<>();

    static {
        for (Suit suit: Suit.values()) {
            for (Value value: Value.values()) {
                deck.add(new Card(suit, value));
            }
        }
    }

    private DeckGenerator() {

    }

    public static List<Card> generateDeck() {
        return new ArrayList<>(deck);
    }

    public static List<Card> generateRandomDeck(int count) {
        List<Card> generatedDeck = new ArrayList<>(deck);
        Collections.shuffle(generatedDeck);
        return generatedDeck.subList(0, count);
    }
}
