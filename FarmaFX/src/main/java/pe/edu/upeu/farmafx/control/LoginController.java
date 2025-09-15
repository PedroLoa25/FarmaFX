package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioImp;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class LoginController {

    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable r) {
        this.onLoginSuccess = r;
    }

    @FXML
    private void onIngresar() {
        String u = userField.getText()==null ? "" : userField.getText().trim();
        String p = passField.getText()==null ? "" : passField.getText().trim();

        // TODO: valida contra tu servicio real
        if ("admin".equals(u) && "admin".equals(p)) {
            if (onLoginSuccess != null) onLoginSuccess.run();
        } else {
            new Alert(Alert.AlertType.ERROR, "Credenciales inválidas").showAndWait();
        }
    }

    private final UsuarioServicioI usuarioServicio = UsuarioServicioImp.getInstance();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        aplicarFiltroDni(dniField);
        loginButton.setDisable(true);
        dniField.textProperty().addListener((obs, o, n) -> validarCampos());
        passwordField.textProperty().addListener((obs, o, n) -> validarCampos());
    }

    private void aplicarFiltroDni(TextField field) {
        UnaryOperator<Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,8}")) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private void validarCampos() {
        boolean ok = !dniField.getText().trim().isEmpty()
                && !passwordField.getText().isEmpty();
        loginButton.setDisable(!ok);
    }

    private void resetLoginForm() {
        dniField.clear();
        passwordField.clear();
        loginButton.setDisable(true);
        dniField.requestFocus();
    }

    @FXML
    public void ingresar(ActionEvent event) {
        errorLabel.setText("");
        String dni = dniField.getText().trim();
        String pass = passwordField.getText();

        if (!ValidacionUtils.isValidDni(dni)) {
            errorLabel.setText("Ingresa un DNI válido (8 dígitos).");
            resetLoginForm();
            return;
        }
        if (pass.length() < 8) {
            errorLabel.setText("La contraseña debe tener al menos 8 caracteres.");
            resetLoginForm();
            return;
        }

        try {
            Usuario u = usuarioServicio.authenticate(dni, pass);
            // Abrir ventana principal
            abrirMain(u);
            // Cerrar login
            Stage loginStage = (Stage) dniField.getScene().getWindow();
            loginStage.close();
        } catch (UsuarioServicioImp.ErrorCredenciales e) {
            errorLabel.setText(e.getMessage());
            resetLoginForm();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            resetLoginForm();
        }
    }

    private void abrirMain(Usuario u) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setUsuario(u);

        Stage stage = new Stage();
        stage.setTitle("FarmaFX - Principal");
        stage.setScene(new Scene(root));
        stage.setResizable(true);
        stage.setMaximized(true);

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

    @FXML
    public void abrirRegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Crear cuenta - FarmaFX");
            dialog.initModality(Modality.WINDOW_MODAL);
            Stage owner = (Stage) dniField.getScene().getWindow();
            dialog.initOwner(owner);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir registro: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void salir(ActionEvent event) {
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