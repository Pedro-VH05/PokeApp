package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.User;
import models.UserSession;
import utils.BBDDUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PokedexController {

    @FXML
    private VBox pokedexContainer;

    @FXML
    public void initialize() {
        loadUserPokedex();
    }

    private void loadUserPokedex() {
        User loggedUser = UserSession.getLoggedUser();
        if (loggedUser == null) {
            return;
        }

        int userId = loggedUser.getId();

        try (Connection conn = BBDDUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT pokemon.name, pokemon.image_url, pokemon.type FROM usuario_pokemon " +
                             "JOIN pokemon ON usuario_pokemon.pokemon_id = pokemon.id WHERE usuario_pokemon.user_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String pokemonName = rs.getString("name");
                String imageUrl = rs.getString("image_url");
                String type = rs.getString("type");

                // Crear una tarjeta para cada Pokémon
                VBox pokemonCard = createPokemonCard(pokemonName, imageUrl, type);
                pokedexContainer.getChildren().add(pokemonCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPokemonCard(String name, String imageUrl, String type) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        Label typeLabel = new Label("Tipo: " + type);
        typeLabel.setFont(Font.font("Arial", 12));

        card.getChildren().addAll(imageView, nameLabel, typeLabel);

        return card;
    }
}