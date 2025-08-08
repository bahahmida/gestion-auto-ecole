package org.example;

import Persistance.models.Candidat;
import Service.CandidatService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import javafx.geometry.Pos;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;
import static Persistance.utils.Alert.showConfirmationAlert; // Ajout de l'import

public class CandidatController {
    @FXML private TextField searchField;
    @FXML private TableView<Candidat> candidatesTable;
    @FXML private TableColumn<Candidat, String> photoColumn;
    @FXML private TableColumn<Candidat, String> nameColumn;
    @FXML private TableColumn<Candidat, String> typeColumn;
    @FXML private TableColumn<Candidat, String> statusColumn;
    @FXML private TableColumn<Candidat, String> phoneColumn;
    @FXML private TableColumn<Candidat, String> sessionsColumn;
    @FXML private TableColumn<Candidat, String> paymentColumn;
    @FXML private TableColumn<Candidat, Void> documentsColumn;
    @FXML private TableColumn<Candidat, Void> actionsColumn;
    @FXML private ToggleButton activeFilter;
    @FXML private ToggleButton inactiveFilter;
    @FXML private ToggleButton nonPayesFilter;
    @FXML private ToggleButton allFilter;

    private CandidatService candidatService;
    private ObservableList<Candidat> allCandidats;
    private FilteredList<Candidat> filteredCandidats;
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML
    public void initialize() {
        candidatService = new CandidatService();
        setupTableColumns();
        loadCandidats();
        updateButtonStyles(allFilter);
        setupSearchListener();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.trim().toLowerCase();
            try {
                if (searchText.isEmpty()) {
                    filteredCandidats.setPredicate(candidat -> true);
                } else {
                    filteredCandidats.setPredicate(candidat ->
                            (candidat.getNom() != null && candidat.getNom().toLowerCase().contains(searchText)) ||
                                    (candidat.getPrenom() != null && candidat.getPrenom().toLowerCase().contains(searchText)) ||
                                    String.valueOf(candidat.getCin()).contains(searchText)
                    );
                }
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de la recherche des candidats : " + e.getMessage());
            }
        });
    }

    private void setupTableColumns() {
        photoColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/candidat.jpg")));

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

        nameColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            return new SimpleStringProperty(candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "");
        });

        typeColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            return new SimpleStringProperty(candidat != null ? candidat.getCategorie() : "");
        });
        typeColumn.setCellFactory(column -> new TableCell<Candidat, String>() {
            private final Circle circle = new Circle(12);
            private final Label label = new Label();
            private final StackPane stackPane = new StackPane(circle, label);
            private final LinearGradient blueGradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.web("#1e5b9c")),
                    new Stop(1, javafx.scene.paint.Color.web("#174e85"))
            );

            {
                circle.setFill(blueGradient);
                label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                stackPane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(getItem(), empty);
                if (empty || category == null || category.isEmpty()) {
                    setGraphic(null);
                } else {
                    label.setText(category.substring(0, 1).toUpperCase());
                    setGraphic(stackPane);
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            return new SimpleStringProperty(candidat != null ? candidat.getEtat() : "");
        });

        phoneColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            return new SimpleStringProperty(candidat != null ? candidat.getTelephone() : "");
        });

        sessionsColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            if (candidat != null) {
                return new SimpleStringProperty(String.format("%d/%d",
                        candidat.getSeances_effectuees(),
                        candidat.getSeances_totales()));
            }
            return new SimpleStringProperty("");
        });

        paymentColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            if (candidat != null) {
                return new SimpleStringProperty(String.format("%.2f/%.2f",
                        candidat.getMontant_paye(),
                        candidat.getMontant_total()));
            }
            return new SimpleStringProperty("");
        });
        paymentColumn.setCellFactory(column -> new TableCell<Candidat, String>() {
            private final Circle dot = new Circle(5);
            private final Label label = new Label();
            private final HBox hbox = new HBox(5, dot, label);

            {
                hbox.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    label.setText(payment);
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    double paidAmount = candidat.getMontant_paye();
                    double totalAmount = candidat.getMontant_total();
                    if (paidAmount == totalAmount) {
                        label.setStyle("-fx-text-fill: green; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
                        dot.setFill(javafx.scene.paint.Color.GREEN);
                    } else if (totalAmount > paidAmount) {
                        label.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
                        dot.setFill(javafx.scene.paint.Color.RED);
                    } else {
                        label.setStyle("");
                        dot.setFill(javafx.scene.paint.Color.BLACK);
                    }
                    setGraphic(hbox);
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editIcon.setFitHeight(28);
                editIcon.setFitWidth(28);
                deleteIcon.setFitHeight(28);
                deleteIcon.setFitWidth(28);
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");

                editButton.setOnAction(event -> {
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    handleUpdateCandidat(candidat);
                });

                deleteButton.setOnAction(event -> {
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    handleDeleteCandidat(candidat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
                setAlignment(Pos.CENTER);
            }
        });

        documentsColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView docIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/document.png")));
            private final Button documentsButton = new Button();

            {
                docIcon.setFitHeight(27);
                docIcon.setFitWidth(27);
                documentsButton.setGraphic(docIcon);
                documentsButton.setStyle("-fx-background-color: transparent;");
                documentsButton.setOnAction(event -> {
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    handleViewDocuments(candidat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : documentsButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    @FXML
    private void handleActiveFilter() {
        try {
            allCandidats.setAll(candidatService.filterCandidats("ACTIF"));
            filteredCandidats.setPredicate(candidat -> true);
            updateButtonStyles(activeFilter);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du filtrage des candidats : " + e.getMessage());
        }
    }

    @FXML
    private void handleInactiveFilter() {
        try {
            allCandidats.setAll(candidatService.filterCandidats("INACTIF"));
            filteredCandidats.setPredicate(candidat -> true);
            updateButtonStyles(inactiveFilter);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du filtrage des candidats : " + e.getMessage());
        }
    }

    @FXML
    private void handleNonPayesFilter() {
        try {
            allCandidats.setAll(candidatService.filterCandidats("NON_PAYE"));
            filteredCandidats.setPredicate(candidat -> true);
            updateButtonStyles(nonPayesFilter);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du filtrage des candidats : " + e.getMessage());
        }
    }

    @FXML
    private void handleAllFilter() {
        try {
            allCandidats.setAll(candidatService.filterCandidats("TOUT"));
            filteredCandidats.setPredicate(candidat -> true);
            updateButtonStyles(allFilter);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du filtrage des candidats : " + e.getMessage());
        }
    }

    private void updateButtonStyles(ToggleButton selectedButton) {
        String defaultStyle = " -fx-background-color: linear-gradient(to bottom, rgba(255, 255, 255, 0.95), rgba(245, 248, 252, 0.95));\n" +
                "    -fx-text-fill: #1e5b9c;\n" +
                "    -fx-border-color: rgba(30, 91, 156, 0.6);\n" +
                "    -fx-border-width: 1.2px;\n" +
                "    -fx-border-radius: 22px;\n" +
                "    -fx-background-radius: 22px;\n" +
                "    -fx-padding: 12px 22px;\n" +
                "    -fx-cursor: hand;\n" +
                "    -fx-font-weight: 500;\n" +
                "    -fx-font-size: 14px;\n" +
                "    -fx-min-width: 110px;\n" +
                "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);\n" +
                "    -fx-transition: background-color 0.25s, text-fill 0.25s, effect 0.25s;";
        String selectedStyle = "-fx-text-fill: white;\n" +
                "    -fx-background-color: linear-gradient(to left, #1e5b9c, #134673);\n" +
                "    -fx-border-color: #1e5b9c;\n" +
                "    -fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        activeFilter.setStyle(defaultStyle);
        inactiveFilter.setStyle(defaultStyle);
        nonPayesFilter.setStyle(defaultStyle);
        allFilter.setStyle(defaultStyle);
        selectedButton.setStyle(selectedStyle);
    }

    private void loadCandidats() {
        try {
            allCandidats = FXCollections.observableArrayList(candidatService.getAllCandidats());
            filteredCandidats = new FilteredList<>(allCandidats);
            candidatesTable.setItems(filteredCandidats);
            handleAllFilter();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Échec du chargement des candidats : " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCandidat() {
        menuController.loadView("addCandidat.fxml");
    }

    private void handleUpdateCandidat(Candidat candidat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateCandidat.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Candidat");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(candidatesTable.getScene().getWindow());
            Scene scene = new Scene(page, 550, 600);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            UpdateCandidatController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCandidat(candidat);
            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                loadCandidats();
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleDeleteCandidat(Candidat candidat) {
        showConfirmationAlert("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer " + candidat.getNom() + " " + candidat.getPrenom() + " ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            candidatService.deleteCandidat(candidat.getCin());
                            loadCandidats();
                            showSuccessAlert("Succès", "Le candidat a été supprimé avec succès.");
                        } catch (SQLException e) {
                            showErrorAlert("Erreur", "Échec de la suppression du candidat : " + e.getMessage());
                        }
                    }
                });
    }

    private void handleViewDocuments(Candidat candidat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/candidatDocument.fxml"));
            Parent root = loader.load();
            DocumentController controller = loader.getController();
            controller.setCandidat(candidat);
            Stage stage = new Stage();
            stage.setTitle("Documents du Candidat");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Échec de l'ouverture de la fenêtre des documents : " + e.getMessage());
        }
    }
}