package org.example;

import Persistance.models.Seance;
import Persistance.utils.Alert; // Import ajouté
import Service.SeanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeanceConduitAvenirController {

    @FXML
    private TableView<Seance> tableSeancesConduite;

    @FXML
    private TableColumn<Seance, ImageView> conduitIconColumn;

    @FXML
    private TableColumn<Seance, Timestamp> colonneDateHeureConduite;

    @FXML
    private TableColumn<Seance, String> colonneVehiculeConduite;

    @FXML
    private TableColumn<Seance, Integer> colonneCandidatConduite;

    @FXML
    private TableColumn<Seance, Integer> colonneMoniteurConduite;

    @FXML
    private TableColumn<Seance, String> colonneLocalisation;

    @FXML
    private Button closeButton;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadThisWeekSeances();
    }

    private void setupTableColumns() {
        // Icon column with driving image
        conduitIconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView;

            {
                Image image = new Image(getClass().getResourceAsStream("/images/seance_conduite_icon.png"));
                if (image.isError()) {
                    imageView = new ImageView();
                    System.err.println("Warning: /org/example/images/seance_conduite_icon.png not found");
                } else {
                    imageView = new ImageView(image);
                    imageView.setFitHeight(35);
                    imageView.setFitWidth(35);
                }
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        // Data columns
        colonneDateHeureConduite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateTime()));
        colonneDateHeureConduite.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDateTime()));
            }
        });
        colonneVehiculeConduite.setCellValueFactory(cellData -> {
            String location = cellData.getValue().getLocation();
            System.out.println("Location for seance: " + location); // Debug
            return new javafx.beans.property.SimpleStringProperty(location != null ? location : "N/A");
        });
        colonneCandidatConduite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCandidatId()).asObject());
        colonneMoniteurConduite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMoniteurId()).asObject());
        colonneLocalisation.setCellValueFactory(cellData -> {
            String localisation = cellData.getValue().getLocalisation();
            System.out.println("Localisation for seance: " + localisation); // Debug
            return new javafx.beans.property.SimpleStringProperty(localisation != null ? localisation : "N/A");
        });
    }

    private void loadThisWeekSeances() {
        List<Seance> seances = SeanceService.filterConduiteByThisWeek();
        // Debug: Print the sessions
        seances.forEach(seance -> System.out.println("Seance: " + seance.getDateTime() + ", Location: " + seance.getLocation() + ", Localisation: " + seance.getLocalisation()));

        ObservableList<Seance> thisWeekSeances = FXCollections.observableArrayList(seances);
        tableSeancesConduite.setItems(thisWeekSeances);

        if (thisWeekSeances.isEmpty()) {
            Alert.showInformationAlert("Aucune Séance", "Aucune séance de conduite prévue cette semaine.");
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}