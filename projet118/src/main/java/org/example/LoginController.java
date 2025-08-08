package org.example;

import Persistance.dao.AutoEcoleDAO;
import Persistance.models.AutoEcole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        // Ajouter des écouteurs pour valider les champs en temps réel
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validatePassword());

        // Initialiser l'UI selon l'état de la base de données
        initUI();
    }

    // Initialize UI based on database state
    public void initUI() {
        int rowCount = AutoEcoleDAO.getRowCount();
        if (rowCount == 0) {
            emailField.setText("admin");
            passwordField.setText("admin");
            emailErrorLabel.setText("L'auto-école n'a pas encore d'email");
            emailErrorLabel.setStyle("-fx-text-fill: green;");
            emailErrorLabel.setVisible(true);
            passwordErrorLabel.setText("L'auto-école n'a pas encore de mot de passe");
            passwordErrorLabel.setStyle("-fx-text-fill: green;");
            passwordErrorLabel.setVisible(true);
        } else {
            emailField.setText("");
            passwordField.setText("");
            emailErrorLabel.setVisible(false);
            passwordErrorLabel.setVisible(false);
        }
    }

    // Validation de l'email
    private void validateEmail() {
        String email = emailField.getText().trim();
        int rowCount = AutoEcoleDAO.getRowCount();
        boolean isValid;

        if (rowCount == 0) {
            isValid = email.equals("admin");
            emailErrorLabel.setVisible(!isValid);
            emailErrorLabel.setText(isValid ? "" : "Veuillez entrer 'admin' pour une table vide");
            emailErrorLabel.setStyle(isValid ? "" : "-fx-text-fill: red;");
        } else {
            boolean isEmpty = email.isEmpty();
            boolean isEmailFormat = email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
            isValid = !isEmpty && isEmailFormat;
            emailErrorLabel.setVisible(isEmpty || !isEmailFormat);
            emailErrorLabel.setText(isEmpty ? "L'email est requis" : (!isEmailFormat ? "Format d'email invalide" : ""));
            emailErrorLabel.setStyle(isEmpty || !isEmailFormat ? "-fx-text-fill: red;" : "");
        }
        emailField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du mot de passe
    private void validatePassword() {
        String password = passwordField.getText().trim();
        int rowCount = AutoEcoleDAO.getRowCount();
        boolean isValid;

        if (rowCount == 0) {
            isValid = password.equals("admin");
            passwordErrorLabel.setVisible(!isValid);
            passwordErrorLabel.setText(isValid ? "" : "Veuillez entrer 'admin' pour une table vide");
            passwordErrorLabel.setStyle(isValid ? "" : "-fx-text-fill: red;");
        } else {
            isValid = !password.isEmpty();
            passwordErrorLabel.setVisible(!isValid);
            passwordErrorLabel.setText(isValid ? "" : "Le mot de passe est requis");
            passwordErrorLabel.setStyle(isValid ? "" : "-fx-text-fill: red;");
        }
        passwordField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    @FXML
    private void handleLogin() {
        validateEmail();
        validatePassword();

        if (emailErrorLabel.getText().isEmpty() && passwordErrorLabel.getText().isEmpty()) {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            int rowCount = AutoEcoleDAO.getRowCount();

            try {
                if (rowCount == 0) {
                    if (email.equals("admin") && password.equals("admin")) {

                        loadMenu();
                    } else {
                        showErrorAlert("Erreur de connexion", "Email ou mot de passe incorrect.");
                    }
                } else {
                    AutoEcole autoEcole = AutoEcoleDAO.find();
                    if (autoEcole != null && email.equals(autoEcole.getEmail()) && password.equals(autoEcole.getPassword())) {

                        loadMenu();
                    } else {
                        showErrorAlert("Erreur de connexion", "Email ou mot de passe incorrect.");
                    }
                }
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Échec de la connexion : " + e.getMessage());
            } catch (IOException e) {
                showErrorAlert("Erreur", "Erreur lors du chargement du menu : " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    private void loadMenu() throws IOException {
        if (loginButton == null) {
            System.err.println("Erreur : loginButton est null dans loadMenu");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/menu.fxml"));
        Parent menuPage = loader.load();
        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene menuScene = new Scene(menuPage, 1346, 700);
        stage.setScene(menuScene);
        stage.setTitle("Menu Auto-École");
        stage.setResizable(true); // Allow resizing for the menu page
        stage.centerOnScreen(); // Center the menu page
        stage.show();
    }

    @FXML
    private void handleForgotPassword() {
        int rowCount = AutoEcoleDAO.getRowCount();
        if (rowCount == 0) {
            showErrorAlert("Erreur", "Aucun utilisateur enregistré. Utilisez admin/admin pour vous connecter.");
            return;
        }

        try {
            AutoEcole autoEcole = AutoEcoleDAO.find();
            if (autoEcole == null) {
                showErrorAlert("Erreur", "Aucune auto-école enregistrée.");
                return;
            }
            String registeredEmail = autoEcole.getEmail();
            AutoEcoleDAO.sendPasswordResetEmail(registeredEmail, autoEcole.getPassword());
            showSuccessAlert("Succès", "Un email a été envoyé à " + registeredEmail);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de la vérification : " + e.getMessage());
        } catch (MessagingException e) {
            showErrorAlert("Erreur", "Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    // Alertes (inchangées)
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
}