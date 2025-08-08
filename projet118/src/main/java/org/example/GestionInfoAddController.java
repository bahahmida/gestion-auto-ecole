package org.example;


import Persistance.models.AutoEcole;
import Service.AutoEcoleService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class GestionInfoAddController {

    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private TextField addressField;
    @FXML private Label addressErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private TextField phoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordErrorLabel;

    private Stage dialogStage;

    public GestionInfoAddController() {
        // Ne pas appeler initListeners ici pour éviter NullPointerException
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        // Initialiser les écouteurs après l'injection des champs FXML
        initListeners();
    }

    // Method to initialize listeners for real-time validation
    private void initListeners() {
        // Vérifier que les champs ne sont pas null avant d'ajouter les écouteurs
        if (phoneField != null) {
            phoneField.textProperty().addListener((observable, oldValue, newValue) -> validatePhone());
        }
        if (emailField != null) {
            emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
        }
        if (nameField != null) {
            nameField.textProperty().addListener((observable, oldValue, newValue) -> validateName());
        }
        if (addressField != null) {
            addressField.textProperty().addListener((observable, oldValue, newValue) -> validateAddress());
        }
        if (passwordField != null) {
            passwordField.textProperty().addListener((observable, oldValue, newValue) -> validatePassword());
        }
    }

    @FXML
    public void handleCancel() {
        // Clear all fields if they are not null
        if (nameField != null) nameField.clear();
        if (addressField != null) addressField.clear();
        if (emailField != null) emailField.clear();
        if (phoneField != null) phoneField.clear();
        if (passwordField != null) passwordField.clear();

        // Reset error labels if they are not null
        if (nameErrorLabel != null) {
            nameErrorLabel.setText("");
            nameErrorLabel.setVisible(false);
        }
        if (addressErrorLabel != null) {
            addressErrorLabel.setText("");
            addressErrorLabel.setVisible(false);
        }
        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
        }
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText("");
            phoneErrorLabel.setVisible(false);
        }
        if (passwordErrorLabel != null) {
            passwordErrorLabel.setText("");
            passwordErrorLabel.setVisible(false);
        }

        // Reset field styles if they are not null
        if (nameField != null) nameField.setStyle("");
        if (addressField != null) addressField.setStyle("");
        if (emailField != null) emailField.setStyle("");
        if (phoneField != null) phoneField.setStyle("");
        if (passwordField != null) passwordField.setStyle("");

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    public void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    public void handleSave() {
        // Re-validate all fields
        validateName();
        validateAddress();
        validateEmail();
        validatePhone();
        validatePassword();

        // Check if all fields are valid
        if ((nameErrorLabel == null || nameErrorLabel.getText().isEmpty()) &&
                (addressErrorLabel == null || addressErrorLabel.getText().isEmpty()) &&
                (emailErrorLabel == null || emailErrorLabel.getText().isEmpty()) &&
                (phoneErrorLabel == null || phoneErrorLabel.getText().isEmpty()) &&
                (passwordErrorLabel == null || passwordErrorLabel.getText().isEmpty())) {

            try {
                // Check if an auto-école record already exists
                int rowCount = AutoEcoleService.getRowCount();
                if (rowCount != 0) {
                    showWarningAlert("Avertissement", "Une auto-école est déjà enregistrée. Veuillez modifier les données existantes au lieu d'ajouter une nouvelle.");
                    return;
                }

                // Create AutoEcole object
                String name = nameField != null ? nameField.getText().trim() : "";
                String address = addressField != null ? addressField.getText().trim() : "";
                String email = emailField != null ? emailField.getText().trim() : "";
                String phone = phoneField != null ? phoneField.getText().trim() : "";
                String password = passwordField != null ? passwordField.getText().trim() : "";
                int phoneNumber = Integer.parseInt(phone);

                AutoEcole autoEcole = new AutoEcole(phoneNumber, name, address, email, password);


                AutoEcoleService.save(autoEcole);

                // Show success alert
                showSuccessAlert("Succès", "Toutes les informations ont été validées et enregistrées avec succès !");

                // Close the dialog
                if (dialogStage != null) {
                    dialogStage.close();
                }

            } catch (NumberFormatException e) {
                if (phoneErrorLabel != null) {
                    phoneErrorLabel.setText("Le numéro de téléphone doit être un nombre valide");
                    phoneErrorLabel.setVisible(true);
                }
                if (phoneField != null) phoneField.setStyle("-fx-border-color: red;");
                System.err.println("Erreur de format de numéro : " + e.getMessage());
            } catch (Exception e) {
                showErrorAlert("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
                System.err.println("Erreur générale : " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    // Validation methods
    private void validateName() {
        if (nameField == null || nameErrorLabel == null) return;
        String name = nameField.getText().trim();
        boolean isEmpty = name.isEmpty();
        boolean isValid = name.matches("[a-zA-Z\\s]+"); // Letters and spaces only
        nameErrorLabel.setVisible(isEmpty || !isValid);
        nameErrorLabel.setText(isEmpty ? "Le nom est obligatoire" : (!isValid ? "Le nom doit contenir uniquement des lettres et des espaces" : ""));
        nameField.setStyle(isEmpty || !isValid ? "-fx-border-color: red;" : "");
    }

    private void validateAddress() {
        if (addressField == null || addressErrorLabel == null) return;
        String address = addressField.getText().trim();
        boolean isEmpty = address.isEmpty();
        boolean isValid = address.matches("[a-zA-Z0-9\\s,]+"); // Letters, numbers, spaces, and commas
        addressErrorLabel.setVisible(isEmpty || !isValid);
        addressErrorLabel.setText(isEmpty ? "L'adresse est obligatoire" : (!isValid ? "L'adresse doit contenir des lettres, chiffres, espaces ou virgules" : ""));
        addressField.setStyle(isEmpty || !isValid ? "-fx-border-color: red;" : "");
    }

    private void validateEmail() {
        if (emailField == null || emailErrorLabel == null) return;
        String email = emailField.getText().trim();
        boolean isEmpty = email.isEmpty();
        boolean isValidFormat = email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"); // Stricter email regex
        emailErrorLabel.setVisible(isEmpty || !isValidFormat);
        emailErrorLabel.setText(isEmpty ? "L'email est obligatoire" : (!isValidFormat ? "Format d'email invalide (ex. exemple@domaine.com)" : ""));
        emailField.setStyle(isEmpty || !isValidFormat ? "-fx-border-color: red;" : "");
    }

    private void validatePhone() {
        if (phoneField == null || phoneErrorLabel == null) return;
        String phone = phoneField.getText().trim();
        boolean isValid = phone.matches("^[0-9]{8}$");
        phoneErrorLabel.setVisible(!isValid);
        phoneErrorLabel.setText(isValid ? "" : "Le numéro doit contenir 8 chiffres");
        phoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validatePassword() {
        if (passwordField == null || passwordErrorLabel == null) return;
        String password = passwordField.getText().trim();
        boolean isEmpty = password.isEmpty();
        boolean isValidLength = password.length() >= 6; // Minimum 6 caractères
        boolean isValid = !isEmpty && isValidLength;
        passwordErrorLabel.setVisible(!isValid);
        passwordErrorLabel.setText(isEmpty ? "Le mot de passe est obligatoire" : (!isValidLength ? "Le mot de passe doit contenir au moins 6 caractères" : ""));
        passwordField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Alert methods
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
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #2E7D32;"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("error-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-weight: bold;");
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle("-fx-background-color: #CC0000; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #AA0000;"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #CC0000; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("warning-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #FF9800; -fx-text-fill: black; -fx-font-weight: bold;");
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #E65100;"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }
}