package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.Parent;
import utils.BBDDUtils; // Importar la clase utils
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterController {

   @FXML
   private TextField nameField;

   @FXML
   private TextField usernameField;

   @FXML
   private TextField emailField;

   @FXML
   private PasswordField passwordField;

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

      if (!isValidName(name)) {
         showAlert("Error", "El nombre solo puede contener letras (mayúsculas y minúsculas).");
         return;
      }

      if (!isValidUsername(username)) {
         showAlert("Error",
               "El nombre de usuario solo puede contener letras, números o la barra baja (_) y debe tener al menos 4 caracteres.");
         return;
      }

      if (!isValidEmail(email)) {
         showAlert("Error", "El correo electrónico no es válido.");
         return;
      }

      if (!isValidPassword(password)) {
         showAlert("Error",
               "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial.");
         return;
      }

      // Verificar si el nombre de usuario ya está registrado
      if (isUsernameTaken(username)) {
         showAlert("Error", "El nombre de usuario ya está registrado.");
         return;
      }

      // Verificar si el correo electrónico ya está registrado
      if (isEmailTaken(email)) {
         showAlert("Error", "El correo electrónico ya está registrado.");
         return;
      }

      // Si todo está bien, registrar al usuario
      if (registerUser(name, username, email, password)) {
         showAlert("Éxito", "Usuario registrado correctamente.");
      } else {
         showAlert("Error", "Hubo un problema al registrar el usuario.");
      }
   }

   private boolean isValidName(String name) {
      // Solo letras (mayúsculas y minúsculas)
      return name.matches("[a-zA-Z]+");
   }

   private boolean isValidUsername(String username) {
      // Letras, números o barra baja (_) y al menos 4 caracteres
      return username.matches("^[a-zA-Z0-9_]{4,}$");
   }

   private boolean isValidEmail(String email) {
      // Validación básica de correo electrónico
      return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
   }

   private boolean isValidPassword(String password) {
      // Al menos una letra mayúscula, una letra minúscula, un número y un carácter
      // especial
      return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
   }

   private boolean isUsernameTaken(String username) {
      String query = "SELECT COUNT(*) FROM usuario WHERE username = ?";

      try (Connection conn = BBDDUtils.getConnection(); // Usar BBDDUtils para la conexión
            PreparedStatement stmt = conn.prepareStatement(query)) {

         stmt.setString(1, username);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               return rs.getInt(1) > 0; // Si hay al menos un registro, el nombre de usuario está en uso
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return false;
   }

   private boolean isEmailTaken(String email) {
      String query = "SELECT COUNT(*) FROM usuario WHERE email = ?";

      try (Connection conn = BBDDUtils.getConnection(); // Usar BBDDUtils para la conexión
            PreparedStatement stmt = conn.prepareStatement(query)) {

         stmt.setString(1, email);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               return rs.getInt(1) > 0; // Si hay al menos un registro, el correo está en uso
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return false;
   }

   private boolean registerUser(String name, String username, String email, String password) {
      String query = "INSERT INTO usuario (name, username, email, password) VALUES (?, ?, ?, ?)";

      try (Connection conn = BBDDUtils.getConnection(); // Usar BBDDUtils para la conexión
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

         loginStage.getIcons().add(new Image("/images/pokeball.png"));
         
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

   private void showAlert(String title, String message) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }
}