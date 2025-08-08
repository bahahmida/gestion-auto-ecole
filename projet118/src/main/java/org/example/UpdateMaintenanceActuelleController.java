package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Persistance.models.MaintenanceVehicule;
import Persistance.utils.Alert;
import Service.MaintenanceVehiculeService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.sql.rowset.serial.SerialBlob;

public class UpdateMaintenanceActuelleController {
    @FXML private TextField typeField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField descriptionField;
    @FXML private TextField coutField;
    @FXML private TextField factureField;
    @FXML private Button browseButton;
    @FXML private Button updateButton;
    @FXML private Button closeButton;
    @FXML private Label typeErrorLabel;
    @FXML private Label dateDebutErrorLabel;
    @FXML private Label dateFinErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label coutErrorLabel;
    @FXML private Label factureErrorLabel;

    private Stage dialogStage;
    private MaintenanceVehicule maintenance;
    private boolean okClicked = false;
    private File selectedFactureFile;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMaintenance(MaintenanceVehicule maintenance) {
        this.maintenance = maintenance;
        populateFields();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        dateDebutPicker.setDisable(true);

        typeField.textProperty().addListener((obs, old, newVal) -> validateType());
        dateFinPicker.valueProperty().addListener((obs, old, newVal) -> validateDateFin());
        descriptionField.textProperty().addListener((obs, old, newVal) -> validateDescription());
        coutField.textProperty().addListener((obs, old, newVal) -> validateCout());
        factureField.textProperty().addListener((obs, old, newVal) -> validateFacture());
    }

    private void populateFields() {
        if (maintenance != null) {
            typeField.setText(maintenance.getTypeMaintenance() != null ? maintenance.getTypeMaintenance() : "");
            dateDebutPicker.setValue(maintenance.getDateDebut());
            dateFinPicker.setValue(maintenance.getDateFin());
            descriptionField.setText(maintenance.getDescription() != null ? maintenance.getDescription() : "");
            coutField.setText(maintenance.getCout() != 0 ? String.valueOf(maintenance.getCout()) : "");

            if (maintenance.getFacture() != null) {
                factureField.setText("Facture existante");
                factureField.setDisable(true);
                browseButton.setDisable(true);
            } else {
                factureField.setText("");
                factureField.setEditable(false);
                browseButton.setDisable(false);
            }

            validateType();
            validateDateFin();
            validateDescription();
            validateCout();
            validateFacture();
        }
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une facture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers supportés", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            selectedFactureFile = file;
            factureField.setText(file.getName());
            validateFacture();
        }
    }

    @FXML
    private void handleUpdate() {
        if (areAllFieldsValid()) {
            try {
                maintenance.setTypeMaintenance(typeField.getText());
                maintenance.setDateFin(dateFinPicker.getValue());
                String descriptionText = descriptionField.getText();
                maintenance.setDescription(descriptionText != null && !descriptionText.trim().isEmpty() ? descriptionText : null);
                maintenance.setCout(Double.parseDouble(coutField.getText()));

                if (maintenance.getFacture() == null && selectedFactureFile != null) {
                    if (selectedFactureFile.exists()) {
                        try (FileInputStream fis = new FileInputStream(selectedFactureFile)) {
                            byte[] fileBytes = fis.readAllBytes();
                            Blob factureBlob = new SerialBlob(fileBytes);
                            maintenance.setFacture(factureBlob);
                        } catch (IOException e) {
                            Alert.showErrorAlert("Erreur", "Impossible de lire le fichier facture : " + e.getMessage());
                            return;
                        } catch (SQLException e) {
                            Alert.showErrorAlert("Erreur", "Erreur lors de la création du Blob : " + e.getMessage());
                            return;
                        }
                    } else {
                        Alert.showErrorAlert("Erreur", "Le fichier facture sélectionné n'existe pas.");
                        return;
                    }
                }

                MaintenanceVehiculeService.update(maintenance);

                okClicked = true;
                dialogStage.close();
                Alert.showSuccessAlert("Succès", "Maintenance mise à jour avec succès !");

            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour de la maintenance : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void close() {
        dialogStage.close();
    }

    private void validateType() {
        String typeText = typeField.getText();
        boolean isValid = typeText != null && !typeText.trim().isEmpty();
        typeErrorLabel.setText(isValid ? "" : "Le type est obligatoire");
        typeErrorLabel.setVisible(!isValid);
        typeErrorLabel.setManaged(!isValid);
        typeField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateDateFin() {
        LocalDate date = dateFinPicker.getValue();
        boolean isValid = true;

        if (date == null) {
            dateFinErrorLabel.setText("La date de fin est obligatoire");
            isValid = false;
        } else if (date.isBefore(maintenance.getDateDebut())) {
            dateFinErrorLabel.setText("La date de fin doit être après la date de début");
            isValid = false;
        } else {
            dateFinErrorLabel.setText("");
        }
        dateFinErrorLabel.setVisible(!isValid);
        dateFinErrorLabel.setManaged(!isValid);
        dateFinPicker.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateDescription() {
        descriptionErrorLabel.setText("");
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        descriptionField.setStyle("");
    }

    private void validateCout() {
        String coutText = coutField.getText();
        boolean isValid = true;

        if (coutText == null || coutText.trim().isEmpty()) {
            coutErrorLabel.setText("Le coût est obligatoire");
            isValid = false;
        } else {
            try {
                double cout = Double.parseDouble(coutText.trim());
                if (cout < 0) {
                    coutErrorLabel.setText("Le coût doit être positif");
                    isValid = false;
                } else {
                    coutErrorLabel.setText("");
                }
            } catch (NumberFormatException e) {
                coutErrorLabel.setText("Nombre valide requis");
                isValid = false;
            }
        }
        coutErrorLabel.setVisible(!isValid);
        coutErrorLabel.setManaged(!isValid);
        coutField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateFacture() {
        boolean isValid = true;

        if (maintenance.getFacture() == null && selectedFactureFile != null) {
            String fileName = selectedFactureFile.getName().toLowerCase();
            if (!fileName.endsWith(".pdf") && !fileName.endsWith(".jpg") &&
                    !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
                factureErrorLabel.setText("Le fichier doit être un PDF ou une image (JPG, JPEG, PNG).");
                isValid = false;
            } else if (!selectedFactureFile.exists()) {
                factureErrorLabel.setText("Le fichier n'existe pas");
                isValid = false;
            } else {
                factureErrorLabel.setText("");
            }
        } else {
            factureErrorLabel.setText("");
        }
        factureErrorLabel.setVisible(!isValid);
        factureErrorLabel.setManaged(!isValid);
        factureField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private boolean areAllFieldsValid() {
        validateType();
        validateDateFin();
        validateDescription();
        validateCout();
        validateFacture();

        return typeErrorLabel.getText().isEmpty() &&
                dateFinErrorLabel.getText().isEmpty() &&
                descriptionErrorLabel.getText().isEmpty() &&
                coutErrorLabel.getText().isEmpty() &&
                factureErrorLabel.getText().isEmpty();
    }
}