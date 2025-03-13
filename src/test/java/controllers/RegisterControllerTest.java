package controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import utils.BBDDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterControllerTest {

   private RegisterController registerController;

   private MockedStatic<BBDDUtils> mockedBBDDUtils;

   @Mock
   private Connection mockConnection;

   @Mock
   private PreparedStatement mockStatement;

   @Mock
   private ResultSet mockResultSet;

   @BeforeEach
   void setUp() throws Exception {
      MockitoAnnotations.openMocks(this);
      registerController = new RegisterController();

      // Registrar el mock estático correctamente
      mockedBBDDUtils = mockStatic(BBDDUtils.class);

      // Simular la conexión a la base de datos
      Connection mockConnection = mock(Connection.class);
      mockedBBDDUtils.when(BBDDUtils::getConnection).thenReturn(mockConnection);
   }

   @AfterEach
   void tearDown() {
      // Cerrar el mock estático después de cada prueba
      mockedBBDDUtils.close();
   }

   // --- TESTS DE VALIDACIONES ---
   @Test
   void testIsValidName() {
      assertTrue(registerController.isValidName("Carlos"));
      assertFalse(registerController.isValidName("Carlos123"));
      assertFalse(registerController.isValidName("John_Doe"));
      assertFalse(registerController.isValidName(""));
   }

   @Test
   void testIsValidUsername() {
      assertTrue(registerController.isValidUsername("user_123"));
      assertTrue(registerController.isValidUsername("abcd"));
      assertFalse(registerController.isValidUsername("abc"));
      assertFalse(registerController.isValidUsername("invalid-username"));
      assertFalse(registerController.isValidUsername(""));
   }

   @Test
   void testIsValidEmail() {
      assertTrue(registerController.isValidEmail("email@example.com"));
      assertFalse(registerController.isValidEmail("invalid-email"));
      assertFalse(registerController.isValidEmail("missing@domain"));
      assertFalse(registerController.isValidEmail(""));
   }

   @Test
   void testIsValidPassword() {
      assertTrue(registerController.isValidPassword("Password1@"));
      assertFalse(registerController.isValidPassword("password"));
      assertFalse(registerController.isValidPassword("12345678"));
      assertFalse(registerController.isValidPassword("WeakPass"));
      assertFalse(registerController.isValidPassword("NoSpecial1"));
   }

   // --- TESTS DE BASE DE DATOS (SIMULADA) ---
   @Test
   void testIsUsernameTaken() throws Exception {
      when(mockStatement.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getInt(1)).thenReturn(1);

      assertTrue(registerController.isUsernameTaken("existingUser"));

      when(mockResultSet.getInt(1)).thenReturn(0);
      assertFalse(registerController.isUsernameTaken("newUser"));
   }

   @Test
   void testIsEmailTaken() throws Exception {
      when(mockStatement.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getInt(1)).thenReturn(1);

      assertTrue(registerController.isEmailTaken("existing@example.com"));

      when(mockResultSet.getInt(1)).thenReturn(0);
      assertFalse(registerController.isEmailTaken("new@example.com"));
   }

   @Test
   void testRegisterUser() throws Exception {
      when(mockStatement.executeUpdate()).thenReturn(1);
      assertTrue(registerController.registerUser("John Doe", "john_doe", "john@example.com", "Password1@"));

      when(mockStatement.executeUpdate()).thenReturn(0);
      assertFalse(registerController.registerUser("John Doe", "john_doe", "john@example.com", "Password1@"));
   }
}
