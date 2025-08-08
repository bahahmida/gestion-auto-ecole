package org.example;

import Persistance.models.MaintenanceVehicule;
import Persistance.models.Vehicule;
import Service.MaintenanceVehiculeService;
import Service.VehiculeService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static Persistance.utils.Alert.*;

public class HistoriqueMaintenanceVehiculeController {
    @FXML private TableView<MaintenanceVehicule> entretienTable;
    @FXML private TableColumn<MaintenanceVehicule, ImageView> iconMaintenanceColumn;
    @FXML private TableColumn<MaintenanceVehicule, Integer> idEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, String> idVehiculeColumn;
    @FXML private TableColumn<MaintenanceVehicule, String> typeEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, LocalDate> dateEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, LocalDate> dateEntretienColumn1;
    @FXML private TableColumn<MaintenanceVehicule, Double> coutColumn;
    @FXML private TableColumn<MaintenanceVehicule, Blob> factureColumn;
    @FXML private TableColumn<MaintenanceVehicule, MaintenanceVehicule> actionColumn;
    @FXML private Button closeButton;

    private Stage dialogStage;
    private Vehicule vehicule;
    private ObservableList<MaintenanceVehicule> allMaintenances;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        loadMaintenances();
    }

    @FXML
    private void initialize() {
        configureColumns();
        allMaintenances = FXCollections.observableArrayList();
        entretienTable.setItems(allMaintenances);
    }

    private void configureColumns() {
        if (iconMaintenanceColumn == null || actionColumn == null) {
            return;
        }

        iconMaintenanceColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView;

            {
                InputStream imageStream = getClass().getResourceAsStream("/images/vehicule.jpg");
                imageView = imageStream != null ? new ImageView(new Image(imageStream)) : new ImageView();
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        idEntretienColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdMaintenance()).asObject());

        idVehiculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(VehiculeService.getImmatriculationById(cellData.getValue().getIdVehicule())));

        typeEntretienColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeMaintenance()));
        dateEntretienColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateDebut()));
        dateEntretienColumn1.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateFin()));
        coutColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCout()).asObject());

        factureColumn.setCellFactory(column -> new TableCell<>() {
            private final Button factureButton = createIconButton("/images/facture.jpg");

            {
                factureButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    openFactureFile(maintenance);
                });
            }

            @Override
            protected void updateItem(Blob item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                setText(null);

                if (empty) {
                    return;
                }

                MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                if (maintenance == null) {
                    return;
                }

                Blob facture = maintenance.getFacture();
                if (facture == null) {
                    setText("Aucune facture");
                    return;
                }

                setGraphic(factureButton);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton("/images/edit.png");
            private final Button deleteButton = createIconButton("/images/delete.png");
            private final HBox actionBox;

            {
                actionBox = new HBox(10, editButton, deleteButton);
                actionBox.setAlignment(javafx.geometry.Pos.CENTER);

                editButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    openUpdateMaintenanceForm(maintenance);
                });

                deleteButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    handleDeleteMaintenance(maintenance);
                });
            }

            @Override
            protected void updateItem(MaintenanceVehicule maintenance, boolean empty) {
                super.updateItem(maintenance, empty);
                setGraphic(empty ? null : actionBox);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
    }

    private void loadMaintenances() {
        if (vehicule == null) {
            entretienTable.setPlaceholder(new Label("Aucun véhicule sélectionné"));
            return;
        }

        try {
            List<MaintenanceVehicule> maintenanceList = MaintenanceVehiculeService.getHistoricalMaintenancesByIdVehiculeId(vehicule.getIdVehicule());
            if (maintenanceList != null && !maintenanceList.isEmpty()) {
                allMaintenances.setAll(maintenanceList);
            } else {
                allMaintenances.clear();
                entretienTable.setPlaceholder(new Label("Aucune maintenance trouvée pour " + vehicule.getImmatriculation()));
            }
        } catch (Exception e) {
            allMaintenances.clear();
            entretienTable.setPlaceholder(new Label("Erreur lors du chargement des données"));
        }
    }

    private Button createIconButton(String imagePath) {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        ImageView icon = imageStream != null ? new ImageView(new Image(imageStream)) : new ImageView();
        icon.setFitHeight(28);
        icon.setFitWidth(28);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private void openFactureFile(MaintenanceVehicule maintenance) {
        try {
            Blob facture = maintenance.getFacture();
            if (facture == null) {
                showInformationAlert("Aucune facture", "Cette maintenance n'a pas de facture associée.");
                return;
            }

            if (facture.length() == 0) {
                showInformationAlert("Facture vide", "Cette facture est vide.");
                return;
            }

            byte[] factureBytes = facture.getBytes(1, (int) facture.length());

            if (isPDF(factureBytes)) {
                File tempFile = File.createTempFile("facture_" + maintenance.getIdMaintenance(), ".pdf");
                Files.write(tempFile.toPath(), factureBytes);

                if (tempFile.exists() && tempFile.length() > 0) {
                    java.awt.Desktop.getDesktop().open(tempFile);
                    tempFile.deleteOnExit();
                } else {
                    showErrorAlert("Erreur", "Le fichier temporaire de la facture est vide ou n'existe pas.");
                }
            } else if (isImage(factureBytes)) {
                Image image = new Image(new ByteArrayInputStream(factureBytes));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(600);
                imageView.setFitHeight(400);
                imageView.setPreserveRatio(true);

                Stage stage = new Stage();
                stage.setTitle("Facture - Visualisation");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(new HBox(imageView)));
                stage.showAndWait();
            } else {
                showErrorAlert("Erreur", "Format de facture non supporté (ni PDF ni image).");
            }

        } catch (SQLException | IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la facture : " + e.getMessage());
        }
    }

    private void openUpdateMaintenanceForm(MaintenanceVehicule maintenance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateMaintenanceActuelle.fxml"));
            Parent root = loader.load();

            UpdateMaintenanceActuelleController controller = loader.getController();
            controller.setMaintenance(maintenance);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier la Maintenance - ID " + maintenance.getIdMaintenance());
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setDialogStage(stage);

            stage.showAndWait();

            if (controller.isOkClicked()) {
                loadMaintenances();
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Erreur lors du chargement du formulaire de modification : " + e.getMessage());
        }
    }

    private void handleDeleteMaintenance(MaintenanceVehicule maintenance) {
        showConfirmationAlert("Confirmation de suppression", "Êtes-vous sûr de vouloir supprimer cette maintenance ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            MaintenanceVehiculeService.delete(maintenance.getIdMaintenance());
                            loadMaintenances();
                            showSuccessAlert("Succès", "Maintenance supprimée avec succès !");
                        } catch (Exception e) {
                            showErrorAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                        }
                    }
                });
    }

    private boolean isPDF(byte[] bytes) {
        return bytes.length > 4 &&
                bytes[0] == (byte)0x25 && // %
                bytes[1] == (byte)0x50 && // P
                bytes[2] == (byte)0x44 && // D
                bytes[3] == (byte)0x46;  // F
    }

    private boolean isImage(byte[] bytes) {
        boolean isJPEG = bytes.length > 2 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8;
        boolean isPNG = bytes.length > 4 && bytes[0] == (byte)0x89 && bytes[1] == (byte)0x50 &&
                bytes[2] == (byte)0x4E && bytes[3] == (byte)0x47;
        return isJPEG || isPNG;
    }

    @FXML
    private void close() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}