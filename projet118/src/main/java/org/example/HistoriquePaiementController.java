package org.example;

import Persistance.models.AutoEcole;
import Persistance.models.Candidat;
import Persistance.models.Paiement;
import Persistance.utils.Alert; // Import ajouté pour utiliser les méthodes d'alerte
import Service.CandidatService;
import Service.PaiementService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class HistoriquePaiementController {

    @FXML private TableView<Paiement> paiementTable;
    @FXML private TableColumn<Paiement, Void> iconPaiementColumn;
    @FXML private TableColumn<Paiement, Double> montantPaiementColumn;
    @FXML private TableColumn<Paiement, LocalDate> datePaiementColumn;
    @FXML private TableColumn<Paiement, String> descriptionColumn;
    @FXML private TableColumn<Paiement, Void> actionColumn;

    private ObservableList<Paiement> paiementsList;
    private PaiementService paiementService;
    private CandidatService candidatService;
    private int cinCandidat;
    private PaiementController paiementController;
    private AutoEcole autoEcole;

    public void setCinCandidat(int cinCandidat) {
        this.cinCandidat = cinCandidat;
        loadPaiements();
    }

    public void setPaiementController(PaiementController paiementController) {
        this.paiementController = paiementController;
    }

    public void setAutoEcole(AutoEcole autoEcole) {
        this.autoEcole = autoEcole;
    }

    @FXML
    private void initialize() {
        paiementService = new PaiementService();
        candidatService = new CandidatService();
        paiementsList = FXCollections.observableArrayList();
        setupTableColumns();
    }

    private void setupTableColumns() {
        iconPaiementColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/facture.jpg")));
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

        montantPaiementColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMontant()).asObject());
        datePaiementColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDatePaiement()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button printButton = new Button();
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            private final ImageView printIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/print.jpg")));
            private final HBox actionsBox = new HBox(5, editButton, deleteButton, printButton);

            {
                editIcon.setFitHeight(28);
                editIcon.setFitWidth(28);
                deleteIcon.setFitHeight(28);
                deleteIcon.setFitWidth(28);
                printIcon.setFitHeight(28);
                printIcon.setFitWidth(28);

                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);
                printButton.setGraphic(printIcon);

                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                printButton.setStyle("-fx-background-color: transparent;");

                editButton.setOnAction(event -> {
                    Paiement paiement = getTableView().getItems().get(getIndex());
                    handleEditPaiement(paiement);
                });
                deleteButton.setOnAction(event -> {
                    Paiement paiement = getTableView().getItems().get(getIndex());
                    handleDeletePaiement(paiement);
                });
                printButton.setOnAction(event -> {
                    Paiement paiement = getTableView().getItems().get(getIndex());
                    handlePrintReceipt(paiement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionsBox);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void loadPaiements() {
        paiementsList.clear();
        List<Paiement> paiements = paiementService.getPaiementsByCinCandidat(cinCandidat);
        paiementsList.addAll(paiements);
        paiementTable.setItems(paiementsList);
    }

    private void handleEditPaiement(Paiement paiement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updatePaiement.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Paiement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(paiementTable.getScene().getWindow());
            Scene scene = new Scene(page, 584, 580);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            UpdatePaiementController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPaiement(paiement);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadPaiements();
                if (paiementController != null) {
                    paiementController.refreshTableView();
                }
            }
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleDeletePaiement(Paiement paiement) {
        Alert.showConfirmationAlert("Confirmation de suppression", "Êtes-vous sûr de vouloir supprimer ce paiement de " + paiement.getMontant() + " TND ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            Candidat candidat = candidatService.getCandidat(cinCandidat);
                            if (candidat == null) {
                                throw new Exception("Candidat introuvable pour le CIN : " + cinCandidat);
                            }

                            paiementService.deletePaiement(paiement.getId());

                            double nouveauMontantPaye = candidat.getMontant_paye() - paiement.getMontant();
                            if (nouveauMontantPaye < 0) {
                                nouveauMontantPaye = 0;
                            }
                            candidat.setMontant_paye(nouveauMontantPaye);

                            candidatService.updateCandidat(candidat);

                            loadPaiements();

                            if (paiementController != null) {
                                paiementController.refreshTableView();
                            }

                            Alert.showSuccessAlert("Succès", "Le paiement a été supprimé avec succès et le candidat a été mis à jour.");
                        } catch (Exception e) {
                            Alert.showErrorAlert("Erreur", "Erreur lors de la suppression du paiement ou de la mise à jour du candidat : " + e.getMessage());
                        }
                    }
                });
    }

    private void handlePrintReceipt(Paiement paiement) {
        try {
            String directoryPath = "./files_pdf";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filePath = directoryPath + "/recu_paiement.pdf";

            PaiementService.generateRecuPDF(PaiementService.find(), paiement, filePath);

            Alert.showSuccessAlert("Succès", "Le reçu a été généré avec succès.");
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de la génération du reçu : " + e.getMessage());
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) paiementTable.getScene().getWindow();
        stage.close();
    }
}