package pe.edu.upeu.farmafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class FarmaFXApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("FarmaFX - Login");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(evt -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Salir");
            confirm.setHeaderText("¿Deseas salir de la aplicación?");
            confirm.setContentText("Se cerrará la aplicación.");
            Optional<ButtonType> res = confirm.showAndWait();
            if (!(res.isPresent() && res.get() == ButtonType.OK)) {
                evt.consume();
            } else {
                Platform.exit();
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}