package org.example;

import Persistance.models.Paiement;
import Service.PaiementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class UpdatePaiementController {

    @FXML private TextField montantField;
    @FXML private DatePicker datePaiementPicker;
    @FXML private TextArea descriptionField;
    @FXML private Label montantErrorLabel;
    @FXML private Label datePaiementErrorLabel;

    private Stage dialogStage;
    private Paiement paiement;
    private boolean okClicked = false;
    private PaiementService paiementService;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
        // Récupérer les anciennes valeurs
        montantField.setText(String.valueOf(paiement.getMontant()));
        datePaiementPicker.setValue(paiement.getDatePaiement());
        descriptionField.setText(paiement.getDescription() != null ? paiement.getDescription() : "");
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        paiementService = new PaiementService();

        // S'assurer que les labels d'erreur sont invisibles au démarrage
        montantErrorLabel.setVisible(false);
        montantErrorLabel.setManaged(false);
        datePaiementErrorLabel.setVisible(false);
        datePaiementErrorLabel.setManaged(false);

        // Ajouter un écouteur pour valider le montant en temps réel
        montantField.textProperty().addListener((observable, oldValue, newValue) -> validateMontant());

        // Désactiver le DatePicker
        datePaiementPicker.setDisable(true);
        datePaiementPicker.setStyle("-fx-opacity: 1;"); // Maintenir l'apparence visible
    }

    @FXML
    private void handleSave() {
        // Valider le montant avant de sauvegarder
        validateMontant();

        if (isInputValid()) {
            // Mettre à jour le paiement (sans modifier la date)
            paiement.setMontant(Double.parseDouble(montantField.getText()));
            paiement.setDescription(descriptionField.getText());

            try {
                PaiementService.update(paiement);
                okClicked = true;
                dialogStage.close();
                showSuccessAlert("Succès", "Le paiement a été modifié avec succès.");
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de la mise à jour du paiement : " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void close() {
        dialogStage.close();
    }

    // Validation du montant en temps réel
    private void validateMontant() {
        String montantText = montantField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";

        if (montantText.isEmpty()) {
            isValid = false;
            errorMessage = "Le montant est requis";
        } else {
            try {
                double montant = Double.parseDouble(montantText);
                if (montant <= 0) {
                    isValid = false;
                    errorMessage = "Le montant doit être supérieur à 0";
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage = "Le montant doit être un nombre valide";
            }
        }

        // Afficher le label d'erreur seulement si une erreur est détectée
        montantErrorLabel.setVisible(!isValid);
        montantErrorLabel.setManaged(!isValid);
        montantErrorLabel.setText(errorMessage);
        montantField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Vérifier si tous les champs sont valides
    private boolean isInputValid() {
        // La description est facultative, donc pas de validation
        // Pas de validation sur la date car elle est désactivée
        datePaiementErrorLabel.setVisible(false);
        datePaiementErrorLabel.setManaged(false);
        datePaiementErrorLabel.setText("");
        return montantErrorLabel.getText().isEmpty();
    }
}