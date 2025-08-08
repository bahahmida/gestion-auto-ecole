package org.example;

import Persistance.models.MaintenanceVehicule;
import Persistance.models.Vehicule;
import Service.MaintenanceVehiculeService;
import Service.VehiculeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class AddMaintenanceController {
    private Vehicule vehicule;
    private File selectedFactureFile;

    @FXML private TextField typeField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField descriptionField;
    @FXML private TextField coutField;
    @FXML private TextField factureField;
    @FXML private Button browseButton;
    @FXML private Label typeErrorLabel;
    @FXML private Label dateDebutErrorLabel;
    @FXML private Label dateFinErrorLabel;
    @FXML private Label coutErrorLabel;
    @FXML private Label factureErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    @FXML
    private void initialize() {
        if (typeField != null) typeField.textProperty().addListener((obs, old, newVal) -> validateType());
        if (dateDebutPicker != null) dateDebutPicker.valueProperty().addListener((obs, old, newVal) -> validateDateDebut());
        if (dateFinPicker != null) dateFinPicker.valueProperty().addListener((obs, old, newVal) -> validateDateFin());
        if (coutField != null) coutField.textProperty().addListener((obs, old, newVal) -> validateCout());
        if (factureField != null) factureField.textProperty().addListener((obs, old, newVal) -> validateFacture());
        if (descriptionField != null) descriptionField.textProperty().addListener((obs, old, newVal) -> validateDescription());

        clearErrorLabels();
    }

    private void clearErrorLabels() {
        if (typeErrorLabel != null) {
            typeErrorLabel.setText("");
            typeErrorLabel.setVisible(false);
            typeErrorLabel.setManaged(false);
        }
        if (dateDebutErrorLabel != null) {
            dateDebutErrorLabel.setText("");
            dateDebutErrorLabel.setVisible(false);
            dateDebutErrorLabel.setManaged(false);
        }
        if (dateFinErrorLabel != null) {
            dateFinErrorLabel.setText("");
            dateFinErrorLabel.setVisible(false);
            dateFinErrorLabel.setManaged(false);
        }
        if (coutErrorLabel != null) {
            coutErrorLabel.setText("");
            coutErrorLabel.setVisible(false);
            coutErrorLabel.setManaged(false);
        }
        if (factureErrorLabel != null) {
            factureErrorLabel.setText("");
            factureErrorLabel.setVisible(false);
            factureErrorLabel.setManaged(false);
        }
        if (descriptionErrorLabel != null) {
            descriptionErrorLabel.setText("");
            descriptionErrorLabel.setVisible(false);
            descriptionErrorLabel.setManaged(false);
        }
    }

    private void validateType() {
        if (typeField == null) return;
        String type = typeField.getText().trim();
        boolean isValid = !type.isEmpty();
        typeErrorLabel.setText(isValid ? "" : "Le type est requis.");
        typeErrorLabel.setVisible(!isValid);
        typeErrorLabel.setManaged(!isValid);
        typeField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateDateDebut() {
        if (dateDebutPicker == null) return;
        LocalDate dateDebut = dateDebutPicker.getValue();
        boolean isValid = dateDebut != null;

        if (!isValid) {
            dateDebutErrorLabel.setText("La date de début est requise.");
            dateDebutErrorLabel.setVisible(true);
            dateDebutErrorLabel.setManaged(true);
            dateDebutPicker.setStyle("-fx-border-color: red;");
        } else {
            try {
                if (vehicule != null && MaintenanceVehiculeService.maintenanceExists(vehicule.getIdVehicule(), dateDebut)) {
                    dateDebutErrorLabel.setText("Une maintenance existe déjà à cette date.");
                    dateDebutErrorLabel.setVisible(true);
                    dateDebutErrorLabel.setManaged(true);
                    dateDebutPicker.setStyle("-fx-border-color: red;");
                    isValid = false;
                } else {
                    dateDebutErrorLabel.setText("");
                    dateDebutErrorLabel.setVisible(false);
                    dateDebutErrorLabel.setManaged(false);
                    dateDebutPicker.setStyle("");
                }
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Erreur lors de la vérification : " + e.getMessage());
                return;
            }
        }

        if (isValid) validateDateFin();
    }

    private void validateDateFin() {
        if (dateFinPicker == null || dateDebutPicker == null) return;
        LocalDate dateFin = dateFinPicker.getValue();
        LocalDate dateDebut = dateDebutPicker.getValue();
        boolean isValid = true;

        if (dateFin == null) {
            dateFinErrorLabel.setText("La date de fin est requise.");
            dateFinErrorLabel.setVisible(true);
            dateFinErrorLabel.setManaged(true);
            isValid = false;
        } else if (dateDebut != null && dateFin.isBefore(dateDebut)) {
            dateFinErrorLabel.setText("La date de fin doit être après la date de début.");
            dateFinErrorLabel.setVisible(true);
            dateFinErrorLabel.setManaged(true);
            isValid = false;
        } else {
            dateFinErrorLabel.setText("");
            dateFinErrorLabel.setVisible(false);
            dateFinErrorLabel.setManaged(false);
        }
        dateFinPicker.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateCout() {
        if (coutField == null) return;
        String coutText = coutField.getText().trim();
        boolean isValid = true;

        if (coutText.isEmpty()) {
            coutErrorLabel.setText("Le coût est requis.");
            coutErrorLabel.setVisible(true);
            coutErrorLabel.setManaged(true);
            isValid = false;
        } else {
            try {
                double cout = Double.parseDouble(coutText);
                if (cout < 0) {
                    coutErrorLabel.setText("Le coût doit être positif.");
                    coutErrorLabel.setVisible(true);
                    coutErrorLabel.setManaged(true);
                    isValid = false;
                } else {
                    coutErrorLabel.setText("");
                    coutErrorLabel.setVisible(false);
                    coutErrorLabel.setManaged(false);
                }
            } catch (NumberFormatException e) {
                coutErrorLabel.setText("Le coût doit être un nombre valide.");
                coutErrorLabel.setVisible(true);
                coutErrorLabel.setManaged(true);
                isValid = false;
            }
        }
        coutField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateFacture() {
        if (factureField == null) return;
        boolean isValid = true;

        if (selectedFactureFile != null) {
            String fileName = selectedFactureFile.getName().toLowerCase();
            if (!fileName.endsWith(".pdf") && !fileName.endsWith(".jpg") &&
                    !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
                factureErrorLabel.setText("Le fichier doit être un PDF ou une image (JPG, JPEG, PNG).");
                factureErrorLabel.setVisible(true);
                factureErrorLabel.setManaged(true);
                isValid = false;
            } else {
                factureErrorLabel.setText("");
                factureErrorLabel.setVisible(false);
                factureErrorLabel.setManaged(false);
            }
        } else {
            factureErrorLabel.setText("");
            factureErrorLabel.setVisible(false);
            factureErrorLabel.setManaged(false);
        }
        factureField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateDescription() {
        if (descriptionField == null) return;
        String description = descriptionField.getText().trim();
        boolean isValid = true;
        descriptionErrorLabel.setText("");
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        descriptionField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private boolean validateForm() {
        validateType();
        validateDateDebut();
        validateDateFin();
        validateCout();
        validateFacture();
        validateDescription();

        return !typeErrorLabel.isVisible() &&
                !dateDebutErrorLabel.isVisible() &&
                !dateFinErrorLabel.isVisible() &&
                !coutErrorLabel.isVisible() &&
                !factureErrorLabel.isVisible() &&
                !descriptionErrorLabel.isVisible();
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier facture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers supportés", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            factureField.setText(file.getName());
            selectedFactureFile = file;
            validateFacture();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        clearFormFields();
    }

    private void clearFormFields() {
        if (typeField != null) typeField.clear();
        if (dateDebutPicker != null) dateDebutPicker.setValue(null);
        if (dateFinPicker != null) dateFinPicker.setValue(null);
        if (descriptionField != null) descriptionField.clear();
        if (coutField != null) coutField.clear();
        if (factureField != null) factureField.clear();
        selectedFactureFile = null;
        clearErrorLabels();
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
            return;
        }

        try {
            MaintenanceVehicule maintenance = new MaintenanceVehicule();
            maintenance.setIdMaintenance(0);
            maintenance.setIdVehicule(vehicule != null ? vehicule.getIdVehicule() : 0);
            maintenance.setTypeMaintenance(typeField.getText().trim());
            maintenance.setDateDebut(dateDebutPicker.getValue());
            maintenance.setDateFin(dateFinPicker.getValue());
            maintenance.setCout(Double.parseDouble(coutField.getText().trim()));
            maintenance.setDescription(descriptionField.getText().trim());

            if (selectedFactureFile != null) {
                byte[] fileContent = Files.readAllBytes(selectedFactureFile.toPath());
                maintenance.setFacture(new SerialBlob(fileContent));
            } else {
                maintenance.setFacture(null);
            }

            MaintenanceVehiculeService.save(maintenance);

            showSuccessAlert("Succès", "Maintenance ajoutée avec succès !");

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }
}