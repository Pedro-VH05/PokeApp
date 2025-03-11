package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import models.User;
import models.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.BBDDUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DashboardController {

   @FXML
   private GridPane pokemonGrid;

   @FXML
   private TextField searchField;

   @FXML
   private HBox paginationBox;

   @FXML
   private VBox loadingContainer;

   @FXML
   private ImageView loadingGif;

   private final OkHttpClient client = new OkHttpClient();
   private int currentPage = 0;
   private final int pokemonPerPage = 10;
   private int totalPages = 0;

   @FXML
   public void initialize() {
      loadPokemon(currentPage);
      updatePagination();
   }

   private void loadPokemon(int page) {
      loadingGif.setVisible(true);

      Task<Void> task = new Task<Void>() {
         @Override
         protected Void call() throws Exception {
            try {
               Request request = new Request.Builder().url(
                     "https://pokeapi.co/api/v2/pokemon?offset=" + (page * pokemonPerPage) + "&limit=" + pokemonPerPage)
                     .build();

               Response response = client.newCall(request).execute();
               String jsonData = response.body().string();
               JSONObject jsonObject = new JSONObject(jsonData);
               JSONArray results = jsonObject.getJSONArray("results");

               totalPages = (int) Math.ceil((double) jsonObject.getInt("count") / pokemonPerPage);

               // Crear una lista de tarjetas de Pokémon para actualizar la UI en una sola
               // operación
               List<VBox> pokemonCards = new ArrayList<>();

               for (int i = 0; i < results.length(); i++) {
                  JSONObject pokemon = results.getJSONObject(i);
                  String pokemonUrl = pokemon.getString("url");
                  String pokemonName = pokemon.getString("name");
                  Request detailsRequest = new Request.Builder().url(pokemonUrl).build();
                  Response detailsResponse = client.newCall(detailsRequest).execute();
                  JSONObject pokemonDetails = new JSONObject(detailsResponse.body().string());
                  String imageUrl = pokemonDetails.getJSONObject("sprites").getString("front_default");
                  JSONArray pokemonType = pokemonDetails.getJSONArray("types");

                  VBox pokemonCard = createPokemonCard(pokemonName, pokemonType, imageUrl);
                  pokemonCards.add(pokemonCard);
               }

               // Actualizar la UI en el hilo principal
               Platform.runLater(() -> {
                  pokemonGrid.getChildren().clear();
                  for (int i = 0; i < pokemonCards.size(); i++) {
                     pokemonGrid.add(pokemonCards.get(i), i % 5, i / 5);
                  }
                  updatePagination();
                  loadingGif.setVisible(false);
               });

            } catch (IOException e) {
               e.printStackTrace();
            }
            return null;
         }
      };

      new Thread(task).start();
   }

   private VBox createPokemonCard(String name, JSONArray types, String imageUrl) {
      VBox card = new VBox(10);
      card.setAlignment(Pos.CENTER);
      card.setPadding(new Insets(10));
      card.setStyle(
            "-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

      Label nameLabel = new Label(name);
      nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

      HBox typeImagesBox = new HBox(5);
      typeImagesBox.setAlignment(Pos.CENTER);

      for (int i = 0; i < types.length(); i++) {
         String type = types.getJSONObject(i).getJSONObject("type").getString("name");
         ImageView typeImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + type + ".png")));
         typeImageView.setFitWidth(40);
         typeImageView.setFitHeight(20);
         typeImagesBox.getChildren().add(typeImageView);
      }

      Button togglePokedexButton = new Button();
      togglePokedexButton.setOnAction(e -> addPokemonToPokedex(name, imageUrl, types));

      checkIfPokemonInPokedex(name, togglePokedexButton, imageUrl, types);

      ImageView imageView = new ImageView();
      imageView.setFitWidth(100);
      imageView.setFitHeight(100);

      CompletableFuture.runAsync(() -> {
         Image image = new Image(imageUrl, true);
         Platform.runLater(() -> imageView.setImage(image));
      });

      card.getChildren().addAll(imageView, nameLabel, typeImagesBox, togglePokedexButton);

      return card;
   }

   private void updatePagination() {
      paginationBox.getChildren().clear();
      int startPage = Math.max(1, currentPage - 2);
      int endPage = Math.min(totalPages, startPage + 6);

      if (currentPage > 0) {
         Button prevButton = new Button("Anterior");
         prevButton.setOnAction(e -> handlePageChange(currentPage - 1));
         paginationBox.getChildren().add(prevButton);
      }

      for (int i = startPage; i <= endPage; i++) {
         final int pageIndex = i - 1;
         Button pageButton = new Button(String.valueOf(i));
         pageButton.setOnAction(e -> handlePageChange(pageIndex));
         if (pageIndex == currentPage) {
            pageButton.setStyle("-fx-background-color: #cccccc;");
         }
         paginationBox.getChildren().add(pageButton);
      }

      if (currentPage < totalPages - 1) {
         Button nextButton = new Button("Siguiente");
         nextButton.setOnAction(e -> handlePageChange(currentPage + 1));
         paginationBox.getChildren().add(nextButton);
      }
   }

   private void handlePageChange(int newPage) {
      currentPage = newPage;
      loadPokemon(currentPage);
   }

   @FXML
   private void handleSearch() {
      loadingGif.setVisible(true);
      String searchTerm = searchField.getText().toLowerCase();

      if (!searchTerm.isEmpty()) {
         CompletableFuture.runAsync(() -> {
            try {
               Request request = new Request.Builder().url("https://pokeapi.co/api/v2/pokemon/" + searchTerm).build();
               Response response = client.newCall(request).execute();

               if (response.isSuccessful()) {
                  JSONObject pokemonDetails = new JSONObject(response.body().string());
                  String imageUrl = pokemonDetails.getJSONObject("sprites").getString("front_default");

                  VBox pokemonCard = createPokemonCard(pokemonDetails.getString("name"),
                        pokemonDetails.getJSONArray("types"), imageUrl);

                  Platform.runLater(() -> {
                     pokemonGrid.getChildren().clear();
                     pokemonGrid.add(pokemonCard, 0, 0);
                     loadingGif.setVisible(false);
                  });
               } else {
                  Platform.runLater(() -> {
                     pokemonGrid.getChildren().clear();
                     pokemonGrid.add(new Label("Pokémon no encontrado"), 0, 0);
                     loadingGif.setVisible(false);
                  });
               }
            } catch (IOException e) {
               e.printStackTrace();
               Platform.runLater(() -> loadingGif.setVisible(true));
            }
         });
      } else {
         loadPokemon(currentPage);
      }
   }

   private void addPokemonToPokedex(String name, String imageUrl, JSONArray types) {
      User loggedUser = UserSession.getLoggedUser();
      if (loggedUser == null) {
         showAlert("Error", "Debes iniciar sesión primero.");
         return;
      }

      int userId = loggedUser.getId();

      // Convertir los tipos a texto para la base de datos
      StringBuilder typeBuilder = new StringBuilder();
      for (int i = 0; i < types.length(); i++) {
         if (i > 0)
            typeBuilder.append(", ");
         typeBuilder.append(types.getJSONObject(i).getJSONObject("type").getString("name"));
      }
      String type = typeBuilder.toString();

      CompletableFuture.runAsync(() -> {
         try (Connection conn = BBDDUtils.getConnection();
               PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM pokemon WHERE name = ?");
               PreparedStatement insertPokemonStmt = conn.prepareStatement(
                     "INSERT INTO pokemon (name, type, image_url) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
               PreparedStatement insertUserPokemonStmt = conn
                     .prepareStatement("INSERT INTO usuario_pokemon (user_id, pokemon_id) VALUES (?, ?)")) {

            // Verificar si el Pokémon ya existe en la BD
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            int pokemonId;

            if (rs.next()) {
               pokemonId = rs.getInt("id");
            } else {
               // Insertamos el Pokémon
               insertPokemonStmt.setString(1, name);
               insertPokemonStmt.setString(2, type);
               insertPokemonStmt.setString(3, imageUrl);
               insertPokemonStmt.executeUpdate();

               // Obtener el ID generado
               ResultSet generatedKeys = insertPokemonStmt.getGeneratedKeys();
               if (generatedKeys.next()) {
                  pokemonId = generatedKeys.getInt(1);
               } else {
                  throw new SQLException("Error al obtener ID del Pokémon insertado.");
               }
            }

            insertUserPokemonStmt.setInt(1, userId);
            insertUserPokemonStmt.setInt(2, pokemonId);
            insertUserPokemonStmt.executeUpdate();

            Platform.runLater(() -> showAlert("Éxito", "Pokémon añadido a tu Pokédex."));
            loadPokemon(currentPage);
         } catch (SQLException e) {
            Platform.runLater(() -> showAlert("Error", "Este Pokémon ya está en tu Pokédex."));
         }
      });
   }

   private void checkIfPokemonInPokedex(String pokemonName, Button togglePokedexButton, String imageUrl,
         JSONArray types) {
      User loggedUser = UserSession.getLoggedUser();
      if (loggedUser == null) {
         return;
      }

      int userId = loggedUser.getId();

      CompletableFuture.runAsync(() -> {
         try (Connection conn = BBDDUtils.getConnection();
               PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM usuario_pokemon WHERE user_id = ? AND pokemon_id = (SELECT id FROM pokemon WHERE name = ?)")) {

            stmt.setInt(1, userId);
            stmt.setString(2, pokemonName);

            ResultSet rs = stmt.executeQuery();
            boolean isPokemonInPokedex = rs.next();

            Platform.runLater(() -> {
               if (isPokemonInPokedex) {
                  // El Pokémon ya está en la Pokédex, cambiar el texto y acción del botón
                  togglePokedexButton.setText("Eliminar de la Pokédex");
                  togglePokedexButton.setOnAction(e -> removePokemonFromPokedex(pokemonName));
               } else {
                  // El Pokémon no está en la Pokédex, mostrar el botón de añadir
                  togglePokedexButton.setText("Añadir a la Pokédex");
                  togglePokedexButton.setOnAction(e -> addPokemonToPokedex(pokemonName, imageUrl, types));
               }
            });
         } catch (SQLException e) {
            e.printStackTrace();
         }
      });
   }

   private void removePokemonFromPokedex(String pokemonName) {
      User loggedUser = UserSession.getLoggedUser();
      if (loggedUser == null) {
         showAlert("Error", "Debes iniciar sesión primero.");
         return;
      }

      int userId = loggedUser.getId();

      CompletableFuture.runAsync(() -> {
         try (Connection conn = BBDDUtils.getConnection();
               PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM usuario_pokemon WHERE user_id = ? AND pokemon_id = (SELECT id FROM pokemon WHERE name = ?)")) {

            stmt.setInt(1, userId);
            stmt.setString(2, pokemonName);
            int rowsAffected = stmt.executeUpdate();

            Platform.runLater(() -> {
               if (rowsAffected > 0) {
                  loadPokemon(currentPage);
                  showAlert("Éxito", "Pokémon eliminado de tu Pokédex.");
               } else {
                  showAlert("Error", "No se pudo eliminar el Pokémon.");
               }
            });
         } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error", "Ocurrió un error al eliminar el Pokémon."));
         }
      });
   }

   @FXML
   private void openPokedexWindow() {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Pokedex.fxml"));
         Scene scene = new Scene(loader.load());

         Stage stage = new Stage();
         stage.setTitle("Pokédex");
         stage.setScene(scene);
         stage.show();
      } catch (IOException e) {
         showAlert("Error", "No se pudo cargar la ventana de la Pokédex.");
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
