package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final String DB_URL = "jdbc:sqlite:pokeapp.db";

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Todos los campos son obligatorios.");
            return;
        }

        if (registerUser(name, username, email, password)) {
            showAlert("Éxito", "Usuario registrado correctamente.");
            loadLogin();
        } else {
            showAlert("Error", "El usuario ya existe o hubo un problema.");
        }
    }

    private boolean registerUser(String name, String username, String email, String password) {
        String query = "INSERT INTO usuario (name, username, email, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, password);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            showAlert("Error", "No se pudo registrar el usuario.");
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleBack() {
        try {
            // Cargar el archivo FXML de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana) para Login
            Stage loginStage = new Stage();

            // Obtener la ventana actual (Register)
            Window currentWindow = nameField.getScene().getWindow();
            Stage currentStage = (Stage) currentWindow;

            // Pasar el estado de maximizado a la nueva ventana
            loginStage.setMaximized(currentStage.isMaximized());

            // Pasar la posición de la ventana actual a la nueva ventana
            loginStage.setX(currentStage.getX());
            loginStage.setY(currentStage.getY());

            // Configurar la nueva ventana
            loginStage.setTitle("PokeApp - Login");
            loginStage.setScene(scene);

            // Mostrar la nueva ventana
            loginStage.show();

            // Cerrar la ventana actual (Register)
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PokeApp - Inicio de Sesión");
            stage.setMaximized(true);
        } catch (Exception e) {
            showAlert("Error", "No se pudo cargar la pantalla de login.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
