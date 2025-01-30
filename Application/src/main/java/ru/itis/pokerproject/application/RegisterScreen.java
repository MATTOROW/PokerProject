package ru.itis.pokerproject.application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.itis.pokerproject.shared.template.client.ClientException;
import ru.itis.pokerproject.service.AuthService;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;

public class RegisterScreen {
    private final VBox view;
    private final AuthService authService;
    private final Stage primaryStage;
    private final ConnectionErrorHandler errorHandler;

    public RegisterScreen(AuthService authService, Stage primaryStage, ConnectionErrorHandler errorHandler) {
        this.authService = authService;
        this.primaryStage = primaryStage;
        this.errorHandler = errorHandler;

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
                    AccountResponse account = authService.register(username, password);
                    if (account != null) {
                        System.out.println("Успешная регистрация!");
                        // Возврат на экран логина
                        javafx.application.Platform.runLater(() -> {
                            LoginScreen loginScreen = new LoginScreen(authService, primaryStage, errorHandler);
                            primaryStage.getScene().setRoot(loginScreen.getView());
                        });
                    } else {
                        System.out.println("Ошибка регистрации!");
                    }
                } catch (ClientException e) {
                    errorHandler.showConnectionErrorDialog(primaryStage);
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
            LoginScreen loginScreen = new LoginScreen(authService, primaryStage, errorHandler);
            primaryStage.getScene().setRoot(loginScreen.getView());
        });

        // Создаем layout
        view = new VBox(10, titleLabel, usernameField, passwordField, confirmPasswordField, registerButton, backButton, progressIndicator);
        view.setAlignment(Pos.CENTER);
    }

    public VBox getView() {
        return view;
    }
}
