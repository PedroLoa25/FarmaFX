package pe.edu.upeu.farmafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.control.AdminMainController;
import pe.edu.upeu.farmafx.control.ClienteMainController;
import pe.edu.upeu.farmafx.control.LoginController;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;

import java.io.IOException;
import java.util.Optional;

public class FarmaFXApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Configuración del cierre de la aplicación (botón X de la ventana)
        primaryStage.setOnCloseRequest(evt -> {
            evt.consume(); // Prevenimos el cierre inmediato
            confirmAndExit();
        });

        showLoginScreen();
    }

    /**
     * Muestra la pantalla de login.
     */
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Pasamos un "callback" al controlador de login.
            // Cuando el login sea exitoso, se ejecutará this::showMainScreen.
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

    /**
     * Muestra la pantalla principal (Admin o Cliente) según el rol del usuario.
     * @param usuario El usuario que ha iniciado sesión.
     */
    public void showMainScreen(Usuario usuario) {
        try {
            String fxmlFile = usuario.getRol() == RolUsuario.ADMIN
                    ? "/fxml/main_admin.fxml"
                    : "/fxml/main_cliente.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pasamos un "callback" al controlador principal.
            // Cuando se cierre sesión, se ejecutará this::showLoginScreen.
            Object controller = loader.getController();
            if (controller instanceof AdminMainController amc) {
                amc.initData(usuario, this::showLoginScreen, this::confirmAndExit);
            } else if (controller instanceof ClienteMainController cmc) {
                cmc.initData(usuario, this::showLoginScreen, this::confirmAndExit);
            }

            primaryStage.setTitle("FarmaFX - " + usuario.getRol());
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            showError("No se pudo cargar la pantalla principal", e);
        }
    }

    /**
     * Muestra una alerta de confirmación y cierra la aplicación si el usuario acepta.
     */
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

    /**
     * Muestra un diálogo de error genérico.
     */
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