package ru.itis.pokerproject.application;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.service.SendReadyStatusService;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.template.client.ClientException;

import java.util.List;

public class GameScreen extends BorderPane {

    private int maxPlayers;
    private int currentPlayers;
    private long minBet;
    private List<PlayerInfo> players;
    private PlayerInfo myPlayer;
    private final ScreenManager manager;
    private long pot;
    private long currentBet;


    private Label potLabel;
    private Label currentBetLabel;
    private final Label actionNotification;
    private final HBox topPlayers = new HBox(30);
    private final HBox bottomPlayers = new HBox(30);
    private final VBox leftPlayers = new VBox(30);
    private final VBox rightPlayers = new VBox(30);
    private VBox centerContainer;
    private final Button readyButton;
    private Button foldButton;
    private Button checkButton;
    private Button raiseButton;
    private Button allInButton;
    private Button callButton;
    private TextField raiseAmountField;
    private HBox actionButtonsBox;


    private final SendReadyStatusService sendReadyStatusService;

    public GameScreen(int maxPlayers, int currentPlayers, long minBet, List<PlayerInfo> players, PlayerInfo myPlayer, ScreenManager manager) {
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.minBet = minBet;
        this.players = players;
        this.myPlayer = myPlayer;
        this.manager = manager;

        this.sendReadyStatusService = manager.getSendReadyStatusService();

        readyButton = new Button("Готов");
        readyButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        readyButton.setOnAction(e -> handleReadyButtonClick());

        this.actionNotification = new Label();
        actionNotification.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 14px;");
        actionNotification.setVisible(false);

        this.getChildren().add(actionNotification);

        setupUI();

    }

    private void setupUI() {

        topPlayers.setAlignment(Pos.CENTER);
        bottomPlayers.setAlignment(Pos.CENTER);
        leftPlayers.setAlignment(Pos.CENTER_LEFT);
        rightPlayers.setAlignment(Pos.CENTER_RIGHT);

        this.potLabel = new Label();
        this.currentBetLabel = new Label();

        Ellipse table = new Ellipse(300, 200);
        table.setFill(Color.DARKGREEN);
        StackPane tableContainer = new StackPane(table);
        tableContainer.getChildren().addAll(potLabel, currentBetLabel);
        tableContainer.setPrefSize(600, 400);
        tableContainer.setMaxSize(600, 400);

        centerContainer = new VBox(tableContainer);
        centerContainer.setAlignment(Pos.CENTER);

        this.setCenter(centerContainer);
        this.setTop(topPlayers);
        this.setBottom(bottomPlayers);
        this.setLeft(leftPlayers);
        this.setRight(rightPlayers);

        this.potLabel = new Label();
        this.currentBetLabel = new Label();

        updateUI();
    }

    public void updateUI() {
        topPlayers.getChildren().clear();
        bottomPlayers.getChildren().clear();
        leftPlayers.getChildren().clear();
        rightPlayers.getChildren().clear();

        if (myPlayer != null) {
            VBox myPlayerBox = new VBox(10);
            myPlayerBox.setAlignment(Pos.CENTER);
            myPlayerBox.getChildren().add(createPlayerLabel(myPlayer));

            HBox myCardsBox = new HBox(10);
            if (myPlayer.getHand() != null) {
                for (Card card : myPlayer.getHand()) {
                    myCardsBox.getChildren().add(createCardLabel(card));
                }
            } else {
                myCardsBox.getChildren().addAll(createHiddenCardLabel(), createHiddenCardLabel());
            }
            myPlayerBox.getChildren().add(myCardsBox);

            myPlayerBox.getChildren().add(readyButton);
            if (myPlayer.isReady()) {
                readyButton.setDisable(true);
            }
            bottomPlayers.getChildren().add(myPlayerBox);
        }

        for (int i = 0; i < players.size(); i++) {
            PlayerInfo player = players.get(i);
            VBox playerBox = new VBox(10);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.getChildren().add(createPlayerLabel(player));

            HBox hiddenCardsBox = new HBox(10);
            hiddenCardsBox.getChildren().addAll(createHiddenCardLabel(), createHiddenCardLabel());
            playerBox.getChildren().add(hiddenCardsBox);

            if (i % 2 == 0) {
                topPlayers.getChildren().add(playerBox);
            } else if (i % 3 == 0) {
                leftPlayers.getChildren().add(playerBox);
            } else {
                rightPlayers.getChildren().add(playerBox);
            }
        }

        updatePotAndBet();
    }

    private Label createPlayerLabel(PlayerInfo player) {
        String status = player.isReady() ? "Готов" : "Не готов";
        Label label = new Label(player.getUsername() + "\n$" + player.getMoney() + "\n[" + status + "]");
        label.setStyle("-fx-border-color: white; -fx-padding: 5; -fx-background-color: black; -fx-text-fill: white;");
        return label;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    public long getMinBet() {
        return minBet;
    }

    public void setMinBet(long minBet) {
        this.minBet = minBet;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }

    public PlayerInfo getMyPlayer() {
        return myPlayer;
    }

    public void setMyPlayer(PlayerInfo myPlayer) {
        this.myPlayer = myPlayer;
    }

    public ScreenManager getManager() {
        return manager;
    }

    private void handleReadyButtonClick() {
        readyButton.setDisable(true);
        try {
            sendReadyStatusService.sendStatus();
        } catch (ClientException e) {
            manager.showErrorScreen(e.getMessage());
        }
    }

    private Label createCardLabel(Card card) {
        String cardText = card.suit() + " " + card.value();
        Label cardLabel = new Label(cardText);
        cardLabel.setStyle("-fx-border-color: white; -fx-padding: 5; -fx-background-color: darkred; -fx-text-fill: white;");
        return cardLabel;
    }

    private Label createHiddenCardLabel() {
        Label cardLabel = new Label("🂠");
        cardLabel.setStyle("-fx-border-color: white; -fx-padding: 5; -fx-background-color: black; -fx-text-fill: white;");
        return cardLabel;
    }

    public void updatePotAndBet() {
        potLabel.setText("Банк: " + Game.getPot());
        currentBetLabel.setText("Текущая ставка: " + Game.getCurrentBet());
    }

    public void initializeActionButtons() {
        foldButton = new Button("FOLD");
        checkButton = new Button("CHECK");
        raiseButton = new Button("RAISE");
        allInButton = new Button("ALL IN");
        callButton = new Button("CALL");
        raiseAmountField = new TextField();
        raiseAmountField.setPromptText("Bet amount");

        foldButton.setDisable(true);
        checkButton.setDisable(true);
        raiseButton.setDisable(true);
        allInButton.setDisable(true);
        callButton.setDisable(true);
        raiseAmountField.setDisable(true);

//        foldButton.setOnAction(e -> handleFold());
//        checkButton.setOnAction(e -> handleCheck());
//        raiseButton.setOnAction(e -> handleRaise());
//        allInButton.setOnAction(e -> handleAllIn());

        actionButtonsBox = new HBox(10, foldButton, checkButton, raiseAmountField, raiseButton, allInButton);
        actionButtonsBox.setAlignment(Pos.CENTER);

        this.setBottom(actionButtonsBox);
    }

    public void enableActionButtons() {
        foldButton.setDisable(false);
        checkButton.setDisable(false);
        raiseButton.setDisable(false);
        allInButton.setDisable(false);
        raiseAmountField.setDisable(false);
    }

    public void showPlayerAction(String message) {
        Platform.runLater(() -> {
            actionNotification.setText(message);
            actionNotification.setVisible(true);

            // Анимация исчезновения через 2 секунды
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), actionNotification);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.seconds(2));
            fadeOut.setOnFinished(event -> actionNotification.setVisible(false));

            fadeOut.play();
        });
    }

    public TextField getRaiseAmountField() {
        return this.raiseAmountField;
    }

    public Button getAllInButton() {
        return allInButton;
    }

    public Button getRaiseButton() {
        return raiseButton;
    }

    public Button getCheckButton() {
        return checkButton;
    }

    public Button getFoldButton() {
        return foldButton;
    }

    public Button getCallButton() {
        return callButton;
    }
}
