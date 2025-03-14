package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PokemonDetailsController {

   @FXML
   private ImageView pokemonImage;

   @FXML
   private Label pokemonName;

   @FXML
   private HBox typesBox;

   @FXML
   private HBox abilitiesBox;

   @FXML
   private GridPane statsGrid;

   @FXML
   private VBox movesBox;

   @FXML
   private Text descriptionText;

   @FXML
   private FlowPane weaknessesBox;

   @FXML
   private FlowPane resistancesBox;

   @FXML
   private GridPane evolutionGrid;

   private final OkHttpClient client = new OkHttpClient();

   public void setPokemonName(String name) {
      pokemonName.setText(name);
      pokemonName.setFont(Font.font("Arial", FontWeight.BOLD, 20));
      loadPokemonDetails(name);
   }

   @FXML
   private void goBackToDashboard() {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
         Parent root = loader.load();

         Scene scene = new Scene(root);

         Window currentWindow = pokemonName.getScene().getWindow();
         Stage currentStage = (Stage) currentWindow;

         Stage newStage = new Stage();

         newStage.getIcons().add(new Image("/images/pokeball.png"));
         newStage.setMaximized(currentStage.isMaximized());
         newStage.setX(currentStage.getX());
         newStage.setY(currentStage.getY());
         newStage.setScene(scene);

         newStage.show();
         currentStage.close();
      } catch (Exception e) {
         showAlert("Error", "No se pudo cargar el Dashboard.");
         e.printStackTrace();
      }
   }

   private void loadPokemonDetails(String name) {
      String url = "https://pokeapi.co/api/v2/pokemon/" + name;
      Request request = new Request.Builder().url(url).build();

      try (Response response = client.newCall(request).execute()) {
         if (response.isSuccessful()) {
            JSONObject pokemonDetails = new JSONObject(response.body().string());
            String imageUrl = pokemonDetails.getJSONObject("sprites").getString("front_default");
            JSONArray types = pokemonDetails.getJSONArray("types");

            pokemonImage.setImage(new Image(imageUrl));

            // Cargar tipos
            typesBox.getChildren().clear();
            for (int i = 0; i < types.length(); i++) {
               String type = types.getJSONObject(i).getJSONObject("type").getString("name");
               ImageView typeImageView = new ImageView(
                     new Image(getClass().getResourceAsStream("/images/" + type + ".png")));
               typeImageView.setFitWidth(40);
               typeImageView.setFitHeight(20);
               typesBox.getChildren().add(typeImageView);
            }

            // Cargar habilidades
            loadAbilities(pokemonDetails);

            // Cargar estadísticas base
            loadBaseStats(pokemonDetails);

            // Cargar descripción
            loadDescription(pokemonDetails.getJSONObject("species").getString("url"));

            // Cargar cadena de evolución
            loadEvolutionChain(pokemonDetails.getJSONObject("species").getString("url"));
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void loadAbilities(JSONObject pokemonDetails) {
      JSONArray abilities = pokemonDetails.getJSONArray("abilities");
      abilitiesBox.getChildren().clear();
      for (int i = 0; i < abilities.length(); i++) {
         String ability = abilities.getJSONObject(i).getJSONObject("ability").getString("name");
         Label abilityLabel = new Label(ability);
         abilitiesBox.getChildren().add(abilityLabel);
      }
   }

   private void loadBaseStats(JSONObject pokemonDetails) {
      JSONArray stats = pokemonDetails.getJSONArray("stats");
      statsGrid.getChildren().clear();
      for (int i = 0; i < stats.length(); i++) {
         String statName = stats.getJSONObject(i).getJSONObject("stat").getString("name");
         int baseStat = stats.getJSONObject(i).getInt("base_stat");

         Label statNameLabel = new Label(statName + ":");
         Label statValueLabel = new Label(String.valueOf(baseStat));

         statsGrid.add(statNameLabel, 0, i);
         statsGrid.add(statValueLabel, 1, i);
      }
   }

   private void loadDescription(String speciesUrl) {
      Request request = new Request.Builder().url(speciesUrl).build();

      try (Response response = client.newCall(request).execute()) {
         if (response.isSuccessful()) {
            JSONObject speciesDetails = new JSONObject(response.body().string());
            JSONArray flavorTextEntries = speciesDetails.getJSONArray("flavor_text_entries");
            for (int i = 0; i < flavorTextEntries.length(); i++) {
               JSONObject entry = flavorTextEntries.getJSONObject(i);
               if (entry.getJSONObject("language").getString("name").equals("es")) {
                  descriptionText.setText(entry.getString("flavor_text"));
                  break;
               }
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void loadEvolutionChain(String speciesUrl) {
      Request request = new Request.Builder().url(speciesUrl).build();

      try (Response response = client.newCall(request).execute()) {
         if (response.isSuccessful()) {
            JSONObject speciesDetails = new JSONObject(response.body().string());
            String evolutionChainUrl = speciesDetails.getJSONObject("evolution_chain").getString("url");
            loadEvolutionChainDetails(evolutionChainUrl);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void loadEvolutionChainDetails(String evolutionChainUrl) {
      Request request = new Request.Builder().url(evolutionChainUrl).build();

      try (Response response = client.newCall(request).execute()) {
          if (response.isSuccessful()) {
              JSONObject evolutionChain = new JSONObject(response.body().string());
              JSONObject chain = evolutionChain.getJSONObject("chain");
              List<String> evolutionLine = new ArrayList<>();
              extractEvolutionLine(chain, evolutionLine);

              evolutionGrid.getChildren().clear();
              for (int i = 0; i < evolutionLine.size(); i++) {
                  String pokemonName = evolutionLine.get(i);
                  ImageView imageView = new ImageView();
                  imageView.setFitWidth(100);
                  imageView.setFitHeight(100);

                  // Crear un Label para el nombre del Pokémon
                  Label nameLabel = new Label(pokemonName);
                  nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                  // Obtener la imagen del Pokémon
                  String imageUrl = "https://pokeapi.co/api/v2/pokemon/" + pokemonName;
                  Request pokemonRequest = new Request.Builder().url(imageUrl).build();
                  Response pokemonResponse = client.newCall(pokemonRequest).execute();

                  if (pokemonResponse.isSuccessful()) {
                      JSONObject pokemonDetails = new JSONObject(pokemonResponse.body().string());
                      String image = pokemonDetails.getJSONObject("sprites").getString("front_default");
                      imageView.setImage(new Image(image));
                  }

                  // Agregar la imagen y el nombre al GridPane
                  evolutionGrid.add(imageView, i, 0); // Imagen en la fila 0
                  evolutionGrid.add(nameLabel, i, 1); // Nombre en la fila 1
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

   private void extractEvolutionLine(JSONObject chain, List<String> evolutionLine) {
      evolutionLine.add(chain.getJSONObject("species").getString("name"));
      JSONArray evolvesTo = chain.getJSONArray("evolves_to");
      if (evolvesTo.length() > 0) {
         extractEvolutionLine(evolvesTo.getJSONObject(0), evolutionLine);
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