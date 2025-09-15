package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import pe.edu.upeu.farmafx.FarmaFXApplication;
import pe.edu.upeu.farmafx.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class ClienteMainController {

    @FXML
    private AnchorPane contentPane;

    public void initData(Usuario usuario) {
        onShowPromociones();
    }

    @FXML
    public void onShowPromociones() {
        // ==================================================================
        // RUTA CORREGIDA
        // ==================================================================
        loadView("/fxml/cliente_promociones_view.fxml");
    }

    @FXML
    public void onShowProductos() {
        // ==================================================================
        // RUTA CORREGIDA
        // ==================================================================
        loadView("/fxml/cliente_productos_view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                throw new IOException("No se pudo encontrar el archivo FXML en la ruta del classpath: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            contentPane.getChildren().setAll(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error fatal al cargar la vista.\nCausa: " + e.getMessage()).showAndWait();
        }
    }

    @FXML public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            FarmaFXApplication.getInstance().showLoginScreen();
        }
    }

    @FXML public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 2.0\nDesarrollado por: PedroLoa25").showAndWait();
    }

    @FXML
    public void onExit(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Salir");
        confirm.setHeaderText("¿Deseas salir de la aplicación?");
        confirm.setContentText("Se cerrará la aplicación.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
}