package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.itis.pokerproject.service.AuthService;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class LoginScreen {
    private final VBox view;

    public LoginScreen(AuthService authService, ScreenManager manager) {


        Label titleLabel = new Label("Вход в систему");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Зарегистрироваться");


        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);


        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();


            progressIndicator.setVisible(true);
            loginButton.setDisable(true);


            new Thread(() -> {
                try {
                    AccountResponse account = authService.login(username, password);
                    if (account != null) {
                        System.out.println("Успешный вход!");
                        SessionStorage.setSessionData(account.username(), account.money(), account.token()); // Токен пока пустой
                        Platform.runLater(manager::showRoomsScreen);
                    } else {
                        System.out.println("Ошибка входа!");
                    }
                } catch (ClientException e) {
                    e.printStackTrace();
                    manager.showErrorScreen(e.getMessage());
                } finally {

                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        loginButton.setDisable(false);
                    });
                }
            }).start();
        });


        registerButton.setOnAction(event -> {

            manager.showRegisterScreen();
        });


        view = new VBox(10, titleLabel, usernameField, passwordField, loginButton, registerButton, progressIndicator);
        view.setMinWidth(300);
        view.setMinHeight(400);
        view.setAlignment(Pos.CENTER);
    }

    public VBox getView() {
        return view;
    }
}
