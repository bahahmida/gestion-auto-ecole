package org.example;

import Persistance.models.AutoEcole;
import Persistance.utils.Alert; // Import modifié
import Service.AutoEcoleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

public class UpdateMotDePasseController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label oldPasswordErrorLabel;
    @FXML private Label newPasswordErrorLabel;
    @FXML private Button closeButton;
    @FXML private Button updateButton;
    @FXML private Label forgotPasswordLabel; // Changé de Button à Label pour correspondre au FXML

    private Stage dialogStage; // Référence au Stage pour fermer la fenêtre
    private String storedPassword; // Mot de passe actuel récupéré depuis la base de données

    // Méthode pour définir le Stage
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Méthode pour initialiser les données
    public void initData() {
        // Récupérer le mot de passe actuel depuis AutoEcoleService
        AutoEcole autoEcole = AutoEcoleService.find();
        storedPassword = autoEcole != null ? autoEcole.getPassword() : "";

        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        // Ajouter des écouteurs pour valider les champs en temps réel
        oldPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateOldPassword());
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateNewPassword());
    }

    // Validation de l'ancien mot de passe
    private void validateOldPassword() {
        String input = oldPasswordField.getText();
        boolean isValid = input.equals(storedPassword);
        oldPasswordErrorLabel.setText(isValid ? "" : "L'ancien mot de passe est incorrect");
        oldPasswordErrorLabel.setVisible(!isValid);
        oldPasswordErrorLabel.setManaged(!isValid);
        oldPasswordField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du nouveau mot de passe
    private void validateNewPassword() {
        String input = newPasswordField.getText().trim();
        boolean isValid = input.length() >= 6; // Exemple : minimum 6 caractères
        newPasswordErrorLabel.setText(isValid ? "" : "Le mot de passe doit contenir au moins 6 caractères");
        newPasswordErrorLabel.setVisible(!isValid);
        newPasswordErrorLabel.setManaged(!isValid);
        newPasswordField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Réinitialiser les messages d'erreur
    private void clearErrorMessages() {
        oldPasswordErrorLabel.setText("");
        oldPasswordErrorLabel.setVisible(false);
        oldPasswordErrorLabel.setManaged(false);
        newPasswordErrorLabel.setText("");
        newPasswordErrorLabel.setVisible(false);
        newPasswordErrorLabel.setManaged(false);

        // Réinitialiser les styles des champs
        oldPasswordField.setStyle("");
        newPasswordField.setStyle("");
    }

    // Gérer le clic sur le bouton "Modifier"
    @FXML
    public void handleUpdate() {
        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        // Valider les champs
        validateOldPassword();
        validateNewPassword();

        // Vérifier si les deux champs sont valides
        if (oldPasswordErrorLabel.getText().isEmpty() && newPasswordErrorLabel.getText().isEmpty()) {
            // Mettre à jour le mot de passe
            AutoEcole autoEcole = AutoEcoleService.find();
            if (autoEcole != null) {
                autoEcole.setPassword(newPasswordField.getText().trim());
                AutoEcoleService.save(autoEcole); // Sauvegarder les modifications

                // Afficher une alerte de succès
                Alert.showSuccessAlert("Succès", "Le mot de passe a été mis à jour avec succès !");

                // Fermer la fenêtre
                if (dialogStage != null) {
                    dialogStage.close();
                }
            }
        }
    }

    // Gérer le clic sur le lien "Mot de passe oublié"
    @FXML
    public void handleForgotPassword() {
        int rowCount = AutoEcoleService.getRowCount();
        if (rowCount == 0) {
            Alert.showErrorAlert("Erreur", "Aucune auto-école n'est enregistrée dans le système.");
            return;
        }

        AutoEcole autoEcole = AutoEcoleService.find();
        if (autoEcole == null) {
            Alert.showErrorAlert("Erreur", "Aucune auto-école n'est trouvée dans la base de données.");
            return;
        }

        String registeredEmail = autoEcole.getEmail();
        AutoEcoleService.sendPasswordResetEmail(registeredEmail, autoEcole.getPassword());

        Alert.showSuccessAlert("Succès", "Un email a été envoyé à " + registeredEmail);
    }

    // Gérer le clic sur le bouton "X" (fermeture)
    @FXML
    public void handleClose() {
        if (dialogStage != null) {
            dialogStage.close(); // Ferme la fenêtre sans sauvegarder
        }
    }

    // Méthode statique pour ouvrir la fenêtre de modification du mot de passe
    public static void showDialog(Stage owner) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(UpdateMotDePasseController.class.getResource("updateMotDePasse.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur
            UpdateMotDePasseController controller = loader.getController();

            // Créer un nouveau Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier le mot de passe");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            // Configurer le contrôleur
            controller.setDialogStage(dialogStage);
            controller.initData();

            // Afficher la fenêtre
            dialogStage.showAndWait();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification du mot de passe : " + e.getMessage());
        }
    }
}