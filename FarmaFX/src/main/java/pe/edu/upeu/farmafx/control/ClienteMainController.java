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
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent rootLogin = loader.load();
                Stage st = new Stage();
                st.setTitle("FarmaFX - Login");
                st.setScene(new Scene(rootLogin));
                st.setResizable(false);
                st.show();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "No se pudo abrir Login: " + e.getMessage()).showAndWait();
            }
            ((Stage) root.getScene().getWindow()).close();
        }
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

    // Catálogos en modo estándar por ahora; luego podemos poner solo lectura si lo prefieres
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
}