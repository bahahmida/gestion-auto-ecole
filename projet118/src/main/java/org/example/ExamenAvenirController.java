package org.example;

import Persistance.models.PasserExamen;
import Service.PasserExamenService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showInformationAlert;

public class ExamenAvenirController {

    @FXML private TableView<PasserExamen> examsTable;
    @FXML private TableColumn<PasserExamen, ImageView> iconColumn;
    @FXML private TableColumn<PasserExamen, Integer> idColumn;
    @FXML private TableColumn<PasserExamen, String> typeColumn;
    @FXML private TableColumn<PasserExamen, LocalDateTime> dateColumn;
    @FXML private TableColumn<PasserExamen, Integer> candidatCinColumn;
    @FXML private TableColumn<PasserExamen, Float> prixColumn;
    @FXML private TableColumn<PasserExamen, String> resultatColumn;
    @FXML private Button closeButton;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadThisWeekExams();
    }

    private void setupTableColumns() {
        iconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/exam.jpg")));
            {
                imageView.setFitHeight(30);
                imageView.setFitWidth(30);
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdExamen()).asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomExamen()));
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateExamen()));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });
        candidatCinColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCinCondidat()).asObject());
        prixColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getPrix()).asObject());
        resultatColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getResultatExamen()));
    }

    private void loadThisWeekExams() {
        try {
            ObservableList<PasserExamen> thisWeekExams = FXCollections.observableArrayList(PasserExamenService.filterByThisWeek());
            examsTable.setItems(thisWeekExams);
            if (thisWeekExams.isEmpty()) {
                showInformationAlert("Information", "Aucun examen prévu cette semaine.");
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du chargement des examens : " + e.getMessage());
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}