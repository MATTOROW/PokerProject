package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.itis.pokerproject.service.AuthService;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class RegisterScreen {
    private final VBox view;

    public RegisterScreen(AuthService authService, ScreenManager manager) {


        Label titleLabel = new Label("Регистрация");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");


        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);


        registerButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {
                System.out.println("Пароли не совпадают!");
                return;
            }


            progressIndicator.setVisible(true);
            registerButton.setDisable(true);


            new Thread(() -> {
                try {
                    boolean registered = authService.register(username, password);
                    System.out.println("Успешная регистрация!");

                    Platform.runLater(manager::showLoginScreen);
                } catch (ClientException e) {
                    manager.showErrorScreen(e.getMessage());
                } finally {

                    javafx.application.Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        registerButton.setDisable(false);
                    });
                }
            }).start();
        });


        backButton.setOnAction(event -> {

            manager.showLoginScreen();
        });


        view = new VBox(10, titleLabel, usernameField, passwordField, confirmPasswordField, registerButton, backButton, progressIndicator);
        view.setMinWidth(300);
        view.setMinHeight(400);
        view.setAlignment(Pos.CENTER);
    }

    public VBox getView() {
        return view;
    }
}
