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
import models.User;
import models.UserSession;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

   @FXML
   private TextField usernameField;

   @FXML
   private PasswordField passwordField;

   private final String DB_URL = "jdbc:sqlite:pokeapp.db";

   @FXML
   private void handleLogin(ActionEvent event) {
      String username = usernameField.getText();
      String password = passwordField.getText();

      if (username.isEmpty() || password.isEmpty()) {
         showAlert("Error", "Debes completar todos los campos.");
         return;
      }

      User loggedInUser = validateLogin(username, password);
      if (loggedInUser != null) {
          // Guardar el usuario en la sesión
          UserSession.setLoggedUser(loggedInUser);
          showAlert("Éxito", "Inicio de sesión exitoso.");
          loadDashboard();
      } else {
          showAlert("Error", "Usuario o contraseña incorrectos.");
      }
   }

   @FXML
   private void handleRegister() {
      loadWindow("/views/Register.fxml");
   }

   private User validateLogin(String username, String password) {
      String query = "SELECT * FROM usuario WHERE username = ? AND password = ?";

      try (Connection conn = DriverManager.getConnection(DB_URL);
           PreparedStatement stmt = conn.prepareStatement(query)) {

          stmt.setString(1, username);
          stmt.setString(2, password);
          ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
              int id = rs.getInt("id");
              String dbName = rs.getString("name");
              String dbUsername = rs.getString("username");
              String dbPassword = rs.getString("password");
              String email = rs.getString("email");
              // Aquí construimos un objeto User con los datos recuperados
              return new User(id, dbName, dbUsername, dbPassword, email);
          } else {
              return null;
          }
      } catch (Exception e) {
          showAlert("Error", "Error al conectar con la base de datos.");
          e.printStackTrace();
          return null;
      }
  }

   private void loadDashboard() {
      loadWindow("/views/DashBoard.fxml");
   }

   private void loadWindow(String archivo) {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource(archivo));
         Parent root = loader.load();

         Scene scene = new Scene(root);

         Stage newStage = new Stage();

         Window currentWindow = usernameField.getScene().getWindow();
         Stage currentStage = (Stage) currentWindow;
         
         newStage.getIcons().add(new Image("/images/pokeball.png"));
         newStage.setMaximized(currentStage.isMaximized());
         newStage.setX(currentStage.getX());
         newStage.setY(currentStage.getY());
         newStage.setScene(scene);

         newStage.show();
         currentStage.close();
      } catch (Exception e) {
         showAlert("Error", "No se pudo cargar la pantalla de inicio.");
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
