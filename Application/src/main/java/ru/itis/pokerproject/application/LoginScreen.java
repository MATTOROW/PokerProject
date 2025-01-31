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

public class LoginScreen {
    private final VBox view;

    public LoginScreen(AuthService authService, ScreenManager manager) {

        // Создаем элементы интерфейса
        Label titleLabel = new Label("Вход в систему");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Зарегистрироваться");

        // Анимация ожидания
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        // Обработка нажатия на кнопку "Войти"
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Показываем анимацию ожидания
            progressIndicator.setVisible(true);
            loginButton.setDisable(true);

            // Запускаем задачу в отдельном потоке
            new Thread(() -> {
                try {
                    AccountResponse account = authService.login(username, password);
                    if (account != null) {
                        // Переход на главный экран (можно добавить позже)
                        System.out.println("Успешный вход!");
                        Platform.runLater(manager::showRoomsScreen);
                    } else {
                        System.out.println("Ошибка входа!");
                    }
                } catch (ClientException e) {
                    e.printStackTrace();
                    Platform.runLater(
                            () -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.initModality(Modality.APPLICATION_MODAL);
                                alert.setTitle("Ошибка!");
                                alert.setContentText(e.getMessage());
                                alert.show();
                            }
                    );
                } finally {
                    // Скрываем анимацию и активируем кнопку
                    javafx.application.Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        loginButton.setDisable(false);
                    });
                }
            }).start();
        });

        // Обработка нажатия на кнопку "Зарегистрироваться"
        registerButton.setOnAction(event -> {
            // Переход на экран регистрации
            manager.showRegisterScreen();
        });

        // Создаем layout
        view = new VBox(10, titleLabel, usernameField, passwordField, loginButton, registerButton, progressIndicator);
        view.setMinWidth(300);
        view.setMinHeight(400);
        view.setAlignment(Pos.CENTER);
    }

    public VBox getView() {
        return view;
    }
}
