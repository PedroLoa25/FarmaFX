package pe.edu.upeu.farmafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.control.MainController;

import java.util.Optional;

public class FarmaFXApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // El MainController se encargará de mostrar el login primero
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("FarmaFX");
        stage.setScene(scene);
        // --- INICIO DE LA MODIFICACIÓN ---
        // Ya no ponemos el tamaño aquí. El controlador lo manejará.

        // Obtenemos una referencia al controlador principal
        MainController mainController = loader.getController();
        // Le pasamos el control para que inicie la lógica de vistas
        mainController.postInit();
        // --- FIN DE LA MODIFICACIÓN ---

        // --- INICIO DE LA MODIFICACIÓN ---
        // Ya no queremos que la ventana principal arranque maximizada.
        // La haremos de tamaño fijo para que se ajuste al login.
        stage.setResizable(false);
        // stage.setMaximized(true); // <-- LÍNEA ELIMINADA O COMENTADA
        // --- FIN DE LA MODIFICACIÓN ---

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