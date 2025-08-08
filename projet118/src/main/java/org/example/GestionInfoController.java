package org.example;

import Persistance.models.AutoEcole;
import Service.AutoEcoleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class GestionInfoController {

    @FXML private TableView<String[]> autoEcoleTable;
    @FXML private TableColumn<String[], String> fieldColumn;
    @FXML private TableColumn<String[], String> valueColumn;
    @FXML private Button addInfoButton;
    @FXML private Button modifyInfoButton;
    @FXML private Button modifyPasswordButton;

    private ObservableList<String[]> autoEcoleData = FXCollections.observableArrayList();
    private MenuController menuController;
    private Stage primaryStage;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        // Configure TableView columns
        fieldColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[0]));
        valueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1]));

        // Load auto-école data and update button states
        loadAutoEcoleData();

        // Bind data to TableView
        autoEcoleTable.setItems(autoEcoleData);
    }

    // Open the appropriate window based on database state
    public void openInitialWindow() throws IOException {
        if (primaryStage == null) {
            showAlert("Erreur", "Stage non initialisé", "Le Stage principal n'a pas été défini.");
            return;
        }

        int rowCount = AutoEcoleService.getRowCount();
        if (rowCount == 0) {
            handleAddInfo();
        } else {
            handleModifyInfo();
        }
    }

    private void loadAutoEcoleData() {
        autoEcoleData.clear(); // Clear existing data

        int rowCount = AutoEcoleService.getRowCount();
        if (rowCount == 0) {
            // No records in the database, show default values
            autoEcoleData.add(new String[]{"Nom Auto ecole", "Non défini"});
            autoEcoleData.add(new String[]{"Numéro Téléphone", "Non défini"});
            autoEcoleData.add(new String[]{"Adresse", "Non défini"});
            autoEcoleData.add(new String[]{"Email", "Non défini"});

            // ACTIVER "Ajouter", DÉSACTIVER "Modifier" et "Modifier mot de passe"
            addInfoButton.setDisable(false);
            modifyInfoButton.setDisable(true);
            modifyPasswordButton.setDisable(true);
        } else {
            // Fetch and display actual data
            AutoEcole autoEcole = AutoEcoleService.find();
            if (autoEcole == null) {
                // Handle unexpected null case
                autoEcoleData.add(new String[]{"Nom Auto ecole", "Non défini"});
                autoEcoleData.add(new String[]{"Numéro Téléphone", "Non défini"});
                autoEcoleData.add(new String[]{"Adresse", "Non défini"});
                autoEcoleData.add(new String[]{"Email", "Non défini"});

                // ACTIVER "Ajouter", DÉSACTIVER "Modifier" et "Modifier mot de passe"
                addInfoButton.setDisable(false);
                modifyInfoButton.setDisable(true);
                modifyPasswordButton.setDisable(true);
            } else {
                // Ajouter les données dans le tableau
                autoEcoleData.add(new String[]{"Nom Auto ecole", autoEcole.getNom() != null && !autoEcole.getNom().trim().isEmpty() ? autoEcole.getNom() : "Non défini"});
                autoEcoleData.add(new String[]{"Numéro Téléphone", autoEcole.getNumTel() != 0 ? String.valueOf(autoEcole.getNumTel()) : "Non défini"});
                autoEcoleData.add(new String[]{"Adresse", autoEcole.getAdresse() != null && !autoEcole.getAdresse().trim().isEmpty() ? autoEcole.getAdresse() : "Non défini"});
                autoEcoleData.add(new String[]{"Email", autoEcole.getEmail() != null && !autoEcole.getEmail().trim().isEmpty() ? autoEcole.getEmail() : "Non défini"});

                // DÉSACTIVER "Ajouter", ACTIVER "Modifier" et "Modifier mot de passe"
                addInfoButton.setDisable(true);
                modifyInfoButton.setDisable(false);
                modifyPasswordButton.setDisable(false);
            }
        }

        // Refresh TableView
        autoEcoleTable.setItems(autoEcoleData);
        autoEcoleTable.refresh();
    }

    @FXML
    public void handleAddInfo() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gestionInfoAdd.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Ajouter Informations Auto-École");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        GestionInfoAddController controller = loader.getController();
        controller.setDialogStage(dialogStage);

        dialogStage.showAndWait();
        loadAutoEcoleData();  // Recharger les données après ajout
    }

    @FXML
    public void handleModifyInfo() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gestionInfoUpdate.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifier Informations Auto-École");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        GestionInfoUpdateController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.initData(); // Ajouter cet appel pour remplir les champs avec les anciennes valeurs

        dialogStage.showAndWait();
        loadAutoEcoleData();  // Recharger les données après modification
    }

    @FXML
    public void handleModifyPassword() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateMotDePasse.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Modifier le mot de passe");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        UpdateMotDePasseController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.initData();

        dialogStage.showAndWait();
        loadAutoEcoleData();  // Recharger les données après modification
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}