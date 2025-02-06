package ru.itis.pokerproject.gameserver.service.game;

import ru.itis.pokerproject.gameserver.model.game.HandType;
import ru.itis.pokerproject.gameserver.model.game.HandWorth;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.model.Suit;
import ru.itis.pokerproject.shared.model.Value;

import java.util.*;
import java.util.stream.Collectors;

public class HandEvaluator {
    private HandEvaluator() {
    }

    public static HandWorth calculateHandValue(List<Card> hand, int count) {
        if (count < 5) {
            return null;
        } else {
            return generateCardsCombinations(hand).stream()
                    .map(HandEvaluator::calculateHandValue)
                    .max(Comparator.naturalOrder()).get();
        }
    }

    private static HandWorth calculateHandValue(List<Card> hand) {
        HandType handType = getHandType(hand);
        List<Card> normalizedHand = normalizedHand(hand, handType);
        int deckValue = handType.ordinal();
        for (int i = 0; i < 5; ++i) {
            deckValue = (deckValue << 4) + normalizedHand.get(i).value().ordinal();
        }
        return new HandWorth(handType, normalizedHand, deckValue);
    }

    public static HandType getHandType(List<Card> hand, int count) {
        if (count < 5) {
            return null;
        }
        return generateCardsCombinations(hand).stream().map(comb -> getHandType(comb)).max(Comparator.naturalOrder()).get();
    }

    public static HandType getHandType(List<Card> hand) {
        Map<Suit, Integer> suitCount = hand.stream().collect(Collectors.toMap(Card::suit, card -> 1, Integer::sum));
        Map<Value, Integer> valueCount = hand.stream().collect(Collectors.toMap(Card::value, card -> 1, Integer::sum));

        boolean isFlush = suitCount.containsValue(5);
        boolean isStraight = isStraight(hand);

        if (isStraight && isFlush && valueCount.containsKey(Value.ACE) && valueCount.containsKey(Value.KING)) {
            return HandType.FLUSH_ROYAL;
        }
        if (isStraight && isFlush) {
            return HandType.STRAIGHT_FLUSH;
        }
        if (valueCount.containsValue(4)) {
            return HandType.FOUR_OF_A_KIND;
        }
        if (valueCount.containsValue(3) && valueCount.containsValue(2)) {
            return HandType.FULL_HOUSE;
        }
        if (isFlush) {
            return HandType.FLUSH;
        }
        if (isStraight) {
            return HandType.STRAIGHT;
        }
        if (valueCount.containsValue(3)) {
            return HandType.THREE_OF_A_KIND;
        }
        long pairCount = valueCount.values().stream().filter(count -> count == 2).count();
        if (pairCount == 2) {
            return HandType.TWO_PAIR;
        }
        if (pairCount == 1) {
            return HandType.ONE_PAIR;
        }
        return HandType.HIGH_CARD;
    }

    private static boolean isStraight(List<Card> hand) {
        if (hand.size() != 5) return false;
        List<Card> sorted = hand.stream().sorted().toList();
        for (int i = 1; i < 5; ++i) {
            if (sorted.get(i).value().compareTo(sorted.get(i - 1).value()) != 1 &&
                    (sorted.get(i).value() != Value.ACE || sorted.get(i - 1).value() != Value.FIVE)) {
                return false;
            }
        }
        return true;
    }

    public static List<Card> normalizedHand(List<Card> hand, HandType handType) {
        List<Card> normalized = new ArrayList<>(hand);
        normalized.sort(Collections.reverseOrder());
        switch (handType) {
            case FOUR_OF_A_KIND -> sortFourOfAKind(normalized);
            case FULL_HOUSE -> sortFullHouse(normalized);
            case STRAIGHT, STRAIGHT_FLUSH -> sortStraight(normalized);
            case THREE_OF_A_KIND -> sortThreeOfAKind(normalized);
            case TWO_PAIR -> sortTwoPair(normalized);
            case ONE_PAIR -> sortOnePair(normalized);
        }
        return normalized;
    }

    private static void sortOnePair(List<Card> hand) {
        if (hand.get(1).value() == hand.get(2).value()) {
            swapById(hand, 0, 2);
        } else if (hand.get(2).value() == hand.get(3).value()) {
            swapById(hand, 0, 2);
            swapById(hand, 1, 3);
        } else if (hand.get(3).value() == hand.get(4).value()) {
            hand.sort(Comparator.comparing(Card::value));
            swapById(hand, 2, 4);
        }
    }

    private static void sortTwoPair(List<Card> hand) {
        if (hand.get(0).value() != hand.get(1).value()) {
            for (int i = 0; i < 4; ++i) {
                swapById(hand, i, i + 1);
            }
        } else if (hand.get(0).value() == hand.get(1).value() && hand.get(3).value() == hand.get(4).value()) {
            swapById(hand, 2, 4);
        }
    }

    private static void sortThreeOfAKind(List<Card> hand) {
        if (hand.get(1).value() == hand.get(3).value()) {
            swapById(hand, 0, 3);
        } else if (hand.get(2).value() == hand.get(4).value()) {
            swapById(hand, 0, 3);
            swapById(hand, 1, 4);
        }
    }

    private static void sortStraight(List<Card> hand) {
        if (hand.get(0).value() == Value.ACE) {
            if (hand.get(1).value() != Value.KING) {
                for (int i = 0; i < 4; ++i) {
                    swapById(hand, i, i + 1);
                }
            }
        }
    }

    private static void sortFullHouse(List<Card> hand) {
        if (hand.get(0).value() != hand.get(2).value()) {
            swapById(hand, 0, 3);
            swapById(hand, 1, 4);
        }
    }

    private static void sortFourOfAKind(List<Card> hand) {
        if (hand.get(0).value() != hand.get(1).value()) {
            swapById(hand, 0, 4);
        }
    }

    private static <T> void swapById(List<T> list, int index1, int index2) {
        T obj1 = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, obj1);
    }

    public static List<List<Card>> generateCardsCombinations(List<Card> cards) {
        List<List<Card>> result = new ArrayList<>();
        int cardsCount = cards.size();
        if (cardsCount < 5) {
            return null;
        }
        int[] combination = new int[5];
        for (int i = 0; i < 5; i++) {
            combination[i] = i;
        }

        while (combination[4] < cardsCount) {
            List<Card> current = new ArrayList<>(4);
            for (int index : combination) {
                current.add(cards.get(index));
            }
            result.add(current);

            int t = 4;
            while (t >= 0 && combination[t] == cardsCount - 5 + t) {
                t--;
            }

            if (t >= 0) {
                combination[t]++;
                for (int i = t + 1; i < 5; i++) {
                    combination[i] = combination[i - 1] + 1;
                }
            } else {
                break;
            }
        }
        return result;
    }
}