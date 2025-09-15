package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // <--- AÑADIR esta importación
import javafx.scene.Parent;    // <--- AÑADIR esta importación
import javafx.scene.Scene;      // <--- AÑADIR esta importación
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.Modality;  // <--- AÑADIR esta importación
import javafx.stage.Stage;      // <--- AÑADIR esta importación
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioImp;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.io.IOException; // <--- AÑADIR esta importación
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
        // ---- INICIO DEL CÓDIGO CORREGIDO ----
        try {
            // 1. Cargar el FXML de la ventana de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            // 2. Crear una nueva ventana (Stage) para el diálogo de registro
            Stage stage = new Stage();
            stage.setTitle("Crear Nueva Cuenta");
            stage.setScene(new Scene(root));

            // 3. (Opcional pero recomendado) Hacer que la ventana de login se bloquee hasta que se cierre la de registro
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage loginStage = (Stage) dniField.getScene().getWindow();
            stage.initOwner(loginStage);

            // 4. Mostrar la ventana y esperar a que el usuario la cierre
            stage.showAndWait();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de registro: " + e.getMessage()).show();
            e.printStackTrace();
        }
        // ---- FIN DEL CÓDIGO CORREGIDO ----
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