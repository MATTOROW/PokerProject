package ru.itis.pokerproject.application;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
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

    private final HBox topPlayers = new HBox(30);
    private final HBox bottomPlayers = new HBox(30);
    private final VBox leftPlayers = new VBox(30);
    private final VBox rightPlayers = new VBox(30);
    private VBox centerContainer;
    private final Button readyButton;


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

        setupUI();

    }

    private void setupUI() {

        topPlayers.setAlignment(Pos.CENTER);
        bottomPlayers.setAlignment(Pos.CENTER);
        leftPlayers.setAlignment(Pos.CENTER_LEFT);
        rightPlayers.setAlignment(Pos.CENTER_RIGHT);

        Ellipse table = new Ellipse(300, 200);
        table.setFill(Color.DARKGREEN);
        StackPane tableContainer = new StackPane(table);
        tableContainer.setPrefSize(600, 400);
        tableContainer.setMaxSize(600, 400);

        centerContainer = new VBox(tableContainer);
        centerContainer.setAlignment(Pos.CENTER);

        this.setCenter(centerContainer);
        this.setTop(topPlayers);
        this.setBottom(bottomPlayers);
        this.setLeft(leftPlayers);
        this.setRight(rightPlayers);

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
}
