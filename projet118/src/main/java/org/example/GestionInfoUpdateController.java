package org.example;

import Persistance.models.AutoEcole;
import Service.AutoEcoleService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class GestionInfoUpdateController {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private Label nameErrorLabel;
    @FXML private Label addressErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;

    private Stage dialogStage;

    // Définir le Stage
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Initialiser les champs avec les anciennes valeurs
    public void initData() {
        AutoEcole autoEcole = AutoEcoleService.find(); // Récupérer les données existantes

        // Vérifier si les données existent et remplir les champs
        if (autoEcole != null) {
            nameField.setText(autoEcole.getNom() != null ? autoEcole.getNom() : "");
            addressField.setText(autoEcole.getAdresse() != null ? autoEcole.getAdresse() : "");
            emailField.setText(autoEcole.getEmail() != null ? autoEcole.getEmail() : "");
            phoneField.setText(autoEcole.getNumTel() > 0 ? String.valueOf(autoEcole.getNumTel()) : "");
        } else {
            // Si aucune donnée n'est trouvée, initialiser les champs à vide
            nameField.setText("");
            addressField.setText("");
            emailField.setText("");
            phoneField.setText("");
        }

        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        // Ajouter des écouteurs pour valider les champs en temps réel
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> validatePhone());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateName());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> validateAddress());
    }

    // Gérer le clic sur "Annuler"
    @FXML
    public void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Gérer le clic sur "Enregistrer"
    @FXML
    public void handleSave() {
        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        // Valider tous les champs
        validateName();
        validateAddress();
        validateEmail();
        validatePhone();

        // Vérifier si toutes les validations passent
        if (nameErrorLabel.getText().isEmpty() &&
                addressErrorLabel.getText().isEmpty() &&
                emailErrorLabel.getText().isEmpty() &&
                phoneErrorLabel.getText().isEmpty()) {
            try {
                // Créer un nouvel objet AutoEcole avec les données mises à jour
                AutoEcole autoEcole = new AutoEcole(
                        Integer.parseInt(phoneField.getText()),
                        nameField.getText().trim(),
                        addressField.getText().trim(),
                        emailField.getText().trim(),
                        AutoEcoleService.find().getPassword() // Conserver le mot de passe existant
                );

                // Sauvegarder les données via le service
                AutoEcoleService.save(autoEcole);

                // Afficher une alerte de succès
                showSuccessAlert("Succès", "Les informations ont été enregistrées avec succès !");

                // Fermer la fenêtre
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Erreur de format", "Le numéro de téléphone doit être un nombre valide.");
            }
        }
    }

    // Validation du numéro de téléphone
    private void validatePhone() {
        String phoneText = phoneField.getText().trim();
        boolean isValid = phoneText.matches("^[0-9]{8}$");
        phoneErrorLabel.setText(isValid ? "" : "Le numéro doit contenir 8 chiffres");
        phoneErrorLabel.setVisible(!isValid);
        phoneErrorLabel.setManaged(!isValid);
        phoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation de l'email
    private void validateEmail() {
        String emailText = emailField.getText().trim();
        boolean isValid = emailText.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        emailErrorLabel.setText(isValid ? "" : "Format d'email invalide");
        emailErrorLabel.setVisible(!isValid);
        emailErrorLabel.setManaged(!isValid);
        emailField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du nom
    private void validateName() {
        String nameText = nameField.getText().trim();
        boolean isValid = !nameText.isEmpty();
        nameErrorLabel.setText(isValid ? "" : "Le nom de l'auto-école est obligatoire");
        nameErrorLabel.setVisible(!isValid);
        nameErrorLabel.setManaged(!isValid);
        nameField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation de l'adresse
    private void validateAddress() {
        String addressText = addressField.getText().trim();
        boolean isValid = !addressText.isEmpty();
        addressErrorLabel.setText(isValid ? "" : "L'adresse est obligatoire");
        addressErrorLabel.setVisible(!isValid);
        addressErrorLabel.setManaged(!isValid);
        addressField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Réinitialiser les messages d'erreur
    private void clearErrorMessages() {
        nameErrorLabel.setText("");
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        addressErrorLabel.setText("");
        addressErrorLabel.setVisible(false);
        addressErrorLabel.setManaged(false);
        emailErrorLabel.setText("");
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        phoneErrorLabel.setText("");
        phoneErrorLabel.setVisible(false);
        phoneErrorLabel.setManaged(false);

        // Réinitialiser les styles des champs
        nameField.setStyle("");
        addressField.setStyle("");
        emailField.setStyle("");
        phoneField.setStyle("");
    }

    // Afficher une alerte de succès
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("success-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2E7D32;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }

    // Afficher une alerte d'erreur
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}