package ru.itis.pokerproject.gameserver.model;


import ru.itis.pokerproject.gameserver.model.game.HandWorth;
import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.gameserver.service.game.DeckGenerator;
import ru.itis.pokerproject.gameserver.service.game.HandEvaluator;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.protocol.exception.MessageException;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private final Room room;
    private final SocketServer socketServer;
    private final long minBet;
    private int currentDiller = -1; // Индекс дилера (по умолчанию -1, чтобы сдвигался).
    private int currentStep; // Индекс текущего игрока
    private int lastRaiser;   // Индекс последнего игрока, повысившего ставку
    private long currentBet;  // Текущая ставка
    private long pot; // Весь банк
    private List<Player> activePlayers = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();

    public GameHandler(Room room, SocketServer socketServer) {
        this.room = room;
        this.minBet = room.getMinBet();
        this.socketServer = socketServer;
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
            try {
                player.getSocket().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    private void sendMessage(Player player, GameServerMessage message) {
        socketServer.sendMessage(player.getSocket(), message);
    }

    private void sendBroadcastToAll(GameServerMessage message) {
        for (Player p : room.getPlayers()) {
            socketServer.sendMessage(p.getSocket(), message);
        }
    }

    public void startGame() {
        this.activePlayers = new ArrayList<>(room.getPlayers());
        this.pot = 0;
        this.currentBet = minBet;
        List<Card> deck = DeckGenerator.generateRandomDeck(5 + 2 * room.currentPlayersCount());

        this.communityCards = deck.subList(0, 5);
        List<Card> playerCards = deck.subList(5, deck.size()); // Остальные - игрокам

        currentDiller = (currentDiller + 1) % activePlayers.size(); // Определяем дилера
        currentStep = currentDiller; // Первый ход - у дилера

        int cardIndex = 0;
        for (Player player : activePlayers) {
            List<Card> hand = List.of(playerCards.get(cardIndex), playerCards.get(cardIndex + 1));
            cardIndex += 2;
            player.setHand(hand);

            String playerMessage = "%s;%s".formatted(
                    hand.get(0).toString(),
                    hand.get(1).toString()
            );

            GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.START_GAME, playerMessage.getBytes());
            sendMessage(player, message);
        }
        processGame();
    }

    public void processGame() {
        boolean gameProcessing = true;
        boolean needToAsk = true;
        while (true) {
            if (activePlayers.get(currentStep).isAllInned() || activePlayers.get(currentStep).isFolded()) {
                if (currentStep == lastRaiser) {
                    break;
                }
                nextTurn();
                if (lastRaiser == -1 && currentStep == currentDiller) {
                    break;
                }
                continue;
            }
            if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                System.out.println("Остался один не сфолдифший!");
                gameProcessing = false;
                break;
            }
            sendMessage(activePlayers.get(currentStep), GameServerMessageUtils.createMessage(GameMessageType.WAITING_FOR_ACTION, new byte[0]));
            handlePlayerAction();
            if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                gameProcessing = false;
                break;
            }
            if (lastRaiser == -1 && currentStep == currentDiller) {
                break;
            } else if (lastRaiser == currentStep) {
                break;
            }
        }
        if (!gameProcessing) {
            endGame();
        } else {
            GameServerMessage flop = GameServerMessageUtils.createMessage(GameMessageType.COMMUNITY_CARDS, "%s;%s;%s".formatted(communityCards.get(0), communityCards.get(1), communityCards.get(2)).getBytes());
            sendBroadcastToAll(flop);
            currentStep = currentDiller;
            lastRaiser = -1;

            while (activePlayers.get(currentStep).isFolded() || activePlayers.get(currentStep).isAllInned()) {
                nextTurn();
                if (currentStep == currentDiller) {
                    needToAsk = false;
                    break;
                }
            }
            currentDiller = currentStep;
            if (needToAsk) {
                while (true) {
                    if (activePlayers.get(currentStep).isAllInned() || activePlayers.get(currentStep).isFolded()) {
                        if (currentStep == lastRaiser) {
                            break;
                        }
                        nextTurn();
                        if (lastRaiser == -1 && currentStep == currentDiller) {
                            break;
                        }
                        continue;
                    }
                    if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                        gameProcessing = false;
                        break;
                    }
                    sendMessage(activePlayers.get(currentStep), GameServerMessageUtils.createMessage(GameMessageType.WAITING_FOR_ACTION, new byte[0]));
                    handlePlayerAction();
                    if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                        gameProcessing = false;
                        break;
                    }
                    if (lastRaiser == -1 && currentStep == currentDiller) {
                        break;
                    } else if (lastRaiser == currentStep) {
                        break;
                    }
                }
            }
            if (!gameProcessing) {
                endGame();
            } else {
                GameServerMessage tern = GameServerMessageUtils.createMessage(GameMessageType.COMMUNITY_CARDS, "%s".formatted(communityCards.get(3)).getBytes());
                sendBroadcastToAll(tern);
                currentStep = currentDiller;
                lastRaiser = -1;

                while (activePlayers.get(currentStep).isFolded() || activePlayers.get(currentStep).isAllInned()) {
                    nextTurn();
                    if (currentStep == currentDiller) {
                        needToAsk = false;
                        break;
                    }
                }
                currentDiller = currentStep;
                if (needToAsk) {
                    while (true) {
                        if (activePlayers.get(currentStep).isAllInned() || activePlayers.get(currentStep).isFolded()) {
                            if (currentStep == lastRaiser) {
                                break;
                            }
                            nextTurn();
                            if (lastRaiser == -1 && currentStep == currentDiller) {
                                break;
                            }
                            continue;
                        }
                        if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                            gameProcessing = false;
                            break;
                        }
                        sendMessage(activePlayers.get(currentStep), GameServerMessageUtils.createMessage(GameMessageType.WAITING_FOR_ACTION, new byte[0]));
                        handlePlayerAction();
                        if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                            gameProcessing = false;
                            break;
                        }
                        if (lastRaiser == -1 && currentStep == currentDiller) {
                            break;
                        } else if (lastRaiser == currentStep) {
                            break;
                        }
                    }
                }
                if (!gameProcessing) {
                    endGame();
                } else {
                    GameServerMessage river = GameServerMessageUtils.createMessage(GameMessageType.COMMUNITY_CARDS, "%s".formatted(communityCards.get(4)).getBytes());
                    sendBroadcastToAll(river);
                    currentStep = currentDiller;
                    lastRaiser = -1;

                    while (activePlayers.get(currentStep).isFolded() || activePlayers.get(currentStep).isAllInned()) {
                        nextTurn();
                        if (currentStep == currentDiller) {
                            needToAsk = false;
                            break;
                        }
                    }
                    currentDiller = currentStep;
                    if (needToAsk) {
                        while (true) {
                            if (activePlayers.get(currentStep).isAllInned() || activePlayers.get(currentStep).isFolded()) {
                                if (currentStep == lastRaiser) {
                                    break;
                                }
                                nextTurn();
                                if (lastRaiser == -1 && currentStep == currentDiller) {
                                    break;
                                }
                                continue;
                            }
                            if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                                break;
                            }
                            sendMessage(activePlayers.get(currentStep), GameServerMessageUtils.createMessage(GameMessageType.WAITING_FOR_ACTION, new byte[0]));
                            handlePlayerAction();
                            if (activePlayers.stream().filter(r -> !r.isFolded()).toList().size() == 1) {
                                break;
                            }
                            if (lastRaiser == -1 && currentStep == currentDiller) {
                                break;
                            } else if (lastRaiser == currentStep) {
                                break;
                            }
                        }
                    }
                    endGame();
                }
            }
        }
    }

    public void handlePlayerAction() {
        Player player = activePlayers.get(currentStep);
        try {
            GameServerMessage playerAction = socketServer.readMessage(player.getSocket());
            System.out.println("Прочитал сообщение с типом: " + playerAction.getType());
            GameMessageType actionType = playerAction.getType();
            switch (actionType) {
                case FOLD -> handleFold(player);
                case CHECK -> handleCheck(player);
                case CALL -> handleCall(player);
                case RAISE -> handleRaise(player, Long.parseLong(new String(playerAction.getData())));
                case ALL_IN -> handleAllIn(player);
                default -> {
                    player.setFolded(true);
                    sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "WRONG ACTION! DISCONNECTED!".getBytes()));
                    broadcastPlayerDisconnected(player.getUsername());
                    nextTurn();
                }
            }
            System.out.println("Метод handle отработал!");
        } catch (MessageException | NumberFormatException e) {
            player.setFolded(true);
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "WRONG ACTION! DISCONNECTED!".getBytes()));
            broadcastPlayerDisconnected(player.getUsername());
            nextTurn();
        }

    }

    private void handleFold(Player player) {
        player.setFolded(true);
        sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_FOLDED, player.getUsername().getBytes()), player.getUsername());
        nextTurn();
    }

    private void handleCheck(Player player) {
        if (lastRaiser != -1 && lastRaiser != currentStep) {
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "You can't check if someone made a bet!".getBytes()));
            player.setFolded(true);
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "WRONG ACTION! DISCONNECTED!".getBytes()));
            broadcastPlayerDisconnected(player.getUsername());
            nextTurn();
        } else {
            sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_CHECKED, player.getUsername().getBytes()), player.getUsername());
            nextTurn();
        }
    }

    private void handleCall(Player player) {
        long currentMoney = player.getMoney();
        if (currentMoney < currentBet - player.getCurrentBet()) {
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "You not have enough money!".getBytes()));
            player.setFolded(true);
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "WRONG ACTION! DISCONNECTED!".getBytes()));
            broadcastPlayerDisconnected(player.getUsername());
            nextTurn();
        } else {
            long toSubtract = currentBet - player.getCurrentBet();
            System.out.println("Для вычета: " + toSubtract);
            player.subtractMoney(toSubtract);
            pot += toSubtract;
            player.setCurrentBet(currentBet);
            sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_CALLED, "%s;%d".formatted(player.getUsername(), toSubtract).getBytes()), player.getUsername());
            nextTurn();
        }
    }

    private void handleRaise(Player player, long amount) {
        long currentMoney = player.getMoney();
        if (currentMoney < amount - player.getCurrentBet() || amount < minBet) {
            if (currentMoney < amount - player.getCurrentBet()) {
                sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "You not have enough money!".getBytes()));
            } else {
                sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "Minimum bet is %s!".formatted(minBet).getBytes()));
            }
            player.setFolded(true);
            sendMessage(player, GameServerMessageUtils.createMessage(GameMessageType.ERROR, "WRONG ACTION! DISCONNECTED!".getBytes()));
            broadcastPlayerDisconnected(player.getUsername());
            nextTurn();
        } else {
            long toSubtract = amount - player.getCurrentBet();
            player.subtractMoney(toSubtract);
            currentBet = amount;
            lastRaiser = currentStep;
            pot += toSubtract;
            player.setCurrentBet(amount);
            sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_RAISED, "%s;%d;%d".formatted(player.getUsername(), amount, toSubtract).getBytes()), player.getUsername());
            nextTurn();
        }

    }

    private void handleAllIn(Player player) {
        long bet = player.getDefaultMoney();
        long toSubtract = bet - player.getCurrentBet();
        player.subtractMoney(toSubtract);
        if (currentBet >= bet) {
            pot += toSubtract;
            sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_ALL_INNED, "%s;-1".formatted(player.getUsername()).getBytes()), player.getUsername());
        } else {
            currentBet = toSubtract;
            lastRaiser = currentStep;
            pot += toSubtract;
            sendBroadcast(GameServerMessageUtils.createMessage(GameMessageType.PLAYER_ALL_INNED, "%s;%d".formatted(player.getUsername(), toSubtract).getBytes()), player.getUsername());
        }
        player.setAllInned(true);
        nextTurn();
    }

    private void nextTurn() {
        currentStep = (currentStep + 1) % activePlayers.size();
    }

    private void endGame() {
        StringBuilder gameResult = new StringBuilder();

        System.out.println("game ended");
        if (activePlayers.size() == 1) {
            System.out.println("Остался 1");
            List<Player> winners = new ArrayList<>(activePlayers);
            long winnings = pot;
            winners.forEach(p -> p.addMoney(winnings));
            activePlayers.forEach(p -> p.setDefaultMoney(p.getMoney()));
            for (Player player : winners) {
                gameResult.append(player.getUsername());
                gameResult.append(";");
                gameResult.append("1");
                gameResult.append(";");
                gameResult.append(player.getHand().get(0).toString());
                gameResult.append(";");
                gameResult.append(player.getHand().get(1).toString());
                gameResult.append("\n");
                socketServer.sendRequestToClientServer(ClientServerMessageUtils.createMessage(ClientMessageType.UPDATE_USER_DATA_REQUEST, "%s;%s".formatted(player.getUsername(), player.getDefaultMoney()).getBytes()));
            }
            gameResult.deleteCharAt(gameResult.length() - 1);
            sendBroadcastToAll(GameServerMessageUtils.createMessage(GameMessageType.GAME_END, gameResult.toString().getBytes()));

            activePlayers.forEach(p -> {
                try {
                    p.getSocket().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            List<Player> notFoldedPlayers = activePlayers.stream().filter(r -> !r.isFolded()).toList();

            List<Card> communityCards = this.communityCards;
            Map<Player, HandWorth> handValues = new HashMap<>();

            for (Player player : notFoldedPlayers) {
                List<Card> playerHand = List.of(player.getHand().get(0), player.getHand().get(1));
                List<Card> fullHand = new ArrayList<>(communityCards);
                fullHand.addAll(playerHand);

                handValues.put(player, HandEvaluator.calculateHandValue(fullHand, 7));
            }

            int maxHandValue = handValues.values().stream().mapToInt(HandWorth::value).max().orElse(0);
            List<Player> winners = handValues.entrySet().stream()
                    .filter(entry -> entry.getValue().value() == maxHandValue)
                    .map(Map.Entry::getKey)
                    .toList();

            long winnings = pot / winners.size();
            System.out.println(winnings);
            List<Player> otherPlayers = new ArrayList<>(activePlayers);
            otherPlayers.removeAll(winners);

            winners.forEach(p -> p.addMoney(winnings));
            winners.forEach(p -> System.out.println("Деньги игрока: " + p.getUsername() + " = " + p.getMoney()));
            System.out.println("___________________________________-");
            activePlayers.forEach(p -> p.setDefaultMoney(p.getMoney()));
            activePlayers.forEach(p -> System.out.println("Деньги игрока: " + p.getUsername() + " = " + p.getDefaultMoney()));

            for (Player player : winners) {
                gameResult.append(player.getUsername());
                gameResult.append(";");
                gameResult.append("1");
                gameResult.append(";");
                gameResult.append(player.getHand().get(0).toString());
                gameResult.append(";");
                gameResult.append(player.getHand().get(1).toString());
                gameResult.append("\n");
                ClientServerMessage answer = socketServer.sendRequestToClientServer(ClientServerMessageUtils.createMessage(ClientMessageType.UPDATE_USER_DATA_REQUEST, "%s;%d".formatted(player.getUsername(), player.getDefaultMoney()).getBytes()));
                System.out.println("Значение денег в базе у игрока " + player.getUsername() + " - " + new String(answer.getData()));
            }
            if (!otherPlayers.isEmpty()) {
                for (Player player : otherPlayers) {
                    gameResult.append(player.getUsername());
                    gameResult.append(";");
                    gameResult.append("0");
                    gameResult.append(";");
                    gameResult.append(player.getHand().get(0).toString());
                    gameResult.append(";");
                    gameResult.append(player.getHand().get(1).toString());
                    gameResult.append("\n");
                    ClientServerMessage answer = socketServer.sendRequestToClientServer(ClientServerMessageUtils.createMessage(ClientMessageType.UPDATE_USER_DATA_REQUEST, "%s;%d".formatted(player.getUsername(), player.getDefaultMoney()).getBytes()));
                    System.out.println("Значение денег в базе у игрока " + player.getUsername() + " - " + new String(answer.getData()));
                }
            }
            gameResult.deleteCharAt(gameResult.length() - 1);
            sendBroadcastToAll(GameServerMessageUtils.createMessage(GameMessageType.GAME_END, gameResult.toString().getBytes()));

            activePlayers.forEach(p -> {
                try {
                    p.getSocket().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        room.getManager().removeRoom(room);
    }

}
