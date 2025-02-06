package ru.itis.pokerproject.application;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.service.SendMessageToGameServerService;
import ru.itis.pokerproject.service.SendReadyStatusService;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.ClientException;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends BorderPane {


    private final ScreenManager manager;
    private final SendReadyStatusService sendReadyStatusService;
    private final SendMessageToGameServerService sendMessageToGameServerService;
    private int maxPlayers;
    private int currentPlayers;
    private long minBet;
    private List<PlayerInfo> opponents;
    private PlayerInfo myPlayer;
    private boolean gameStarted = false; // Флаг: игра началась
    private Label potLabel;
    private Label currentBetLabel;
    private Label bankValueLabel;
    private Label currentBetValueLabel;
    private Label notificationLabel;
    private HBox opponentsPane;
    private StackPane centerPane;
    private Ellipse tableShape;         // Графика стола
    private HBox communityCardsBox;     // Контейнер для 5 общих карт
    private List<Label> communityCardLabels; // Метки для общих карт
    private HBox bottomContainer;
    private VBox myPlayerInfoPane;      // Информация о вашем игроке
    private HBox readinessPanel;
    private HBox actionButtonsPane;
    private Button foldButton;
    private Button checkButton;
    private Button callButton;
    private Button raiseButton;
    private Button allInButton;
    private TextField raiseAmountField;
    private Button readyButton;

    public GameScreen(int maxPlayers, int currentPlayers, long minBet, List<PlayerInfo> players, PlayerInfo myPlayer, ScreenManager manager) {
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.minBet = minBet;

        this.opponents = players;
        this.myPlayer = myPlayer;
        this.manager = manager;
        this.sendReadyStatusService = manager.getSendReadyStatusService();
        this.sendMessageToGameServerService = manager.getSendMessageToGameServerService();

        setupLayout();
        updateUI();
    }


    private void setupLayout() {

        this.setStyle("-fx-background-color: darkslategray;");


        opponentsPane = new HBox(20);
        opponentsPane.setAlignment(Pos.CENTER);
        opponentsPane.setPadding(new Insets(10));
        opponentsPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
        this.setTop(opponentsPane);


        centerPane = new StackPane();
        centerPane.setPrefSize(800, 600);


        tableShape = new Ellipse(200, 120);
        tableShape.setFill(Color.DARKGREEN);
        tableShape.setStroke(Color.BLACK);
        tableShape.setStrokeWidth(2);


        communityCardsBox = new HBox(10);
        communityCardsBox.setAlignment(Pos.CENTER);
        communityCardLabels = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Label cardLabel = createHiddenCardLabel();
            communityCardLabels.add(cardLabel);
            communityCardsBox.getChildren().add(cardLabel);
        }


        StackPane tableContainer = new StackPane();
        tableContainer.setPrefSize(400, 300);
        tableContainer.getChildren().addAll(tableShape, communityCardsBox);
        StackPane.setAlignment(communityCardsBox, Pos.CENTER);


        Label bankStaticLabel = new Label("Банк: ");
        bankStaticLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 16px;");
        bankValueLabel = new Label(String.valueOf(Game.getPot()));
        bankValueLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 16px;");
        HBox bankBox = new HBox(5, bankStaticLabel, bankValueLabel);
        bankBox.setAlignment(Pos.CENTER);

        Label currentBetStaticLabel = new Label("Текущая ставка: ");
        currentBetStaticLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 16px;");
        currentBetValueLabel = new Label(String.valueOf(Game.getCurrentBet()));
        currentBetValueLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 16px;");
        HBox currentBetBox = new HBox(5, currentBetStaticLabel, currentBetValueLabel);
        currentBetBox.setAlignment(Pos.CENTER);


        VBox potInfoBox = new VBox(5, bankBox, currentBetBox);
        potInfoBox.setAlignment(Pos.TOP_CENTER);
        potInfoBox.setPadding(new Insets(10));
        potInfoBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        StackPane.setAlignment(potInfoBox, Pos.TOP_CENTER);
        centerPane.getChildren().addAll(tableContainer, potInfoBox);


        notificationLabel = new Label();
        notificationLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-padding: 5px; -fx-font-size: 14px;");
        notificationLabel.setVisible(false);
        StackPane notificationOverlay = new StackPane(notificationLabel);
        notificationOverlay.setAlignment(Pos.TOP_CENTER);
        notificationOverlay.setPadding(new Insets(60, 0, 0, 0));
        notificationOverlay.setMouseTransparent(true);
        centerPane.getChildren().add(notificationOverlay);

        this.setCenter(centerPane);


        myPlayerInfoPane = createMyPlayerInfoPane();
        readyButton = new Button("Готов");
        readyButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        readyButton.setOnAction(e -> handleReadyButtonClick());
        readinessPanel = new HBox(readyButton);
        readinessPanel.setAlignment(Pos.CENTER);
        readinessPanel.setPadding(new Insets(10));

        bottomContainer = new HBox(20, myPlayerInfoPane, readinessPanel);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.setPadding(new Insets(10));
        this.setBottom(bottomContainer);
    }


    private VBox createMyPlayerInfoPane() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5; -fx-border-color: white; -fx-border-width: 1;");


        Label nameLabel = new Label(myPlayer.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");


        Label moneyLabel = new Label();

        moneyLabel.textProperty().bind(myPlayer.moneyProperty().asString("Баланс: %d"));
        moneyLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 14px;");

        box.getChildren().addAll(nameLabel, moneyLabel);


        HBox cardsBox = new HBox(5);
        cardsBox.setAlignment(Pos.CENTER);
        List<Card> myCards = myPlayer.getHand();
        if (myCards != null && !myCards.isEmpty()) {
            for (Card card : myCards) {
                Label cardLabel = createCardLabel(card);
                cardsBox.getChildren().add(cardLabel);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                Label hiddenCard = createHiddenCardLabel();
                cardsBox.getChildren().add(hiddenCard);
            }
        }
        box.getChildren().add(cardsBox);

        return box;
    }


    public void updateUI() {
        updatePotAndBet();
        updateMyPlayerInfo();
        updateOpponentsUI();
    }


    public void updatePotAndBet() {
        bankValueLabel.setText(String.valueOf(Game.getPot()));
        currentBetValueLabel.setText(String.valueOf(Game.getCurrentBet()));
    }


    public void updateMyPlayerInfo() {
        VBox newMyInfo = createMyPlayerInfoPane();
        bottomContainer.getChildren().set(0, newMyInfo);
        myPlayerInfoPane = newMyInfo;
    }


    public void updateOpponentsUI() {
        opponentsPane.getChildren().clear();
        for (PlayerInfo opponent : opponents) {
            VBox opponentBox = createOpponentBox(opponent);
            opponentsPane.getChildren().add(opponentBox);
        }
    }


    private VBox createOpponentBox(PlayerInfo opponent) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5; -fx-border-color: white; -fx-border-width: 1;");

        Label nameLabel = new Label(opponent.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label moneyLabel = new Label("$" + opponent.getMoney());
        moneyLabel.setStyle("-fx-text-fill: gold;");
        box.getChildren().addAll(nameLabel, moneyLabel);

        if (!gameStarted) {
            String statusText = opponent.isReady() ? "Готов" : "Не готов";
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle("-fx-text-fill: lightblue;");
            box.getChildren().add(statusLabel);
        }

        HBox cardsBox = new HBox(5);
        cardsBox.setAlignment(Pos.CENTER);
        List<Card> oppCards = opponent.getHand();
        if (oppCards != null && !oppCards.isEmpty()) {
            for (Card card : oppCards) {
                Label cardLabel = createCardLabel(card);
                cardsBox.getChildren().add(cardLabel);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                Label hiddenCard = createHiddenCardLabel();
                cardsBox.getChildren().add(hiddenCard);
            }
        }
        box.getChildren().add(cardsBox);

        return box;
    }


    private Label createHiddenCardLabel() {
        Label cardLabel = new Label("🂠");
        cardLabel.setMinSize(50, 70);
        cardLabel.setAlignment(Pos.CENTER);
        cardLabel.setStyle("-fx-border-color: white; -fx-background-color: black; -fx-text-fill: white; -fx-font-size: 24px;");
        return cardLabel;
    }


    private Label createCardLabel(Card card) {

        String suitSymbol = card.suit().getSuitSymbol();
        String valueStr = card.value().getValueRepresentation();
        String cardText = valueStr + suitSymbol;  // например, "J♠"

        Label cardLabel = new Label(cardText);
        cardLabel.setMinSize(50, 70);
        cardLabel.setAlignment(Pos.CENTER);


        String backGroundColor;
        String textColor;
        switch (card.suit()) {
            case HEARTS:
            case DIAMONDS:
                backGroundColor = "darkred";
                textColor = "white";
                break;
            case SPADES:
            case CLUBS:
            default:
                backGroundColor = "lightgray";
                textColor = "black";
                break;
        }


        cardLabel.setStyle("-fx-border-color: white; -fx-background-color: " + backGroundColor +
                "; -fx-text-fill: " + textColor + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        return cardLabel;
    }


    private void handleReadyButtonClick() {
        readyButton.setDisable(true);
        try {
            sendReadyStatusService.sendStatus();
        } catch (ClientException e) {
            manager.showErrorScreen(e.getMessage());
        }
    }


    public void startGame() {
        gameStarted = true;
        bottomContainer.getChildren().remove(1); // Удаляем панель готовности
        initializeActionButtons();
        bottomContainer.getChildren().add(actionButtonsPane);
        showWaitingMessage();
        updateOpponentsUI();
    }


    public void initializeActionButtons() {
        foldButton = new Button("FOLD");
        checkButton = new Button("CHECK");
        callButton = new Button("CALL");
        raiseButton = new Button("RAISE");
        allInButton = new Button("ALL IN");
        raiseAmountField = new TextField();
        raiseAmountField.setPromptText("Ставка");


        foldButton.setDisable(true);
        checkButton.setDisable(true);
        callButton.setDisable(true);
        raiseButton.setDisable(true);
        allInButton.setDisable(true);
        raiseAmountField.setDisable(true);


        foldButton.setOnAction(e -> {
            handleFold();
            showWaitingMessage();
        });
        checkButton.setOnAction(e -> {
            handleCheck();
            showWaitingMessage();
        });
        callButton.setOnAction(e -> {
            handleCall();
            showWaitingMessage();
        });
        raiseButton.setOnAction(e -> {
            handleRaise();
            showWaitingMessage();
        });
        allInButton.setOnAction(e -> {
            handleAllIn();
            showWaitingMessage();
        });


        raiseAmountField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().matches("\\d*")) {
                return change;
            }
            return null;
        }));


        raiseAmountField.textProperty().addListener((obs, oldValue, newValue) -> {
            long playerMoney = myPlayer.getMoney();
            long playerBet = myPlayer.getCurrentBet();
            long currentBet = Game.getCurrentBet();
            long minRaise = (currentBet - playerBet) + 1;
            long maxRaise = playerMoney + playerBet - 1;

            if (newValue.isEmpty()) {
                raiseButton.setDisable(true);
                return;
            }
            try {
                long value = Long.parseLong(newValue);
                if (value < minRaise || value > maxRaise) {
                    raiseButton.setDisable(true);
                } else {
                    raiseButton.setDisable(false);
                }
            } catch (NumberFormatException e) {
                raiseButton.setDisable(true);
            }
        });

        actionButtonsPane = new HBox(10, foldButton, checkButton, callButton, raiseAmountField, raiseButton, allInButton);
        actionButtonsPane.setAlignment(Pos.CENTER);
        actionButtonsPane.setPadding(new Insets(10));
    }


    private void handleFold() {
        showNotification("Вы сбросили карты (FOLD)");
        try {
            sendMessageToGameServerService.sendMessage(GameServerMessageUtils.createMessage(GameMessageType.FOLD, new byte[0]));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCheck() {
        showNotification("Вы сделали CHECK");
        try {
            sendMessageToGameServerService.sendMessage(GameServerMessageUtils.createMessage(GameMessageType.CHECK, new byte[0]));
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCall() {
        try {
            sendMessageToGameServerService.sendMessage(GameServerMessageUtils.createMessage(GameMessageType.CALL, new byte[0]));
            myPlayer.subtractMoney(Long.parseLong(currentBetValueLabel.getText()) - myPlayer.getCurrentBet());
            Game.setPot(Game.getPot() + Long.parseLong(currentBetValueLabel.getText()) - myPlayer.getCurrentBet());
            myPlayer.setCurrentBet(Long.parseLong(currentBetValueLabel.getText()));
            updatePotAndBet();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRaise() {
        String amountText = raiseAmountField.getText();
        try {
            long amount = Long.parseLong(amountText);
            showNotification("Вы сделали RAISE: " + amount);
            try {
                sendMessageToGameServerService.sendMessage(GameServerMessageUtils.createMessage(GameMessageType.RAISE, "%s".formatted(amount).getBytes()));
                myPlayer.subtractMoney(amount - myPlayer.getCurrentBet());
                Game.setPot(Game.getPot() + amount - myPlayer.getCurrentBet());
                myPlayer.setCurrentBet(amount);
                Game.setCurrentBet(amount);
                updatePotAndBet();
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        } catch (NumberFormatException e) {
            showNotification("Неверная сумма для RAISE");
        }
    }

    private void handleAllIn() {
        showNotification("Вы сделали ALL IN");
        try {
            sendMessageToGameServerService.sendMessage(GameServerMessageUtils.createMessage(GameMessageType.ALL_IN, new byte[0]));
            long allMoney = myPlayer.getDefaultMoney();
            long toSubtract = allMoney - myPlayer.getCurrentBet();
            System.out.println("Мои первоначальные деньги: " + allMoney);
            System.out.println("Моя текущая ставка: " + myPlayer.getCurrentBet());
            if (toSubtract < Game.getCurrentBet()) {
                Game.setPot(Game.getPot() + toSubtract);
                myPlayer.subtractMoney(toSubtract);
            } else {
                Game.setPot(Game.getPot() + toSubtract);
                Game.setCurrentBet(toSubtract);
                myPlayer.subtractMoney(toSubtract);
            }
            updatePotAndBet();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }


    public void showNotification(String message) {
        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setOpacity(1.0);
            notificationLabel.setVisible(true);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), notificationLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.seconds(2));
            fadeOut.setOnFinished(event -> notificationLabel.setVisible(false));
            fadeOut.play();
        });
    }


    private void showWaitingMessage() {
        Label waitingLabel = new Label("Ждем действий других игроков...");
        waitingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;");
        waitingLabel.setAlignment(Pos.CENTER);
        bottomContainer.getChildren().set(1, waitingLabel);
    }


    public void restoreActionButtons() {
        bottomContainer.getChildren().set(1, actionButtonsPane);
    }


    public void updateCommunityCards() {
        List<Card> cards = Game.getCommunityCards();
        for (int i = 0; i < communityCardLabels.size(); i++) {
            if (i < cards.size() && cards.get(i) != null) {
                Label cardLabel = createCardLabel(cards.get(i));
                communityCardsBox.getChildren().set(i, cardLabel);
                communityCardLabels.set(i, cardLabel);
            } else {
                Label cardLabel = createHiddenCardLabel();
                communityCardsBox.getChildren().set(i, cardLabel);
                communityCardLabels.set(i, cardLabel);
            }
        }
    }


    public ScreenManager getManager() {
        return manager;
    }

    public Button getFoldButton() {
        return foldButton;
    }

    public void setFoldButton(Button foldButton) {
        this.foldButton = foldButton;
    }

    public Button getCheckButton() {
        return checkButton;
    }

    public void setCheckButton(Button checkButton) {
        this.checkButton = checkButton;
    }

    public Button getCallButton() {
        return callButton;
    }

    public void setCallButton(Button callButton) {
        this.callButton = callButton;
    }

    public Button getRaiseButton() {
        return raiseButton;
    }

    public void setRaiseButton(Button raiseButton) {
        this.raiseButton = raiseButton;
    }

    public Button getAllInButton() {
        return allInButton;
    }

    public void setAllInButton(Button allInButton) {
        this.allInButton = allInButton;
    }

    public TextField getRaiseAmountField() {
        return raiseAmountField;
    }

    public void setRaiseAmountField(TextField raiseAmountField) {
        this.raiseAmountField = raiseAmountField;
    }


}
