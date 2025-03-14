package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.BBDDUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import java.sql.Connection;
import java.sql.PreparedStatement;

@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class RegisterControllerTest {

   @Start
   public void start(Stage stage) throws Exception {
      Parent root = FXMLLoader.load(getClass().getResource("/views/Register.fxml"));
      stage.setScene(new Scene(root));
      stage.show();
   }

   @BeforeEach
   public void setUp(FxRobot robot) throws Exception {
      FxToolkit.registerPrimaryStage();
      closeExistingAlerts(robot);
   }

   @AfterEach
   public void tearDown(FxRobot robot) throws Exception {
      FxToolkit.hideStage();
      closeExistingAlerts(robot);
      Thread.sleep(1000);
   }

   @BeforeAll
   public static void beforeAll() {
      deleteTestUser("johndoe");
   }

   @AfterAll
   public static void afterAll() {
      deleteTestUser("johndoe");
   }

   @Test
   @Order(1)
   public void testRegisterWithEmptyFields(FxRobot robot) {
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Todos los campos son obligatorios."));
   }

   @Test
   @Order(2)
   public void testRegisterWithInvalidName(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe**");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content",
            hasText("El nombre solo puede contener letras (mayúsculas y minúsculas)."));
   }

   @Test
   @Order(3)
   public void testRegisterWithInvalidUsername(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe****");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText(
            "El nombre de usuario solo puede contener letras, números o la barra baja (_) y debe tener al menos 4 caracteres."));
   }

   @Test
   @Order(4)
   public void testRegisterWithInvalidEmail(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("invalid-email");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("El correo electrónico no es válido."));
   }

   @Test
   @Order(5)
   public void testRegisterWithInvalidPassword(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password");
      robot.clickOn("#repeatPasswordField").write("Password");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText(
            "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial."));
   }

   @Test
   @Order(6)
   public void testRegisterWithMismatchedPasswords(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password1234!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Las contraseñas no coinciden."));
   }

   @Test
   @Order(7)
   public void testRegisterWithValidData(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Usuario registrado correctamente."));
   }

   @Test
   @Order(8)
   public void testRegisterWithValidDataRepeatedUsername(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("johndoe");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("El nombre de usuario ya está registrado."));
   }

   @Test
   @Order(9)
   public void testRegisterWithValidDataRepeatedEmail(FxRobot robot) {
      robot.clickOn("#nameField").write("John Doe");
      robot.clickOn("#usernameField").write("AnotherOne");
      robot.clickOn("#emailField").write("test@example.com");
      robot.clickOn("#passwordField").write("Password123!");
      robot.clickOn("#repeatPasswordField").write("Password123!");
      robot.clickOn("#registerButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("El correo electrónico ya está registrado."));
   }

   private static void deleteTestUser(String username) {
      try (Connection conn = BBDDUtils.getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuario WHERE username = ?")) {
         stmt.setString(1, username);
         stmt.executeUpdate();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void closeExistingAlerts(FxRobot robot) {
      robot.interact(() -> {
         if (robot.lookup(".alert").tryQuery().isPresent()) {
            robot.clickOn(".alert .button");
         }
      });
   }

}
