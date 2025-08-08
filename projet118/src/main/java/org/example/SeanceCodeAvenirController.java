package org.example;

import Persistance.models.Seance;
import Persistance.utils.Alert; // Import modifié
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

public class SeanceCodeAvenirController {

    @FXML
    private TableView<Seance> tableSeancesCode;

    @FXML
    private TableColumn<Seance, ImageView> codeIconColumn;

    @FXML
    private TableColumn<Seance, Timestamp> colonneDateCode;

    @FXML
    private TableColumn<Seance, Integer> colonneMoniteurCode;

    @FXML
    private TableColumn<Seance, Integer> colonneCandidatCode;

    @FXML
    private Button closeButton;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadThisWeekSeances();
        // Add placeholder for empty table
        tableSeancesCode.setPlaceholder(new Label("Aucune séance de code prévue cette semaine."));
    }

    private void setupTableColumns() {
        // Icon column with code image
        codeIconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView;

            {
                Image image = new Image(getClass().getResourceAsStream("/images/seance_code_icon.png"));
                if (image.isError()) {
                    imageView = new ImageView();
                    System.err.println("Warning: /org/example/images/seance_code_icon.png not found");
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
        colonneDateCode.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateTime()));
        colonneDateCode.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDateTime()));
            }
        });
        colonneMoniteurCode.setCellValueFactory(cellData -> {
            int moniteurId = cellData.getValue().getMoniteurId();
            System.out.println("Moniteur ID: " + moniteurId); // Debug
            return new javafx.beans.property.SimpleIntegerProperty(moniteurId).asObject();
        });
        colonneCandidatCode.setCellValueFactory(cellData -> {
            int candidatId = cellData.getValue().getCandidatId();
            System.out.println("Candidat ID: " + candidatId); // Debug
            return new javafx.beans.property.SimpleIntegerProperty(candidatId).asObject();
        });
    }

    private void loadThisWeekSeances() {
        List<Seance> seances = SeanceService.filterCodeByThisWeek();
        // Debug: Print the sessions
        seances.forEach(seance -> System.out.println("Seance: " + seance.getDateTime() + ", Moniteur: " + seance.getMoniteurId() + ", Candidat: " + seance.getCandidatId()));

        ObservableList<Seance> thisWeekSeances = FXCollections.observableArrayList(seances);
        tableSeancesCode.setItems(thisWeekSeances);

        if (thisWeekSeances.isEmpty()) {
            Alert.showInformationAlert("Aucune Séance", "Aucune séance de code prévue cette semaine.");
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}