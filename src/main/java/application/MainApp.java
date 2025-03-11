package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

   @Override
   public void start(Stage primaryStage) throws Exception {

      primaryStage.getIcons().add(new Image("/images/pokeball.png"));

      Pane ventana = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
      primaryStage.initStyle(StageStyle.DECORATED);
      primaryStage.setMaximized(true);
      primaryStage.setMinWidth(800);
      primaryStage.setMinHeight(600);

      Scene scene = new Scene(ventana);
      scene.getStylesheets().add(getClass().getResource("/styles/CredentialsStyles.css").toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.setTitle("PokeApp");
      primaryStage.show();

   }

   public static void main(String[] args) {
      launch(args);
   }

}
