package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itis.pokerproject.shared.template.client.ClientException;
import ru.itis.pokerproject.service.AuthService;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;

public class RegisterScreen {
    private final VBox view;

    public RegisterScreen(AuthService authService, ScreenManager manager) {

        // Создаем элементы интерфейса
        Label titleLabel = new Label("Регистрация");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");

        // Анимация ожидания
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        // Обработка нажатия на кнопку "Зарегистрироваться"
        registerButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {
                System.out.println("Пароли не совпадают!");
                return;
            }

            // Показываем анимацию ожидания
            progressIndicator.setVisible(true);
            registerButton.setDisable(true);

            // Запускаем задачу в отдельном потоке
            new Thread(() -> {
                try {
                    boolean registered = authService.register(username, password);
                    System.out.println("Успешная регистрация!");
                    // Возврат на экран логина
                    Platform.runLater(manager::showLoginScreen);
                } catch (ClientException e) {
                    manager.showErrorScreen(e.getMessage());
                } finally {
                    // Скрываем анимацию и активируем кнопку
                    javafx.application.Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        registerButton.setDisable(false);
                    });
                }
            }).start();
        });

        // Обработка нажатия на кнопку "Назад"
        backButton.setOnAction(event -> {
            // Возврат на экран логина
            manager.showLoginScreen();
        });

        // Создаем layout
        view = new VBox(10, titleLabel, usernameField, passwordField, confirmPasswordField, registerButton, backButton, progressIndicator);
        view.setMinWidth(300);
        view.setMinHeight(400);
        view.setAlignment(Pos.CENTER);
    }

    public VBox getView() {
        return view;
    }
}
