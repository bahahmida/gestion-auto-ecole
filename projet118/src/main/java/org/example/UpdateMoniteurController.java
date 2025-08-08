package org.example;

import Persistance.models.Moniteur;
import Persistance.utils.Alert; // Import ajouté
import Service.MoniteurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class UpdateMoniteurController {
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField salaireField;
    @FXML private CheckBox typeACheckBox;
    @FXML private CheckBox typeBCheckBox;
    @FXML private CheckBox typeCCheckBox;
    @FXML private Button saveButton;

    // Labels pour les messages d'erreur
    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label telephoneErrorLabel;
    @FXML private Label salaireErrorLabel;

    private Moniteur moniteur;
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        // Effacer les messages d'erreur au démarrage
        clearErrorMessages();

        // Ajouter des écouteurs pour valider les champs en temps réel
        nomField.textProperty().addListener((observable, oldValue, newValue) -> validateNom());
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> validatePrenom());
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> validateTelephone());
        salaireField.textProperty().addListener((observable, oldValue, newValue) -> validateSalaire());
    }

    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;

        cinField.setText(String.valueOf(moniteur.getCin()));
        nomField.setText(moniteur.getNom());
        prenomField.setText(moniteur.getPrenom());
        telephoneField.setText(String.valueOf(moniteur.getTel()));
        salaireField.setText(String.valueOf(moniteur.getSalaire()));

        // Initialiser les CheckBox en fonction des types du moniteur
        List<Character> types = moniteur.getCategorie();
        typeACheckBox.setSelected(types.contains('A'));
        typeBCheckBox.setSelected(types.contains('B'));
        typeCCheckBox.setSelected(types.contains('C'));

        // Désactiver uniquement les cases à cocher déjà sélectionnées
        if (typeACheckBox.isSelected()) {
            typeACheckBox.setDisable(true);
        }
        if (typeBCheckBox.isSelected()) {
            typeBCheckBox.setDisable(true);
        }
        if (typeCCheckBox.isSelected()) {
            typeCCheckBox.setDisable(true);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave() {
        // Effacer les messages d'erreur précédents
        clearErrorMessages();

        if (isInputValid()) {
            try {
                moniteur.setNom(nomField.getText().trim());
                moniteur.setPrenom(prenomField.getText().trim());
                moniteur.setTel(Integer.parseInt(telephoneField.getText()));
                moniteur.setSalaire(Float.parseFloat(salaireField.getText()));

                // Mettre à jour les types du moniteur
                List<Character> types = moniteur.getCategorie();
                if (typeACheckBox.isSelected() && !types.contains('A')) {
                    types.add('A');
                }
                if (typeBCheckBox.isSelected() && !types.contains('B')) {
                    types.add('B');
                }
                if (typeCCheckBox.isSelected() && !types.contains('C')) {
                    types.add('C');
                }

                MoniteurService.update(moniteur);
                okClicked = true;
                Alert.showSuccessAlert("Succès", "Le moniteur a été mis à jour avec succès !");
                dialogStage.close();
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour du moniteur : " + e.getMessage());
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    private boolean isInputValid() {
        boolean isValid = true;

        // Validation du nom
        if (!validateNom()) {
            isValid = false;
        }

        // Validation du prénom
        if (!validatePrenom()) {
            isValid = false;
        }

        // Validation du téléphone
        if (!validateTelephone()) {
            isValid = false;
        }

        // Validation du salaire
        if (!validateSalaire()) {
            isValid = false;
        }

        return isValid;
    }

    // Validation du nom (accepte un seul espace entre deux mots)
    private boolean validateNom() {
        String nom = nomField.getText().trim();
        boolean isEmpty = nom.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = nom.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        nomErrorLabel.setVisible(!isValid);
        nomErrorLabel.setManaged(!isValid);
        nomErrorLabel.setText(isValid ? "" : (isEmpty ? "Le nom est obligatoire" : "Le nom doit contenir uniquement des lettres et un seul espace"));
        nomField.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    // Validation du prénom (accepte un seul espace entre deux mots)
    private boolean validatePrenom() {
        String prenom = prenomField.getText().trim();
        boolean isEmpty = prenom.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = prenom.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        prenomErrorLabel.setVisible(!isValid);
        prenomErrorLabel.setManaged(!isValid);
        prenomErrorLabel.setText(isValid ? "" : (isEmpty ? "Le prénom est obligatoire" : "Le prénom doit contenir uniquement des lettres et un seul espace"));
        prenomField.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    private boolean validateTelephone() {
        String telephone = telephoneField.getText().trim();
        boolean isEmpty = telephone.isEmpty();
        boolean isValidFormat = telephone.matches("\\d{8}");

        boolean isValid = !isEmpty && isValidFormat;
        telephoneErrorLabel.setVisible(!isValid);
        telephoneErrorLabel.setManaged(!isValid);
        telephoneErrorLabel.setText(isValid ? "" : (isEmpty ? "Le téléphone est obligatoire" : "Le téléphone doit contenir 8 chiffres"));
        telephoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    private boolean validateSalaire() {
        String salaire = salaireField.getText().trim();
        boolean isEmpty = salaire.isEmpty();

        if (isEmpty) {
            salaireErrorLabel.setVisible(true);
            salaireErrorLabel.setManaged(true);
            salaireErrorLabel.setText("Le salaire est obligatoire");
            salaireField.setStyle("-fx-border-color: red;");
            return false;
        } else {
            try {
                float salaireValue = Float.parseFloat(salaire);
                if (salaireValue < 0) {
                    salaireErrorLabel.setVisible(true);
                    salaireErrorLabel.setManaged(true);
                    salaireErrorLabel.setText("Le salaire doit être positif");
                    salaireField.setStyle("-fx-border-color: red;");
                    return false;
                }
            } catch (NumberFormatException e) {
                salaireErrorLabel.setVisible(true);
                salaireErrorLabel.setManaged(true);
                salaireErrorLabel.setText("Le salaire doit être un nombre valide");
                salaireField.setStyle("-fx-border-color: red;");
                return false;
            }
            salaireErrorLabel.setVisible(false);
            salaireErrorLabel.setManaged(false);
            salaireErrorLabel.setText("");
            salaireField.setStyle("");
            return true;
        }
    }

    private void clearErrorMessages() {
        nomErrorLabel.setText("");
        nomErrorLabel.setVisible(false);
        nomErrorLabel.setManaged(false);
        prenomErrorLabel.setText("");
        prenomErrorLabel.setVisible(false);
        prenomErrorLabel.setManaged(false);
        telephoneErrorLabel.setText("");
        telephoneErrorLabel.setVisible(false);
        telephoneErrorLabel.setManaged(false);
        salaireErrorLabel.setText("");
        salaireErrorLabel.setVisible(false);
        salaireErrorLabel.setManaged(false);

        // Réinitialiser les styles des champs
        nomField.setStyle("");
        prenomField.setStyle("");
        telephoneField.setStyle("");
        salaireField.setStyle("");
    }

    @FXML
    public void close() {
        dialogStage.close();
    }
}