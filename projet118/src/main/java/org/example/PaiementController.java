package org.example;

import Persistance.models.Candidat;
import Service.PaiementService;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

import static Persistance.utils.Alert.showErrorAlert;


public class PaiementController {

    @FXML private TextField searchField;
    @FXML private TableView<Candidat> paiementTable;
    @FXML private TableColumn<Candidat, Void> iconColumn;
    @FXML private TableColumn<Candidat, Integer> cinColumn;
    @FXML private TableColumn<Candidat, String> nameColumn;
    @FXML private TableColumn<Candidat, String> paymentColumn;
    @FXML private TableColumn<Candidat, Void> historiquePaiementColumn;
    @FXML private TableColumn<Candidat, Void> actionsColumn;

    private MenuController menuController;
    private ObservableList<Candidat> allCandidates;
    private FilteredList<Candidat> filteredCandidates;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        loadNonPaidCandidates();
        setupSearchListener();
    }

    private void setupTableColumns() {
        iconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/money.png")));
            {
                imageView.setFitHeight(35);
                imageView.setFitWidth(35);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(Pos.CENTER);
            }
        });

        cinColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCin()).asObject());
        nameColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            String fullName = candidat.getNom() + " " + candidat.getPrenom();
            return new SimpleStringProperty(fullName);
        });

        paymentColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue();
            String paymentText = String.format("%.2f/%.2f", candidat.getMontant_paye(), candidat.getMontant_total());
            return new SimpleStringProperty(paymentText);
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
                    if (paidAmount >= totalAmount) {
                        label.setStyle("-fx-text-fill: green;");
                        dot.setFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        label.setStyle("-fx-text-fill: red;");
                        dot.setFill(javafx.scene.paint.Color.RED);
                    }
                    setGraphic(hbox);
                }
            }
        });

        historiquePaiementColumn.setCellFactory(column -> new TableCell<>() {
            private final Button historyButton = new Button();
            private final ImageView historyIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/historique.jpg")));
            {
                historyIcon.setFitHeight(28);
                historyIcon.setFitWidth(28);
                historyButton.setGraphic(historyIcon);
                historyButton.setStyle("-fx-background-color: transparent;");
                historyButton.setOnAction(event -> {
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    handleViewHistory(candidat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : historyButton);
                setAlignment(Pos.CENTER);
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button addPaiementButton = new Button("Ajouter Paiement");
            {
                addPaiementButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #1e5b9c, #134673);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10px;" +
                                "-fx-padding: 12px 18px;" +
                                "-fx-font-size: 10px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.18), 5, 0.2, 0, 2);" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: rgba(255, 255, 255, 0.25);" +
                                "-fx-border-radius: 10px;" +
                                "-fx-border-width: 1.2px;" +
                                "-fx-transition: background-color 0.25s, effect 0.25s, translate-y 0.1s;"
                );
                addPaiementButton.setOnAction(event -> {
                    Candidat candidat = getTableView().getItems().get(getIndex());
                    handleAddPaiement(candidat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addPaiementButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupSearchListener() {
        filteredCandidates = new FilteredList<>(allCandidates, candidat -> true);
        paiementTable.setItems(filteredCandidates);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.trim().toLowerCase();
            try {
                filteredCandidates.setPredicate(candidat ->
                        String.valueOf(candidat.getCin()).contains(searchText) ||
                                (candidat.getNom() != null && candidat.getNom().toLowerCase().contains(searchText)) ||
                                (candidat.getPrenom() != null && candidat.getPrenom().toLowerCase().contains(searchText))
                );
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de la recherche des candidats : " + e.getMessage());
            }
        });
    }

    public void loadNonPaidCandidates() {
        try {
            List<Candidat> allCandidatesList = PaiementService.getCandidatNonPaye();
            allCandidates = FXCollections.observableArrayList(allCandidatesList);
            filteredCandidates = new FilteredList<>(allCandidates, candidat -> true);
            paiementTable.setItems(filteredCandidates);
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors du chargement des candidats : " + e.getMessage());
            allCandidates = FXCollections.observableArrayList();
            filteredCandidates = new FilteredList<>(allCandidates, candidat -> true);
            paiementTable.setItems(filteredCandidates);
        }
    }

    public void refreshTableView() {
        loadNonPaidCandidates();
    }

    private void handleViewHistory(Candidat candidat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/HistoriquePaiment.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Historique des Paiements - " + candidat.getNom() + " " + candidat.getPrenom());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(paiementTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            HistoriquePaiementController controller = loader.getController();
            controller.setCinCandidat(candidat.getCin());
            controller.setPaiementController(this);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Échec de l'ouverture de l'historique : " + e.getMessage());
        }
    }

    private void handleAddPaiement(Candidat candidat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addPaiment.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un Paiement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(paiementTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            AddPaimentController controller = loader.getController();
            controller.setCinCandidat(candidat.getCin());
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadNonPaidCandidates();
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Échec de l'ouverture de l'ajout : " + e.getMessage());
        }
    }
}