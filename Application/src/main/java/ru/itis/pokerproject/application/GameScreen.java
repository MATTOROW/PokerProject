package ru.itis.pokerproject.application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import ru.itis.pokerproject.model.PlayerInfo;

import java.util.List;

public class GameScreen extends BorderPane {

    private final int maxPlayers;
    private final int currentPlayers;
    private final int minBet;
    private final List<PlayerInfo> players;
    private final PlayerInfo myPlayer;

    public GameScreen(int maxPlayers, int currentPlayers, int minBet, List<PlayerInfo> players, PlayerInfo myPlayer) {
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
        this.minBet = minBet;
        this.players = players;
        this.myPlayer = myPlayer;

        setupUI();
    }

    private void setupUI() {
        // Создаём стол (зелёный овал)
        Ellipse table = new Ellipse(300, 200);
        table.setFill(Color.DARKGREEN);
        StackPane tableContainer = new StackPane(table);
        tableContainer.setPrefSize(600, 400);
        tableContainer.setMaxSize(600, 400);

        // Контейнер для игроков
        VBox centerContainer = new VBox(tableContainer);
        centerContainer.setAlignment(Pos.CENTER);
        this.setCenter(centerContainer);

        // Контейнер для игроков сверху
        HBox topPlayers = new HBox(30);
        topPlayers.setAlignment(Pos.CENTER);

        // Контейнер для игроков снизу (включая нашего)
        HBox bottomPlayers = new HBox(30);
        bottomPlayers.setAlignment(Pos.CENTER);

        // Контейнер для боковых игроков
        VBox leftPlayers = new VBox(30);
        leftPlayers.setAlignment(Pos.CENTER_LEFT);

        VBox rightPlayers = new VBox(30);
        rightPlayers.setAlignment(Pos.CENTER_RIGHT);

        // Распределяем игроков вокруг стола
        for (int i = 0; i < players.size(); i++) {
            PlayerInfo player = players.get(i);
            Label playerLabel = createPlayerLabel(player);

            if (player.getUsername().equals(myPlayer.getUsername())) {
                bottomPlayers.getChildren().add(playerLabel);
            } else if (i % 2 == 0) {
                topPlayers.getChildren().add(playerLabel);
            } else if (i % 3 == 0) {
                leftPlayers.getChildren().add(playerLabel);
            } else {
                rightPlayers.getChildren().add(playerLabel);
            }
        }

        this.setTop(topPlayers);
        this.setBottom(bottomPlayers);
        this.setLeft(leftPlayers);
        this.setRight(rightPlayers);
    }

    private Label createPlayerLabel(PlayerInfo player) {
        String status = player.isReady() ? "Готов" : "Не готов";
        Label label = new Label(player.getUsername() + "\n$" + player.getMoney() + "\n[" + status + "]");
        label.setStyle("-fx-border-color: white; -fx-padding: 5; -fx-background-color: black; -fx-text-fill: white;");
        return label;
    }

    public static void show(Stage stage, int maxPlayers, int currentPlayers, int minBet, List<PlayerInfo> players, PlayerInfo myPlayer) {
        GameScreen gameScreen = new GameScreen(maxPlayers, currentPlayers, minBet, players, myPlayer);
        Scene scene = new Scene(gameScreen, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
