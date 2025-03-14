package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class LoginControllerTest {

   @Start
   public void start(Stage stage) throws Exception {
      Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
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

   @Test
   @Order(1)
   public void testLoginWithEmptyFields(FxRobot robot) {
      robot.clickOn("#loginButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Debes completar todos los campos."));
   }

   @Test
   @Order(2)
   public void testLoginWithInvalidCredentials(FxRobot robot) {
      robot.clickOn("#usernameField").write("invalidUser");
      robot.clickOn("#passwordField").write("invalidPassword");
      robot.clickOn("#loginButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Usuario o contraseña incorrectos."));
   }

   @Test
   @Order(3)
   public void testLoginWithValidCredentials(FxRobot robot) {
      robot.clickOn("#usernameField").write("a");
      robot.clickOn("#passwordField").write("a");
      robot.clickOn("#loginButton");

      verifyThat(".alert", isVisible());
      verifyThat(".alert.dialog-pane .content", hasText("Inicio de sesión exitoso."));
   }

   private void closeExistingAlerts(FxRobot robot) {
      robot.interact(() -> {
         if (robot.lookup(".alert").tryQuery().isPresent()) {
            robot.clickOn(".alert .button");
         }
      });
   }
}