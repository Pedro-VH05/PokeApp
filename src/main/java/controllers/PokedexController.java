package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.User;
import models.UserSession;
import utils.BBDDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PokedexController {

    @FXML
    private GridPane pokedexGrid;

    @FXML
    private VBox mainPokedex;

    @FXML
    private HBox paginationBox;

    @FXML
    private ImageView loadingGif;

    private int currentPage = 0;
    private final int pokemonPerPage = 10;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        loadUserPokedex(currentPage);
        updatePagination();
    }
    
    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            Window currentWindow = mainPokedex.getScene().getWindow();
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
    
    private void openPokemonDetailsWindow(String pokemonName) {
       try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PokemonDetails.fxml"));
          Parent root = loader.load();

          Scene scene = new Scene(root);

          Stage newStage = new Stage();

          Window currentWindow = mainPokedex.getScene().getWindow();
          Stage currentStage = (Stage) currentWindow;
          
          PokemonDetailsController controller = loader.getController();
          controller.setPokemonName(pokemonName);

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

    private void loadUserPokedex(int page) {
        User loggedUser = UserSession.getLoggedUser();
        if (loggedUser == null) {
            return;
        }

        // Mostrar la animación de carga
        loadingGif.setVisible(true);

        int userId = loggedUser.getId();

        CompletableFuture.runAsync(() -> {
            try (Connection conn = BBDDUtils.getConnection();
                 PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM usuario_pokemon WHERE user_id = ?");
                 PreparedStatement stmt = conn.prepareStatement("SELECT pokemon.name, pokemon.image_url, pokemon.type FROM usuario_pokemon " +
                         "JOIN pokemon ON usuario_pokemon.pokemon_id = pokemon.id WHERE usuario_pokemon.user_id = ? LIMIT ? OFFSET ?")) {

                // Obtener el total de Pokémon para calcular las páginas
                countStmt.setInt(1, userId);
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalPokemon = countRs.getInt("total");
                    totalPages = (int) Math.ceil((double) totalPokemon / pokemonPerPage);
                }

                // Obtener los Pokémon de la página actual
                stmt.setInt(1, userId);
                stmt.setInt(2, pokemonPerPage);
                stmt.setInt(3, page * pokemonPerPage);
                ResultSet rs = stmt.executeQuery();

                List<VBox> pokemonCards = new ArrayList<>();

                while (rs.next()) {
                    String pokemonName = rs.getString("name");
                    String imageUrl = rs.getString("image_url");
                    String type = rs.getString("type");

                    VBox pokemonCard = createPokemonCard(pokemonName, imageUrl, type);
                    pokemonCards.add(pokemonCard);
                }

                Platform.runLater(() -> {
                    pokedexGrid.getChildren().clear();
                    for (int i = 0; i < pokemonCards.size(); i++) {
                        pokedexGrid.add(pokemonCards.get(i), i % 5, i / 5);
                    }
                    updatePagination();

                    // Ocultar la animación de carga una vez que se complete la carga
                    loadingGif.setVisible(false);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingGif.setVisible(false)); // Ocultar la animación en caso de error
            }
        });
    }

    private VBox createPokemonCard(String name, String imageUrl, String type) {
       VBox card = new VBox(10);
       card.setAlignment(Pos.CENTER);
       card.setPadding(new Insets(10));
       card.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

       // Nombre del Pokémon
       Label nameLabel = new Label(name);

       // Imagen del Pokémon
       ImageView imageView = new ImageView(new Image(imageUrl));
       imageView.setFitWidth(100);
       imageView.setFitHeight(100);

       // Contenedor para los tipos
       HBox typeImagesBox = new HBox(5);
       typeImagesBox.setAlignment(Pos.CENTER);
       
       Button detailsButton = new Button("Ver Detalles");
       detailsButton.setOnAction(e -> openPokemonDetailsWindow(name));

       // Dividir el texto de tipos y cargar las imágenes correspondientes
       String[] types = type.split(", ");
       for (String typeName : types) {
           try {
               ImageView typeImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + typeName + ".png")));
               typeImageView.setFitWidth(40);
               typeImageView.setFitHeight(20);
               typeImagesBox.getChildren().add(typeImageView);
           } catch (Exception e) {
               System.err.println("No se pudo cargar la imagen para el tipo: " + typeName);
           }
       }

       // Agregar elementos al card
       card.getChildren().addAll(imageView, nameLabel, typeImagesBox, detailsButton);

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
                pageButton.setStyle("-fx-background-color: #A90000;");
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
        loadUserPokedex(currentPage);
    }
    
    private void showAlert(String title, String message) {
       Alert alert = new Alert(Alert.AlertType.INFORMATION);
       alert.setTitle(title);
       alert.setHeaderText(null);
       alert.setContentText(message);
       alert.showAndWait();
   }
}