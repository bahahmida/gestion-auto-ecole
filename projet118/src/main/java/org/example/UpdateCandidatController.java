package org.example;

import Persistance.models.Candidat;
import Persistance.utils.Alert; // Import ajouté
import Service.CandidatService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class UpdateCandidatController {
    private MenuController menuController;
    private Candidat candidat;
    private Stage dialogStage;
    private boolean okClicked = false;
    private CandidatService candidatService = new CandidatService();

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
        populateFields();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML private TextField nameField;
    @FXML private TextField firstNameField;
    @FXML private TextField cinField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private RadioButton typeARadioButton;
    @FXML private RadioButton typeBRadioButton;
    @FXML private RadioButton typeCRadioButton;
    @FXML private ToggleGroup categoryToggleGroup;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField totalAmountField;
    @FXML private TextField paidAmountField;
    @FXML private TextField totalSessionsField;
    @FXML private TextField completedSessionsField;

    @FXML private Label nameErrorLabel;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label birthDateErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label totalAmountErrorLabel;

    @FXML
    private void initialize() {
        statusComboBox.getItems().addAll("Actif", "Inactif");

        if (categoryToggleGroup == null) {
            categoryToggleGroup = new ToggleGroup();
        }
        typeARadioButton.setToggleGroup(categoryToggleGroup);
        typeBRadioButton.setToggleGroup(categoryToggleGroup);
        typeCRadioButton.setToggleGroup(categoryToggleGroup);

        // Ajouter des écouteurs pour valider les champs en temps réel (sauf pour Montant payé)
        nameField.textProperty().addListener((obs, old, newVal) -> validateName());
        firstNameField.textProperty().addListener((obs, old, newVal) -> validateFirstName());
        phoneField.textProperty().addListener((obs, old, newVal) -> validatePhone());
        birthDatePicker.valueProperty().addListener((obs, old, newVal) -> validateBirthDate());
        totalAmountField.textProperty().addListener((obs, old, newVal) -> validateTotalAmount());
        categoryToggleGroup.selectedToggleProperty().addListener((obs, old, newVal) -> validateCategory());
        statusComboBox.valueProperty().addListener((obs, old, newVal) -> validateStatus());

        populateFields();
    }

    private void populateFields() {
        if (candidat != null) {
            nameField.setText(candidat.getNom());
            firstNameField.setText(candidat.getPrenom());
            cinField.setText(String.valueOf(candidat.getCin()));
            cinField.setDisable(true);
            phoneField.setText(candidat.getTelephone());
            birthDatePicker.setValue(candidat.getDate_naissance());
            statusComboBox.setValue(candidat.getEtat());

            String category = candidat.getCategorie();
            typeARadioButton.setSelected("A".equals(category));
            typeBRadioButton.setSelected("B".equals(category));
            typeCRadioButton.setSelected("C".equals(category));

            totalAmountField.setText(String.valueOf(candidat.getMontant_total()));
            paidAmountField.setText(String.valueOf(candidat.getMontant_paye()));
            totalSessionsField.setText(String.valueOf(candidat.getSeances_totales()));
            completedSessionsField.setText(String.valueOf(candidat.getSeances_effectuees()));

            validateAllFields();
        }
    }

    public void updateSeancepasse(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = candidatToUpdate.getSeances_effectuees();
            candidatToUpdate.setSeances_effectuees(currentTotal + 1);
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updateSeancetot(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = candidatToUpdate.getSeances_totales();
            candidatToUpdate.setSeances_totales(currentTotal + 1);
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updatecouttot(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = (int) candidatToUpdate.getMontant_total();
            if (candidatService.getCandidat(cin).getCategorie().charAt(0) == 'A') {
                candidatToUpdate.setMontant_total(currentTotal + 20);
            } else if (candidatService.getCandidat(cin).getCategorie().charAt(0) == 'B') {
                candidatToUpdate.setMontant_total(currentTotal + 25);
            } else {
                candidatToUpdate.setMontant_total(currentTotal + 28);
            }
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updatecouttot1(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = (int) candidatToUpdate.getMontant_total();
            if (candidatService.getCandidat(cin).getCategorie().charAt(0) == 'A') {
                candidatToUpdate.setMontant_total(currentTotal - 20);
            } else if (candidatService.getCandidat(cin).getCategorie().charAt(0) == 'B') {
                candidatToUpdate.setMontant_total(currentTotal - 25);
            } else {
                candidatToUpdate.setMontant_total(currentTotal - 28);
            }
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updatecouttotcode(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = (int) candidatToUpdate.getMontant_total();
            candidatToUpdate.setMontant_total(currentTotal + 15);
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updatecouttotcode1(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = (int) candidatToUpdate.getMontant_total();
            candidatToUpdate.setMontant_total(currentTotal + 15);
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    public void updateSeancetot1(int cin) throws SQLException {
        Candidat candidatToUpdate = candidatService.getCandidat(cin);
        if (candidatToUpdate != null) {
            int currentTotal = candidatToUpdate.getSeances_totales();
            candidatToUpdate.setSeances_totales(currentTotal - 1);
            candidatService.updateCandidat(candidatToUpdate);
        } else {
            Alert.showErrorAlert("Erreur", "Candidat introuvable avec le CIN : " + cin);
        }
    }

    @FXML
    private void updateCandidat() {
        validateAllFields();

        if (areAllFieldsValid()) {
            try {
                candidat.setNom(nameField.getText().trim());
                candidat.setPrenom(firstNameField.getText().trim());
                candidat.setTelephone(phoneField.getText());
                candidat.setDate_naissance(birthDatePicker.getValue());

                RadioButton selectedRadio = (RadioButton) categoryToggleGroup.getSelectedToggle();
                String category;
                if (selectedRadio == typeARadioButton) {
                    category = "A";
                } else if (selectedRadio == typeBRadioButton) {
                    category = "B";
                } else if (selectedRadio == typeCRadioButton) {
                    category = "C";
                } else {
                    throw new IllegalStateException("Aucune catégorie sélectionnée");
                }
                candidat.setCategorie(category.trim());

                candidat.setEtat(statusComboBox.getValue());
                candidat.setMontant_total(Float.parseFloat(totalAmountField.getText()));
                candidat.setMontant_paye(Float.parseFloat(paidAmountField.getText()));
                candidat.setSeances_totales(Integer.parseInt(totalSessionsField.getText()));
                candidat.setSeances_effectuees(Integer.parseInt(completedSessionsField.getText()));

                candidatService.updateCandidat(candidat);

                okClicked = true;
                Alert.showSuccessAlert("Succès", "Le candidat a été modifié avec succès !");
                dialogStage.close();
            } catch (NumberFormatException e) {
                Alert.showErrorAlert("Erreur", "Veuillez vérifier les valeurs numériques (montants, séances).");
            } catch (Exception e) {
                e.printStackTrace();
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour du candidat : " + e.getMessage());
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void cancel() {
        dialogStage.close();
    }

    private void validateAllFields() {
        validateName();
        validateFirstName();
        validatePhone();
        validateBirthDate();
        validateCategory();
        validateStatus();
        validateTotalAmount();
    }

    private boolean areAllFieldsValid() {
        return nameErrorLabel.getText().isEmpty() &&
                firstNameErrorLabel.getText().isEmpty() &&
                phoneErrorLabel.getText().isEmpty() &&
                birthDateErrorLabel.getText().isEmpty() &&
                categoryErrorLabel.getText().isEmpty() &&
                statusErrorLabel.getText().isEmpty() &&
                totalAmountErrorLabel.getText().isEmpty();
    }

    // Validation du nom (accepte un seul espace entre deux mots)
    private void validateName() {
        String name = nameField.getText().trim();
        boolean isEmpty = name.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = name.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        nameErrorLabel.setText(isValid ? "" : (isEmpty ? "Le nom est obligatoire" : "Lettres uniquement, un seul espace autorisé"));
        nameErrorLabel.setVisible(!isValid);
        nameField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du prénom (accepte un seul espace entre deux mots)
    private void validateFirstName() {
        String firstName = firstNameField.getText().trim();
        boolean isEmpty = firstName.isEmpty();
        // Accepte des lettres (y compris accents) suivies d'un seul espace optionnel, puis d'autres lettres
        boolean isValidFormat = firstName.matches("^[a-zA-ZÀ-ÿ]+( [a-zA-ZÀ-ÿ]+)?$");

        boolean isValid = !isEmpty && isValidFormat;
        firstNameErrorLabel.setText(isValid ? "" : (isEmpty ? "Le prénom est obligatoire" : "Lettres uniquement, un seul espace autorisé"));
        firstNameErrorLabel.setVisible(!isValid);
        firstNameField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validatePhone() {
        String phone = phoneField.getText().trim();
        boolean isValid = phone.matches("\\d{8}");
        phoneErrorLabel.setText(isValid ? "" : "8 chiffres requis");
        phoneErrorLabel.setVisible(!isValid);
        phoneField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateBirthDate() {
        LocalDate birthDate = birthDatePicker.getValue();
        boolean isValid = birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(18));
        birthDateErrorLabel.setText(isValid ? "" : "Âge minimum 18 ans");
        birthDateErrorLabel.setVisible(!isValid);
        birthDatePicker.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateCategory() {
        boolean isValid = categoryToggleGroup.getSelectedToggle() != null;
        categoryErrorLabel.setText(isValid ? "" : "Sélectionnez une catégorie");
        categoryErrorLabel.setVisible(!isValid);
    }

    private void validateStatus() {
        boolean isValid = statusComboBox.getValue() != null && !statusComboBox.getValue().isEmpty();
        statusErrorLabel.setText(isValid ? "" : "Sélectionnez un état");
        statusErrorLabel.setVisible(!isValid);
    }

    private void validateTotalAmount() {
        try {
            float totalAmount = Float.parseFloat(totalAmountField.getText());
            boolean isValid = totalAmount >= 0;
            totalAmountErrorLabel.setText(isValid ? "" : "Doit être positif");
            totalAmountErrorLabel.setVisible(!isValid);
            totalAmountField.setStyle(isValid ? "" : "-fx-border-color: red;");
        } catch (NumberFormatException e) {
            totalAmountErrorLabel.setText("Nombre valide requis");
            totalAmountErrorLabel.setVisible(true);
            totalAmountField.setStyle("-fx-border-color: red;");
        }
    }

    @FXML
    private void close() {
        menuController.loadView("candidat.fxml");
    }
}