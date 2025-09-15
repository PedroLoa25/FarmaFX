package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioImp;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.function.Consumer;

public class LoginController {

    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UsuarioServicioI usuarioServicio = UsuarioServicioImp.getInstance();

    // Hook: MainController decide adónde ir según el rol
    private Consumer<Usuario> onLoginSuccess;
    public void setOnLoginSuccess(Consumer<Usuario> c) { this.onLoginSuccess = c; }

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
            if (onLoginSuccess != null) onLoginSuccess.accept(u);
        } catch (UsuarioServicioImp.ErrorCredenciales e) {
            errorLabel.setText(e.getMessage());
            resetLoginForm();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            resetLoginForm();
        }
    }

    @FXML
    public void abrirRegistro(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Registro no implementado en este ejemplo.").showAndWait();
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