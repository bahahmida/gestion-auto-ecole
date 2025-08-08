package org.example;

import Persistance.models.Candidat;
import Persistance.models.CandidatDocument;
import Service.CandidatService;
import Service.PasserExamenService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class AddCandidatController {
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    private CandidatService candidatService;
    private PasserExamenService passerExamenService;

    @FXML private TextField nameField;
    @FXML private TextField firstNameField;
    @FXML private TextField cinField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;

    @FXML private RadioButton typeARadioButton;
    @FXML private RadioButton typeBRadioButton;
    @FXML private RadioButton typeCRadioButton;
    @FXML private ToggleGroup categoryGroup;

    @FXML private TextField documentPathField;

    @FXML private Label nameErrorLabel;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label cinErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label birthDateErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label documentErrorLabel;

    @FXML
    private void initialize() {
        candidatService = new CandidatService();
        passerExamenService = new PasserExamenService();

        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateName());
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateFirstName());
        cinField.textProperty().addListener((observable, oldValue, newValue) -> validateCIN());
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> validatePhone());
        birthDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> validateBirthDate());
        documentPathField.textProperty().addListener((observable, oldValue, newValue) -> validateDocument());

        categoryGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> validateCategory());
    }

    @FXML
    private void saveCandidat() {
        validateName();
        validateFirstName();
        validateCIN();
        validatePhone();
        validateBirthDate();
        validateCategory();
        validateDocument();

        if (areAllFieldsValid()) {
            try {
                Candidat candidat = new Candidat();
                int cin = Integer.parseInt(cinField.getText());
                candidat.setCin(cin);
                candidat.setNom(nameField.getText());
                candidat.setPrenom(firstNameField.getText());
                candidat.setTelephone(phoneField.getText());
                candidat.setDate_naissance(birthDatePicker.getValue());

                String categorie = "";
                if (typeARadioButton.isSelected()) categorie = "A";
                else if (typeBRadioButton.isSelected()) categorie = "B";
                else if (typeCRadioButton.isSelected()) categorie = "C";
                candidat.setCategorie(categorie);

                candidat.setEtat("Actif");
                candidat.setMontant_total(0.0f);
                candidat.setMontant_paye(0.0f);
                candidat.setSeances_totales(0);
                candidat.setSeances_effectuees(0);

                if (candidatService.getCandidat(candidat.getCin()) != null) {
                    showErrorAlert("Erreur", "Un candidat avec ce CIN existe déjà.");
                    return;
                }

                candidatService.ajouterCandidat(candidat);

                String documentPath = documentPathField.getText();
                if (!documentPath.isEmpty()) {
                    File file = new File(documentPath);
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    String documentType = "cin";
                    String description = "";

                    CandidatDocument candidatDocument = new CandidatDocument(documentType, description, fileContent, cin);
                    candidatService.saveDocument(candidatDocument);
                }

                showSuccessAlert("Succès", "Le candidat et son document ont été ajoutés avec succès !");
                close();
            } catch (IOException e) {
                showErrorAlert("Erreur", "Erreur lors de la lecture du fichier : " + e.getMessage());
            } catch (Exception e) {
                showErrorAlert("Erreur", "Une erreur est survenue lors de l'ajout du candidat : " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    private boolean areAllFieldsValid() {
        return !nameErrorLabel.isVisible() &&
                !firstNameErrorLabel.isVisible() &&
                !cinErrorLabel.isVisible() &&
                !phoneErrorLabel.isVisible() &&
                !birthDateErrorLabel.isVisible() &&
                !categoryErrorLabel.isVisible() &&
                !documentErrorLabel.isVisible();
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

    private void validateCIN() {
        String cinText = cinField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";

        if (cinText.isEmpty()) {
            isValid = false;
            errorMessage = "Le CIN est obligatoire";
        } else if (!cinText.matches("\\d{8}")) {
            isValid = false;
            errorMessage = "Le CIN doit contenir 8 chiffres";
        } else {
            try {
                int cin = Integer.parseInt(cinText);
                if (CandidatService.MoniteurExists(cin)) {
                    isValid = false;
                    errorMessage = "Ce CIN est déjà utilisé par un moniteur";
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage = "Le CIN doit être un nombre valide";
            }
        }

        cinErrorLabel.setVisible(!isValid);
        cinErrorLabel.setManaged(!isValid);
        cinErrorLabel.setText(errorMessage);
        cinField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validatePhone() {
        boolean isValid = phoneField.getText().matches("\\d{8}");
        phoneErrorLabel.setVisible(!isValid);
        phoneErrorLabel.setManaged(!isValid);
        phoneErrorLabel.setText(isValid ? "" : "Le téléphone doit contenir 8 chiffres");
        phoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateBirthDate() {
        LocalDate birthDate = birthDatePicker.getValue();
        boolean isValid = birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(18));
        birthDateErrorLabel.setVisible(!isValid);
        birthDateErrorLabel.setManaged(!isValid);
        birthDateErrorLabel.setText(isValid ? "" : "Le candidat doit avoir au moins 18 ans");
        birthDatePicker.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateCategory() {
        boolean isValid = categoryGroup.getSelectedToggle() != null;
        categoryErrorLabel.setVisible(!isValid);
        categoryErrorLabel.setManaged(!isValid);
        categoryErrorLabel.setText(isValid ? "" : "Une catégorie doit être sélectionnée");
    }

    private void validateDocument() {
        boolean isValid = documentPathField.getText() != null && !documentPathField.getText().isEmpty();
        documentErrorLabel.setVisible(!isValid);
        documentErrorLabel.setManaged(!isValid);
        documentErrorLabel.setText(isValid ? "" : "Un document CIN est requis");
        documentPathField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    @FXML
    private void browseDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le document CIN");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(documentPathField.getScene().getWindow());
        if (selectedFile != null) {
            documentPathField.setText(selectedFile.getAbsolutePath());
            validateDocument();
        }
    }

    @FXML
    private void cancel() {
        nameField.clear();
        firstNameField.clear();
        cinField.clear();
        phoneField.clear();
        birthDatePicker.setValue(null);
        documentPathField.clear();

        categoryGroup.selectToggle(null);

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
        birthDateErrorLabel.setText("");
        birthDateErrorLabel.setVisible(false);
        birthDateErrorLabel.setManaged(false);
        categoryErrorLabel.setText("");
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);
        documentErrorLabel.setText("");
        documentErrorLabel.setVisible(false);
        documentErrorLabel.setManaged(false);

        nameField.setStyle("");
        firstNameField.setStyle("");
        cinField.setStyle("");
        phoneField.setStyle("");
        birthDatePicker.setStyle("");
        documentPathField.setStyle("");
    }

    @FXML
    public void close() {
        menuController.loadView("candidat.fxml");
    }




}