package ru.itis.pokerproject.gameserver.model;


import ru.itis.pokerproject.gameserver.model.game.Card;
import ru.itis.pokerproject.gameserver.model.game.HandWorth;
import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.gameserver.service.game.DeckGenerator;
import ru.itis.pokerproject.gameserver.service.game.HandEvaluator;
import ru.itis.pokerproject.gameserver.service.game.MainPot;
import ru.itis.pokerproject.gameserver.service.game.SidePot;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;

import java.net.Socket;
import java.util.*;

public class GameHandler {
    private final Room room;
    private final SocketServer socketServer;
    private MainPot mainPot = new MainPot();
    private List<Player> activePlayers = new ArrayList<>(); // Кто еще в игре
    private long currentBet = 0; // Текущая ставка
    private int lastRaiserIndex = -1; // Кто последний делал RAISE
    private int currentDealerIndex = -1;
    private List<Card> communityCards = new ArrayList<>();
    private int currentStep = 0;
    private Map<Player, HandWorth> handValues = new HashMap<>();

    public GameHandler(Room room, SocketServer socketServer) {
        this.room = room;
        this.socketServer = socketServer;
        this.currentBet = room.getMinBet();
    }

    public boolean addPlayer(Player player) {
        boolean added = room.addPlayer(player);
        if (!added) return false;

        broadcastNewPlayer(player);
        return true;
    }

    private void broadcastNewPlayer(Player player) {
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_CONNECTED, player.getInfo().getBytes());
        sendBroadcast(message, player.getUsername());
    }

    public void removePlayer(Player player) {
        if (player != null) {
            room.removePlayer(player);
            broadcastPlayerDisconnected(player.getUsername());
        }
    }

    public void removePlayer(Socket socket) {
        Player player = room.getPlayer(socket);
        removePlayer(player);
    }

    public void setPlayerReady(String username) {
        room.getPlayer(username).setReady(true);
        broadcastPlayerReady(username);

        if (room.allPlayersReady()) {
            startGame();
        }
    }

    private void broadcastPlayerReady(String username) {
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_IS_READY, username.getBytes());
        sendBroadcast(message, username);
    }

    private void broadcastPlayerDisconnected(String username) {
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_DISCONNECTED, username.getBytes());
        sendBroadcast(message, username);
    }

    private void sendBroadcast(GameServerMessage message, String username) {
        for (Player p : room.getPlayers()) {
            if (!p.getUsername().equals(username)) {
                socketServer.sendMessage(p.getSocket(), message);
            }
        }
    }
    
    private void sendBroadcastToActivePlayers(GameServerMessage message) {
        for (Player p: activePlayers) {
            socketServer.sendMessage(p.getSocket(), message);
        }
    }
    
    private void sendMessage(GameServerMessage message, int playerIndex) {
        socketServer.sendMessage(room.getPlayer(playerIndex).getSocket(), message);
    }
    
    private void sendError(Player player, String data) {
        socketServer.sendMessage(player.getSocket(), GameServerMessageUtils.createMessage(GameMessageType.ERROR, data.getBytes()));
    }

    private void startGame() {
        // Обновляем данные перед новой игрой
        activePlayers = new ArrayList<>(room.getPlayers());
        activePlayers.forEach(Player::reset); // Сбрасываем статусы
        communityCards = new ArrayList<>();
        mainPot = new MainPot();
        lastRaiserIndex = -1;
        currentStep = 0;
        handValues = new HashMap<>();

        if (currentDealerIndex == -1) {
            currentDealerIndex = 0; // Первый запуск
        } else {
            currentDealerIndex = (currentDealerIndex + 1) % activePlayers.size();
        }

        // Генерируем колоду
        List<Card> deck = DeckGenerator.generateRandomDeck(5 + 2 * activePlayers.size());

        // Раздаем карты
        for (int i = 0; i < activePlayers.size(); i++) {
            Player player = activePlayers.get(i);
            List<Card> playerCards = new ArrayList<>();
            playerCards.add(deck.get(5 + i * 2));
            playerCards.add(deck.get(6 + i * 2));
            player.setCards(playerCards);
            sendPlayerCards(player);
        }

        // Общие карты
        communityCards.clear();
        for (int i = 0; i < 5; i++) {
            communityCards.add(deck.get(i));
        }

        // Обновляем банк и блайнды
        mainPot = new MainPot();
        lastRaiserIndex = -1;

        // Блайнды
        int smallBlindIndex = (currentDealerIndex + 1) % activePlayers.size();
        int bigBlindIndex = (currentDealerIndex + 2) % activePlayers.size();

        Player smallBlind = activePlayers.get(smallBlindIndex);
        Player bigBlind = activePlayers.get(bigBlindIndex);

        smallBlind.subtractMoney(currentBet);
        smallBlind.addMoney(currentBet);
        mainPot.addAmount(currentBet);

        bigBlind.subtractMoney(currentBet * 2);
        bigBlind.addMoney(currentBet * 2);
        mainPot.addAmount(currentDealerIndex * 2);

        // Первый ход после блайндов
        currentStep = (currentDealerIndex + 3) % activePlayers.size();
        requestPlayerAction(activePlayers.get(currentStep));
    }

    private void handleBet(Player player, GameMessageType betType, long betAmount) {
        switch (betType) {
            case FOLD:
                player.setFolded(true);
                checkEndRound();
                sendBroadcastToActivePlayers(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_FOLDED, player.getUsername().getBytes()));
                break;

            case CHECK:
                if (player.getCurrentBet() < currentBet) {
                    sendError(player, "You cannot CHECK, current bet is higher!");
                } else {
                    advanceTurn();
                }
                break;

            case CALL:
                long callAmount = currentBet - player.getCurrentBet();
                if (player.getMoney() >= callAmount) {
                    player.subtractMoney(callAmount);
                    player.addBet(callAmount);
                    mainPot.addAmount(callAmount);
                    advanceTurn();
                } else {
                    sendError(player, "Not enough balance to CALL!");
                }
                break;

            case RAISE:
                if (betAmount <= currentBet) {
                    sendError(player, "Raise must be higher than current bet!");
                    return;
                }

                long raiseDiff = betAmount - player.getCurrentBet();
                if (player.getMoney() >= raiseDiff) {
                    player.subtractMoney(raiseDiff);
                    player.addBet(raiseDiff);
                    currentBet = betAmount;
                    lastRaiserIndex = activePlayers.indexOf(player);
                    mainPot.addAmount(raiseDiff);
                    advanceTurn();
                } else {
                    sendError(player, "Not enough balance to RAISE!");
                }
                break;

            case ALL_IN:
                long allInAmount = player.getMoney();
                player.subtractMoney(allInAmount);
                player.addBet(allInAmount);

                // Если ставка игрока меньше текущей, создаем SidePot
                if (player.getCurrentBet() < currentBet) {
                    SidePot sidePot = new SidePot(allInAmount, new ArrayList<>(activePlayers));
                    mainPot.addSidePot(sidePot);
                } else {
                    mainPot.addAmount(allInAmount);
                    currentBet = (int) allInAmount;
                    lastRaiserIndex = activePlayers.indexOf(player);
                }

                player.setAllIn(true);
                advanceTurn();
                break;
        }
    }

    private void checkEndRound() {
        long activeCount = activePlayers.stream().filter(p -> !p.isFolded()).count();

        if (activeCount == 1) {
            endGame(activePlayers.stream().filter(p -> !p.isFolded()).findFirst().get());
        }
    }

    private void endGame(Player winner) {
        long totalWinnings = mainPot.getAmount();
        winner.addMoney(totalWinnings);

        GameServerMessage message = GameServerMessageUtils.createMessage(
                GameMessageType.GAME_END,
                (winner.getUsername() + ";WON;" + totalWinnings).getBytes()
        );

        sendBroadcastToActivePlayers(message);
    }

    private void sendPlayerCards(Player player) {
        String cardData = String.join(";", player.getCardsInfo());
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_CARDS, cardData.getBytes());
        socketServer.sendMessage(player.getSocket(), message);
    }

    private void endBettingRound() {
        if (communityCards.size() == 0) {
            revealFlop();
        } else if (communityCards.size() == 3) {
            revealTurn();
        } else if (communityCards.size() == 4) {
            revealRiver();
        } else {
            determineWinner();
        }
    }

    private void revealFlop() {
        sendCommunityCards(0, 3);
        startNewBettingRound();
    }

    private void revealTurn() {
        sendCommunityCards(3, 4);
        startNewBettingRound();
    }

    private void revealRiver() {
        sendCommunityCards(4, 5);
        startNewBettingRound();
    }

    private void sendCommunityCards(int from, int to) {
        StringBuilder cardData = new StringBuilder();
        for (int i = from; i < to; i++) {
            cardData.append(communityCards.get(i)).append(";");
        }
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.COMMUNITY_CARDS, cardData.toString().getBytes());
        sendBroadcastToActivePlayers(message);
    }

    private void startNewBettingRound() {
        currentBet = 0;
        lastRaiserIndex = -1;

        for (Player player : activePlayers) {
            player.resetBet();
        }

        currentStep = (currentDealerIndex + 1) % activePlayers.size();
        requestPlayerAction(activePlayers.get(currentStep));
    }

    private void determineWinner() {
        Map<Player, HandWorth> handValues = new HashMap<>();

        for (Player player : activePlayers) {
            List<Card> allCards = new ArrayList<>(communityCards);
            List<Card> playerCards = player.getCards();
            allCards.add(playerCards.get(0));
            allCards.add(playerCards.get(1));

            HandWorth handWorth = HandEvaluator.calculateHandValue(allCards, 7);
            handValues.put(player, handWorth);
        }

        Player winner = Collections.max(handValues.entrySet(), Map.Entry.comparingByValue()).getKey();

        distributeWinnings(winner);
    }

    private void distributeWinnings(Player winner) {
        long totalWinnings = mainPot.getAmount();
        winner.addMoney(totalWinnings);

        for (Player player : room.getPlayers()) {
            boolean isWinner = player.equals(winner);
            String messageData = (isWinner ? "1" : "0") + "\n";

            for (Map.Entry<Player, HandWorth> entry : handValues.entrySet()) {
                messageData += entry.getKey().getUsername() + ";" + entry.getValue().toString() + "\n";
            }

            GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.GAME_END, messageData.getBytes());
            socketServer.sendMessage(player.getSocket(), message);
        }

        resetGame();
    }

    private void resetGame() {
        for (Player player : room.getPlayers()) {
            player.reset();
        }
        activePlayers.clear();
    }

    private void advanceTurn() {
        do {
            currentStep = (currentStep + 1) % activePlayers.size();
        } while (activePlayers.get(currentStep).isFolded() || activePlayers.get(currentStep).isAllIn());

        if (currentStep == lastRaiserIndex) {
            endBettingRound();
        } else {
            requestPlayerAction(activePlayers.get(currentStep));
        }
    }

    private void broadcastPlayerAction(GameMessageType type, String data) {
        GameServerMessage message = GameServerMessageUtils.createMessage(type, data.getBytes());
        for (Player p : room.getPlayers()) {
            socketServer.sendMessage(p.getSocket(), message);
        }
    }

    public void handlePlayerDisconnect(Player player) {
        if (player == null) return;

        activePlayers.remove(player);
        player.setFolded(true);

        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_DISCONNECTED, player.getUsername().getBytes());
        sendBroadcastToActivePlayers(message);

        if (activePlayers.size() == 1) {
            determineWinner(); // Если остался один — он победил
        } else if (currentStep >= activePlayers.size()) {
            currentStep = 0; // Чтобы не выйти за границы массива
        }
    }
}
