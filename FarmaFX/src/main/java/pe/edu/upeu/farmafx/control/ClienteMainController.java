package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import pe.edu.upeu.farmafx.modelo.Usuario;

import java.io.IOException;
import java.util.Optional;

public class ClienteMainController {

    @FXML private BorderPane root;
    @FXML private Label dniLabel;
    @FXML private Label rolLabel;

    private Usuario usuario;
    private javafx.scene.Node homeContent;

    public void setUsuario(Usuario u) {
        this.usuario = u;
        if (dniLabel != null) dniLabel.setText(u.getDni());
        if (rolLabel != null) rolLabel.setText(String.valueOf(u.getRol()));
    }

    @FXML
    public void initialize() {
        if (root != null) homeContent = root.getCenter();
    }

    @FXML
    public void onExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Se cerrará la aplicación. ¿Continuar?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) Platform.exit();
    }

    @FXML
    public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 0.1\nDesarrollado por: PedroLoa25").showAndWait();
    }

    @FXML
    public void onOpenCategorias() {
        loadInCenter("/fxml/categorias.fxml");
    }

    @FXML
    public void onOpenMarcas() {
        loadInCenter("/fxml/marcas.fxml");
    }

    @FXML
    public void onOpenProductos() {
        loadInCenter("/fxml/productos.fxml");
    }

    private void loadInCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            Object ctl = loader.getController();
            if (ctl instanceof SupportsClose sc) {
                sc.setOnClose(() -> root.setCenter(homeContent));
            }
            root.setCenter(view);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista:\n" + e.getMessage()).showAndWait();
        }
    }

    private Runnable onLogoutAction = () -> {};
    public void setOnLogoutAction(Runnable action) { this.onLogoutAction = action; }

    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            onLogoutAction.run(); // Llama a la acción
        }
    }
}