package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pe.edu.upeu.farmafx.FarmaFXApplication;

import java.util.Optional;

public class AdminMainController {

    @FXML private BorderPane root;

    private javafx.scene.Node homeContent;

    @FXML
    public void initialize() {
        if (root != null) homeContent = root.getCenter();
    }

    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            FarmaFXApplication.getInstance().showLoginScreen();
        }
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

    @FXML
    public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 0.1\nDesarrollado por: PedroLoa25").showAndWait();
    }

    @FXML public void onOpenCategorias() { loadInCenter("/fxml/categorias.fxml"); }
    @FXML public void onOpenMarcas() { loadInCenter("/fxml/marcas.fxml"); }
    @FXML public void onOpenProductos() { loadInCenter("/fxml/productos.fxml"); }
    @FXML public void onOpenPromociones() { loadInCenter("/fxml/promociones.fxml");}

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
            e.printStackTrace();
        }
    }
}