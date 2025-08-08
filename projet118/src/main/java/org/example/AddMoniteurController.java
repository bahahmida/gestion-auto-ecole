package org.example;

import Persistance.models.Moniteur;
import Service.MoniteurService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class AddMoniteurController {
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML private TextField nameField;
    @FXML private TextField firstNameField;
    @FXML private TextField cinField;
    @FXML private TextField phoneField;
    @FXML private TextField salaryField;

    @FXML private CheckBox typeACheckBox;
    @FXML private CheckBox typeBCheckBox;
    @FXML private CheckBox typeCCheckBox;

    @FXML private Label nameErrorLabel;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label cinErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label salaryErrorLabel;
    @FXML private Label specialtyErrorLabel;

    @FXML private Button saveButton;

    @FXML
    private void initialize() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateName());
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateFirstName());
        cinField.textProperty().addListener((observable, oldValue, newValue) -> validateCIN());
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> validatePhone());
        salaryField.textProperty().addListener((observable, oldValue, newValue) -> validateSalary());

        typeACheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validateSpecialty());
        typeBCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validateSpecialty());
        typeCCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> validateSpecialty());
    }

    @FXML
    private void saveMonitor() {
        validateName();
        validateFirstName();
        validateCIN();
        validatePhone();
        validateSalary();
        validateSpecialty();

        if (!nameErrorLabel.isVisible() &&
                !firstNameErrorLabel.isVisible() &&
                !cinErrorLabel.isVisible() &&
                !phoneErrorLabel.isVisible() &&
                !salaryErrorLabel.isVisible() &&
                !specialtyErrorLabel.isVisible()) {

            String name = nameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String cinText = cinField.getText().trim();
            String phoneText = phoneField.getText().trim();
            String salaryText = salaryField.getText().trim();

            try {
                int cin = Integer.parseInt(cinText);
                int phone = Integer.parseInt(phoneText);
                float salary = Float.parseFloat(salaryText);

                if (MoniteurService.MoniteurExists(cin)) {
                    showErrorAlert("Erreur", "Un moniteur avec ce CIN existe déjà.");
                    return;
                }

                Moniteur moniteur = new Moniteur(cin, name, firstName, phone, salary);

                if (typeACheckBox.isSelected()) {
                    moniteur.getCategorie().add('A');
                }
                if (typeBCheckBox.isSelected()) {
                    moniteur.getCategorie().add('B');
                }
                if (typeCCheckBox.isSelected()) {
                    moniteur.getCategorie().add('C');
                }

                MoniteurService.save(moniteur);
                showSuccessAlert("Succès", "Le moniteur a été ajouté avec succès !");
                close();
            } catch (NumberFormatException e) {
                showErrorAlert("Erreur", "Veuillez vérifier les champs numériques (CIN, téléphone, salaire).");
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    // Validation du nom (accepte un seul espace entre deux mots)
    private void validateName() {
        String name = nameField.getText().trim();
        boolean isEmpty = name.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = name.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        nameErrorLabel.setVisible(!isValid);
        nameErrorLabel.setManaged(!isValid);
        nameErrorLabel.setText(isValid ? "" : (isEmpty ? "Le nom est obligatoire" : "Le nom doit contenir uniquement des lettres et un seul espace"));
        nameField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du prénom (accepte un seul espace entre deux mots)
    private void validateFirstName() {
        String firstName = firstNameField.getText().trim();
        boolean isEmpty = firstName.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = firstName.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        firstNameErrorLabel.setVisible(!isValid);
        firstNameErrorLabel.setManaged(!isValid);
        firstNameErrorLabel.setText(isValid ? "" : (isEmpty ? "Le prénom est obligatoire" : "Le prénom doit contenir uniquement des lettres et un seul espace"));
        firstNameField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du CIN
    private void validateCIN() {
        String cinText = cinField.getText().trim();
        boolean isValid = cinText.matches("\\d{8}");

        cinErrorLabel.setVisible(!isValid);
        cinErrorLabel.setManaged(!isValid);
        cinErrorLabel.setText(isValid ? "" : "Le CIN doit contenir 8 chiffres");
        cinField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du téléphone
    private void validatePhone() {
        String phoneText = phoneField.getText().trim();
        boolean isValid = phoneText.matches("\\d{8}");

        phoneErrorLabel.setVisible(!isValid);
        phoneErrorLabel.setManaged(!isValid);
        phoneErrorLabel.setText(isValid ? "" : "Le téléphone doit contenir 8 chiffres");
        phoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du salaire
    private void validateSalary() {
        String salaryText = salaryField.getText().trim();
        boolean isValid = true;

        if (salaryText.isEmpty()) {
            salaryErrorLabel.setText("Le salaire est obligatoire");
            isValid = false;
        } else {
            try {
                float salary = Float.parseFloat(salaryText);
                if (salary <= 0) {
                    salaryErrorLabel.setText("Le salaire doit être positif");
                    isValid = false;
                } else {
                    salaryErrorLabel.setText("");
                }
            } catch (NumberFormatException e) {
                salaryErrorLabel.setText("Le salaire doit être un nombre valide");
                isValid = false;
            }
        }

        salaryErrorLabel.setVisible(!isValid);
        salaryErrorLabel.setManaged(!isValid);
        salaryField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation de la spécialité
    private void validateSpecialty() {
        boolean isValid = typeACheckBox.isSelected() || typeBCheckBox.isSelected() || typeCCheckBox.isSelected();
        specialtyErrorLabel.setVisible(!isValid);
        specialtyErrorLabel.setManaged(!isValid);
        specialtyErrorLabel.setText(isValid ? "" : "Au moins une catégorie doit être sélectionnée");
    }

    // Annuler et réinitialiser les champs
    @FXML
    private void cancel() {
        nameField.clear();
        firstNameField.clear();
        cinField.clear();
        phoneField.clear();
        salaryField.clear();

        typeACheckBox.setSelected(false);
        typeBCheckBox.setSelected(false);
        typeCCheckBox.setSelected(false);

        nameErrorLabel.setText("");
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        firstNameErrorLabel.setText("");
        firstNameErrorLabel.setVisible(false);
        firstNameErrorLabel.setManaged(false);
        cinErrorLabel.setText("");
        cinErrorLabel.setVisible(false);
        cinErrorLabel.setManaged(false);
        phoneErrorLabel.setText("");
        phoneErrorLabel.setVisible(false);
        phoneErrorLabel.setManaged(false);
        salaryErrorLabel.setText("");
        salaryErrorLabel.setVisible(false);
        salaryErrorLabel.setManaged(false);
        specialtyErrorLabel.setText("");
        specialtyErrorLabel.setVisible(false);
        specialtyErrorLabel.setManaged(false);

        nameField.setStyle("");
        firstNameField.setStyle("");
        cinField.setStyle("");
        phoneField.setStyle("");
        salaryField.setStyle("");
    }

    // Fermer la fenêtre
    @FXML
    public void close() {
        menuController.loadView("moniteur.fxml");
    }




}