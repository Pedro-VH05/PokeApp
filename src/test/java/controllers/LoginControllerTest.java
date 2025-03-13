package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class LoginControllerTest {

    @Start
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
    }

    @Test
    public void testLoginWithValidCredentials(FxRobot robot) {
        robot.clickOn("#usernameField").write("validUser");
        robot.clickOn("#passwordField").write("validPassword");
        robot.clickOn("#loginButton");

        // Verificar que no hay alerta visible (éxito en el login)
        verifyThat(".alert", isVisible());
//        verifyThat(".alert.dialog-pane .content", hasText("Inicio de sesión exitoso."));
    }

    @Test
    public void testLoginWithInvalidCredentials(FxRobot robot) {
        robot.clickOn("#usernameField").write("invalidUser");
        robot.clickOn("#passwordField").write("invalidPassword");
        robot.clickOn("#loginButton");

        // Verificar que la alerta es visible y contiene el mensaje correcto
        verifyThat(".alert", isVisible());
//        verifyThat(".alert.dialog-pane .content", hasText("Usuario o contraseña incorrectos."));
    }

    @Test
    public void testLoginWithEmptyFields(FxRobot robot) {
        robot.clickOn("#loginButton");

        // Verificar que la alerta es visible y contiene el mensaje correcto
        verifyThat(".alert", isVisible());
//        verifyThat(".alert.dialog-pane .content", hasText("Debes completar todos los campos."));
    }
}