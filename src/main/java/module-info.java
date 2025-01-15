module ru.itis.pokerproject.pokerproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;

    exports ru.itis.pokerproject.application;
    opens ru.itis.pokerproject.application to javafx.fxml;
}