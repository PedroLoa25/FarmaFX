package pe.edu.upeu.farmafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.control.LoginController;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import java.io.IOException;
import java.util.Optional;

public class FarmaFXApplication extends Application {

    private static FarmaFXApplication instance;
    private Stage primaryStage;

    public FarmaFXApplication() {
        instance = this;
    }
    public static FarmaFXApplication getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setOnCloseRequest(evt -> {
            evt.consume();
            confirmAndExit();
        });
        showLoginScreen();
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setOnLoginSuccess(this::showMainScreen);

            primaryStage.setTitle("FarmaFX - Login");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.setMaximized(false);
            primaryStage.show();
            primaryStage.sizeToScene(); // Ajusta el tamaño al contenido
            primaryStage.centerOnScreen(); // Centra la ventana

        } catch (IOException e) {
            showError("No se pudo cargar la pantalla de login", e);
        }
    }

    public void showMainScreen(Usuario usuario) {
        try {
            String fxmlFile = usuario.getRol() == RolUsuario.ADMIN
                    ? "/fxml/main_admin.fxml"
                    : "/fxml/main_cliente.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            primaryStage.setTitle("FarmaFX - " + usuario.getRol());
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            showError("No se pudo cargar la pantalla principal", e);
        }
    }

    private void confirmAndExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Salir");
        confirm.setHeaderText("¿Deseas salir de la aplicación?");
        confirm.setContentText("Se cerrará la aplicación.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        e.printStackTrace();
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}