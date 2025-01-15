package ru.itis.pokerproject.network;

import ru.itis.pokerproject.models.Card;
import ru.itis.pokerproject.models.HandWorth;
import ru.itis.pokerproject.services.DeckGenerator;
import ru.itis.pokerproject.services.HandEvaluator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameThread implements Runnable {
    private final int numberOfPlayers;
    private final List<Socket> playersSockets;
    private final List<Card> deck = DeckGenerator.generateDeck();

    public GameThread(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
        this.playersSockets = new ArrayList<>(numberOfPlayers);
    }

    @Override
    public void run() {
        shuffleDeck();
        List<Card> cardsOnTable = this.deck.subList(0, 5);
        List<List<Card>> playersCards = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; ++i) {
            playersCards.add(this.deck.subList(5 + 2 * i, 7 + 2 * i));
        }
        List<HandWorth> handsWorth = playersCards.stream().map(hand -> {
            List<Card> fromTableAndHand = new ArrayList<>(hand);
            fromTableAndHand.addAll(cardsOnTable);
            return HandEvaluator.calculateHandValue(fromTableAndHand, 7);
        }).toList();
        int max = handsWorth.stream().mapToInt(HandWorth::value).max().getAsInt();
        for (int i = 0; i < numberOfPlayers; ++i) {
            Socket playerSocket = playersSockets.get(i);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()))) {
                writer.write("Карты на столе:\n");
                cardsOnTable.forEach(card -> {
                    try {
                        writer.write("%s\n".formatted(card.toString()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                writer.write("----------------------------------\n");
                for (int j = 0; j < numberOfPlayers; ++j) {
                    if (i == j) {
                        writer.write("Ваши карты:\n");
                    } else {
                        writer.write("Карты игрока №%s:\n".formatted(j + 1));
                    }
                    playersCards.get(j).forEach(card -> {
                        try {
                            writer.write("%s\n".formatted(card.toString()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (i == j) {
                        writer.write("У вас %s\n".formatted(handsWorth.get(j).type()));
                    } else {
                        writer.write("У него %s\n".formatted(handsWorth.get(j).type()));
                    }
                    if (handsWorth.get(j).value() == max) {
                        writer.write("ПОБЕДИТЕЛЬ!!!\n");
                    }
                    writer.write("----------------------------------\n");
                }
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean addPlayerSocket(Socket playerSocket) {
        return playersSockets.add(playerSocket);
    }

    public boolean removePlayerSocket(Socket playerSocket) {
        return playersSockets.remove(playerSocket);
    }

    public int countPlayers() {
        return playersSockets.size();
    }

    private void shuffleDeck() {
        Collections.shuffle(this.deck);
    }
}
