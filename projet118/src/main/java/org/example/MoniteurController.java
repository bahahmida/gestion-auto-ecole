package org.example;

import Persistance.models.Moniteur;
import Service.MoniteurService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleFloatProperty;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;
import static Persistance.utils.Alert.showConfirmationAlert; // Ajout de l'import

public class MoniteurController {
    private MenuController menuController;
    private ObservableList<Moniteur> allMonitors;
    private FilteredList<Moniteur> filteredMonitors;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML private TextField searchField;
    @FXML private Button addInstructorButton;
    @FXML private ToggleButton activeFilter;
    @FXML private ToggleButton inactiveFilter;
    @FXML private ToggleButton availableFilter;
    @FXML private ToggleButton allFilter;
    @FXML private TableView<Moniteur> instructorsTable;
    @FXML private TableColumn<Moniteur, String> iconColumn;
    @FXML private TableColumn<Moniteur, Integer> cinColumn;
    @FXML private TableColumn<Moniteur, String> nomColumn;
    @FXML private TableColumn<Moniteur, String> prenomColumn;
    @FXML private TableColumn<Moniteur, Float> salaireColumn;
    @FXML private TableColumn<Moniteur, Integer> telephoneColumn;
    @FXML private TableColumn<Moniteur, String> categorieColumn;
    @FXML private TableColumn<Moniteur, Moniteur> updateColumn;
    @FXML private TableColumn<Moniteur, Moniteur> deleteColumn;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupFilterButtons();
        loadMonitors();
        setupSearchListener();
    }

    private void setupTableColumns() {
        iconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/monitor.jpg")));
            {
                imageView.setFitHeight(35);
                imageView.setFitWidth(35);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(Pos.CENTER);
            }
        });

        cinColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCin()).asObject());
        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));
        salaireColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getSalaire()).asObject());
        telephoneColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTel()).asObject());
        categorieColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategorie().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))
        ));

        updateColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView updateIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            private final Button updateButton = new Button();
            {
                updateIcon.setFitHeight(28);
                updateIcon.setFitWidth(28);
                updateButton.setGraphic(updateIcon);
                updateButton.setStyle("-fx-background-color: transparent;");
                updateButton.setOnAction(event -> {
                    Moniteur moniteur = getTableView().getItems().get(getIndex());
                    handleUpdateMoniteur(moniteur);
                });
            }

            @Override
            protected void updateItem(Moniteur moniteur, boolean empty) {
                super.updateItem(moniteur, empty);
                setGraphic(empty ? null : updateButton);
                setAlignment(Pos.CENTER);
            }
        });

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            private final Button deleteButton = new Button();
            {
                deleteIcon.setFitHeight(27);
                deleteIcon.setFitWidth(27);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setOnAction(event -> {
                    Moniteur moniteur = getTableView().getItems().get(getIndex());
                    handleDeleteMoniteur(moniteur);
                });
            }

            @Override
            protected void updateItem(Moniteur moniteur, boolean empty) {
                super.updateItem(moniteur, empty);
                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.trim().toLowerCase();
            try {
                if (searchText.isEmpty()) {
                    filteredMonitors.setPredicate(moniteur -> true);
                } else {
                    filteredMonitors.setPredicate(moniteur ->
                            (moniteur.getNom() != null && moniteur.getNom().toLowerCase().contains(searchText)) ||
                                    (moniteur.getPrenom() != null && moniteur.getPrenom().toLowerCase().contains(searchText)) ||
                                    String.valueOf(moniteur.getCin()).contains(searchText)
                    );
                }
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de la recherche : " + e.getMessage());
            }
        });
    }

    private void setupFilterButtons() {
        String defaultStyle = " -fx-background-color: linear-gradient(to bottom, rgba(255, 255, 255, 0.95), rgba(245, 248, 252, 0.95));" +
                " -fx-text-fill: #1e5b9c;" +
                " -fx-border-color: rgba(30, 91, 156, 0.6);" +
                " -fx-border-width: 1.2px;" +
                " -fx-border-radius: 22px;" +
                " -fx-background-radius: 22px;" +
                " -fx-padding: 12px 22px;" +
                " -fx-cursor: hand;" +
                " -fx-font-weight: 500;" +
                " -fx-font-size: 14px;" +
                " -fx-min-width: 110px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);" +
                " -fx-transition: background-color 0.25s, text-fill 0.25s, effect 0.25s;";
        String selectedStyle = "-fx-text-fill: white;" +
                " -fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                " -fx-border-color: #1e5b9c;" +
                " -fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        activeFilter.setStyle(defaultStyle);
        inactiveFilter.setStyle(defaultStyle);
        availableFilter.setStyle(defaultStyle);
        allFilter.setStyle(selectedStyle);

        activeFilter.setOnAction(event -> {
            try {
                allMonitors.setAll(MoniteurService.filterMoniteurs("A"));
                filteredMonitors.setPredicate(moniteur -> true);
                updateButtonStyles(activeFilter);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de filtrer les moniteurs : " + e.getMessage());
            }
        });

        inactiveFilter.setOnAction(event -> {
            try {
                allMonitors.setAll(MoniteurService.filterMoniteurs("B"));
                filteredMonitors.setPredicate(moniteur -> true);
                updateButtonStyles(inactiveFilter);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de filtrer les moniteurs : " + e.getMessage());
            }
        });

        availableFilter.setOnAction(event -> {
            try {
                allMonitors.setAll(MoniteurService.filterMoniteurs("C"));
                filteredMonitors.setPredicate(moniteur -> true);
                updateButtonStyles(availableFilter);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de filtrer les moniteurs : " + e.getMessage());
            }
        });

        allFilter.setOnAction(event -> {
            try {
                allMonitors.setAll(MoniteurService.filterMoniteurs("ALL"));
                filteredMonitors.setPredicate(moniteur -> true);
                updateButtonStyles(allFilter);
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de filtrer les moniteurs : " + e.getMessage());
            }
        });
    }

    private void updateButtonStyles(ToggleButton selectedButton) {
        String defaultStyle = " -fx-background-color: linear-gradient(to bottom, rgba(255, 255, 255, 0.95), rgba(245, 248, 252, 0.95));" +
                " -fx-text-fill: #1e5b9c;" +
                " -fx-border-color: rgba(30, 91, 156, 0.6);" +
                " -fx-border-width: 1.2px;" +
                " -fx-border-radius: 22px;" +
                " -fx-background-radius: 22px;" +
                " -fx-padding: 12px 22px;" +
                " -fx-cursor: hand;" +
                " -fx-font-weight: 500;" +
                " -fx-font-size: 14px;" +
                " -fx-min-width: 110px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);" +
                " -fx-transition: background-color 0.25s, text-fill 0.25s, effect 0.25s;";
        String selectedStyle = "-fx-text-fill: white;" +
                " -fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                " -fx-border-color: #1e5b9c;" +
                " -fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        activeFilter.setStyle(defaultStyle);
        inactiveFilter.setStyle(defaultStyle);
        availableFilter.setStyle(defaultStyle);
        allFilter.setStyle(defaultStyle);
        selectedButton.setStyle(selectedStyle);
    }

    private void loadMonitors() {
        allMonitors = FXCollections.observableArrayList(MoniteurService.findAll());
        filteredMonitors = new FilteredList<>(allMonitors);
        instructorsTable.setItems(filteredMonitors);
        allFilter.fire();
    }

    private void handleUpdateMoniteur(Moniteur moniteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateMoniteur.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Moniteur");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(instructorsTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            UpdateMoniteurController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMoniteur(moniteur);
            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                loadMonitors();
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleDeleteMoniteur(Moniteur moniteur) {
        showConfirmationAlert("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer le moniteur " + moniteur.getNom() + " " + moniteur.getPrenom() + " ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        MoniteurService.delete(moniteur.getCin());
                        loadMonitors();
                        showSuccessAlert("Succès", "Le moniteur a été supprimé avec succès.");
                    }
                });
    }

    @FXML
    private void openAddInstructorForm() {
        menuController.loadView("addMoniteur.fxml");
    }
}