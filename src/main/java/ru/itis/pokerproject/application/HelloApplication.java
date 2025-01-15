package ru.itis.pokerproject.application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.CacheHint;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.itis.pokerproject.models.Card;
import ru.itis.pokerproject.services.DeckGenerator;
import ru.itis.pokerproject.services.HandEvaluator;

import java.io.IOException;
import java.util.List;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Задняя сторона карты
        Rectangle back = new Rectangle(100, 150, Color.DARKBLUE);
        Text backText = new Text("BACK");
        backText.setFill(Color.WHITE);
        backText.setFont(Font.font(20));
        StackPane backSide = new StackPane(back, backText);

        // Лицевая сторона карты
        Rectangle front = new Rectangle(100, 150, Color.LIGHTGRAY);
        Text frontText = new Text("ACE ♠");
        frontText.setFill(Color.BLACK);
        frontText.setFont(Font.font(20));
        StackPane frontSide = new StackPane(front, frontText);

        // Контейнер для карты
        StackPane card = new StackPane(backSide, frontSide);
        frontSide.setVisible(false); // Показываем заднюю сторону по умолчанию
        card.setStyle("-fx-background-color: red");
        card.setPrefSize(100, 150);
        card.setMaxSize(100, 150);
        card.setMinSize(100, 150);

        // Добавляем тень
        DropShadow shadow = new DropShadow(10, Color.GRAY);
        shadow.setOffsetY(5);
        shadow.setRadius(10);
        card.setEffect(shadow);
        // Устанавливаем кэширование
        card.setCache(true);
        card.setCacheHint(CacheHint.SPEED);

        // Анимация увеличения карты (эффект приподнятости)
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.5), card);
        scaleUp.setFromX(1);
        scaleUp.setToX(1.1);  // Увеличиваем размер карты по оси X для эффекта приподнятости
        scaleUp.setFromY(1);
        scaleUp.setToY(1.1);

        // Сужение карты
        ScaleTransition flipAnimation = new ScaleTransition(Duration.seconds(0.2), card);
        flipAnimation.setFromX(1.1);
        flipAnimation.setToX(0);// Сжимаем только по оси X

        // Переключение сторон карты после сужения
        flipAnimation.setOnFinished(e -> {
            if (backSide.isVisible()) {
                backSide.setVisible(false);
                frontSide.setVisible(true);
            } else {
                backSide.setVisible(true);
                frontSide.setVisible(false);
            }
        });

        // Восстанавливаем ширину карты
        ScaleTransition flipAnimationEnd = new ScaleTransition(Duration.seconds(0.2), card);
        flipAnimationEnd.setFromX(0);
        flipAnimationEnd.setToX(1.1);

        // Анимация уменьшения карты после переворота
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.5), card);
        scaleDown.setFromX(1.1);
        scaleDown.setToX(1); // Уменьшаем карту обратно до исходного размера
        scaleDown.setFromY(1.1);
        scaleDown.setToY(1);

        // Соединение анимаций
        SequentialTransition fullAnimation = new SequentialTransition(scaleUp, flipAnimation, flipAnimationEnd, scaleDown);
        fullAnimation.setCycleCount(1);

        // Клик для запуска анимации
        card.setOnMouseClicked(e -> {
            if (!fullAnimation.getStatus().equals(Animation.Status.RUNNING)) {
                fullAnimation.play();
            }
        });

        // Настройка сцены
        Scene scene = new Scene(new StackPane(card), 300, 300);
        stage.setScene(scene);
        stage.setTitle("Card Flip Without 3D");
        stage.show();
    }

    public static void main(String[] args) {
        List<Card> cards = DeckGenerator.generateRandomDeck(5);
        for (List<Card> comb: HandEvaluator.generateCardsCombinations(cards)) {
            comb.forEach(System.out::println);
            System.out.println("______________________________");
            System.out.println(HandEvaluator.getHandType(comb));
            System.out.println(HandEvaluator.calculateHandValue(comb));
            System.out.println("******************************");
        }
        launch();
    }
}