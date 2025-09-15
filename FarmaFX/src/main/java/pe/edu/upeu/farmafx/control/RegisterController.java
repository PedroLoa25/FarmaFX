package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter.Change;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioImp;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.function.UnaryOperator;

public class RegisterController {

    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private final UsuarioServicioI usuarioServicio = UsuarioServicioImp.getInstance();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        aplicarFiltroDni(dniField);
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

    private void resetRegisterForm() {
        // Mantiene el errorLabel visible con el motivo del fallo
        dniField.clear();
        passwordField.clear();
        confirmField.clear();
        dniField.requestFocus();
    }

    @FXML
    public void crearCuenta() {
        errorLabel.setText("");
        String dni = dniField.getText().trim();
        String pass = passwordField.getText();
        String conf = confirmField.getText();

        if (!ValidacionUtils.isValidDni(dni)) {
            errorLabel.setText("DNI inválido (8 dígitos).");
            resetRegisterForm();
            return;
        }
        if (!pass.equals(conf)) {
            errorLabel.setText("Las contraseñas no coinciden.");
            resetRegisterForm();
            return;
        }

        try {
            Usuario u = usuarioServicio.register(dni, pass);
            new Alert(Alert.AlertType.INFORMATION,
                    "Cuenta creada para DNI: " + u.getDni()).showAndWait();
            cerrar();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            resetRegisterForm();
        }
    }

    @FXML
    public void cancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage st = (Stage) dniField.getScene().getWindow();
        st.close();
    }
}