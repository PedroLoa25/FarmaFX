package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.modelo.Usuario;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML private BorderPane root;   // fx:id del BorderPane raíz en main.fxml
    @FXML private Label dniLabel;
    @FXML private Label rolLabel;
    @FXML private Menu menuCatalogos;

    private Usuario usuario;
    private Node homeContent; // contenido inicial para poder volver con "Salir"

    public void setUsuario(Usuario u) {
        this.usuario = u;
        if (u != null) {
            if (dniLabel != null) dniLabel.setText(u.getDni());
            if (rolLabel != null) rolLabel.setText(String.valueOf(u.getRol()));
            boolean isAdmin = u.getRol() != null && "ADMIN".equalsIgnoreCase(u.getRol().name());
            if (menuCatalogos != null) menuCatalogos.setDisable(!isAdmin);
        }
    }

    @FXML
    public void initialize() {
        if (root != null) homeContent = root.getCenter(); // guarda el centro por defecto
        if (usuario == null) {
            if (dniLabel != null) dniLabel.setText("-");
            if (rolLabel != null) rolLabel.setText("-");
        }
    }

    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar sesión");
        confirm.setHeaderText("¿Deseas cerrar sesión?");
        confirm.setContentText("Volverás a la pantalla de inicio de sesión.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent rootLogin = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("FarmaFX - Login");
                loginStage.setScene(new Scene(rootLogin));
                loginStage.setResizable(false);
                loginStage.show();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "No se pudo reabrir Login: " + e.getMessage()).showAndWait();
            }
            Stage stage = (Stage) dniLabel.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void onExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Salir");
        confirm.setHeaderText("¿Deseas salir de la aplicación?");
        confirm.setContentText("Se cerrará la aplicación.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    @FXML
    public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION,
                "FarmaFX\nVersión 0.1\nDesarrollado por: PedroLoa25\n" +
                        "Demo con JavaFX (Login, Categorías y Marcas).").showAndWait();
    }

    @FXML
    public void onOpenCategorias() {
        loadInCenter("/fxml/categorias.fxml");
    }

    @FXML
    public void onOpenMarcas() {
        loadInCenter("/fxml/marcas.fxml");
    }

    private void loadInCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SupportsClose sc) {
                sc.setOnClose(() -> root.setCenter(homeContent));
            }

            root.setCenter(view);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista:\n" + e.getMessage()).showAndWait();
        }
    }
}