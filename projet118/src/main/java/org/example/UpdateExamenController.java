package org.example;

import Persistance.models.PasserExamen;
import Persistance.utils.Alert; // Import ajouté
import Service.PasserExamenService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.collections.FXCollections;

public class UpdateExamenController {

    @FXML private TextField idField;
    @FXML private TextField cinCandidatField;
    @FXML private ComboBox<String> nomExamenCombo;
    @FXML private DatePicker dateExamenPicker;
    @FXML private ComboBox<LocalTime> heureExamenCombo;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> resultatCombo;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    // Labels pour les messages d'erreur
    @FXML private Label dateExamenErrorLabel;
    @FXML private Label heureExamenErrorLabel;
    @FXML private Label resultatErrorLabel;

    private PasserExamen examen;
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        // Effacer les messages d'erreur au démarrage
        clearErrorMessages();

        // Initialiser les ComboBox
        nomExamenCombo.setItems(FXCollections.observableArrayList("Code", "Conduite"));
        heureExamenCombo.setItems(FXCollections.observableArrayList(
                LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30),
                LocalTime.of(12, 0), LocalTime.of(12, 30)
        ));
        resultatCombo.setItems(FXCollections.observableArrayList("En attente", "Réussi", "Échoué"));

        // Ajouter des écouteurs pour valider les champs modifiables en temps réel
        dateExamenPicker.valueProperty().addListener((obs, old, newVal) -> validateDateExamen());
        heureExamenCombo.valueProperty().addListener((obs, old, newVal) -> validateHeureExamen());
        resultatCombo.valueProperty().addListener((obs, old, newVal) -> validateResultat());
    }

    public void setExamen(PasserExamen examen) {
        this.examen = examen;

        // Remplir les champs avec les données de l'examen
        idField.setText(String.valueOf(examen.getIdExamen()));
        cinCandidatField.setText(String.valueOf(examen.getCinCondidat()));
        nomExamenCombo.setValue(examen.getNomExamen());
        dateExamenPicker.setValue(examen.getDateExamen().toLocalDate());
        heureExamenCombo.setValue(examen.getDateExamen().toLocalTime());
        prixField.setText(String.valueOf(examen.getPrix()));
        resultatCombo.setValue(examen.getResultatExamen());

        // Désactiver les champs non modifiables
        idField.setDisable(true);
        cinCandidatField.setDisable(true);
        nomExamenCombo.setDisable(true);
        prixField.setDisable(true);

        // Désactiver date et heure si la date initiale est passée
        LocalDate initialDate = examen.getDateExamen().toLocalDate();
        if (initialDate.isBefore(LocalDate.now())) {
            dateExamenPicker.setDisable(true);
            heureExamenCombo.setDisable(true);
            dateExamenErrorLabel.setVisible(false);
            dateExamenErrorLabel.setManaged(false);
            heureExamenErrorLabel.setVisible(false);
            heureExamenErrorLabel.setManaged(false);
            dateExamenErrorLabel.setText("");
            heureExamenErrorLabel.setText("");
        }

        // Désactiver le champ de résultat si la date n'est pas dépassée (aujourd'hui ou dans le futur)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateExamen = examen.getDateExamen();
        boolean isDateNotPassed = dateExamen != null && !dateExamen.isBefore(now);
        resultatCombo.setDisable(isDateNotPassed);
        if (isDateNotPassed) {
            resultatCombo.setStyle("-fx-opacity: 0.5;"); // Optionnel : rendre visuellement grisé
            resultatErrorLabel.setVisible(false);
            resultatErrorLabel.setManaged(false);
            resultatErrorLabel.setText("");
        } else {
            resultatCombo.setStyle(""); // Style normal
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
        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        // Valider les champs modifiables
        validateDateExamen();
        validateHeureExamen();
        validateResultat();

        // Vérifier si tous les champs sont valides
        if (dateExamenErrorLabel.getText().isEmpty() &&
                heureExamenErrorLabel.getText().isEmpty() &&
                resultatErrorLabel.getText().isEmpty()) {
            try {
                // Vérifier si l'examen existe avec les critères actuels
                LocalDateTime oldDateExamen = examen.getDateExamen();

                // Mettre à jour les champs modifiables
                LocalDateTime newDateExamen = LocalDateTime.of(
                        dateExamenPicker.getValue(),
                        heureExamenCombo.getValue()
                );
                examen.setDateExamen(newDateExamen);
                examen.setResultatExamen(resultatCombo.getValue());

                // Mettre à jour dans la base avec l'ancienne date pour le WHERE
                PasserExamenService.update(examen, oldDateExamen);
                okClicked = true;
                dialogStage.close();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    // Validation de la date
    private void validateDateExamen() {
        // Ne rien faire si le champ est désactivé (date initiale passée)
        if (dateExamenPicker.isDisable()) {
            dateExamenErrorLabel.setVisible(false);
            dateExamenErrorLabel.setManaged(false);
            dateExamenErrorLabel.setText("");
            dateExamenPicker.setStyle("");
            return;
        }

        // Valider uniquement si le champ est actif
        LocalDate date = dateExamenPicker.getValue();
        boolean isValid = date != null && !date.isBefore(LocalDate.now());
        dateExamenErrorLabel.setVisible(!isValid);
        dateExamenErrorLabel.setManaged(!isValid);
        dateExamenErrorLabel.setText(isValid ? "" : (date == null ? "La date est obligatoire" : "La date ne peut pas être passée"));
        dateExamenPicker.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation de l'heure
    private void validateHeureExamen() {
        // Ne rien faire si le champ est désactivé (date initiale passée)
        if (heureExamenCombo.isDisable()) {
            heureExamenErrorLabel.setVisible(false);
            heureExamenErrorLabel.setManaged(false);
            heureExamenErrorLabel.setText("");
            heureExamenCombo.setStyle("");
            return;
        }

        // Valider uniquement si le champ est actif
        boolean isValid = heureExamenCombo.getValue() != null;
        heureExamenErrorLabel.setVisible(!isValid);
        heureExamenErrorLabel.setManaged(!isValid);
        heureExamenErrorLabel.setText(isValid ? "" : "L'heure est obligatoire");
        heureExamenCombo.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validation du résultat
    private void validateResultat() {
        // Ne rien faire si le champ est désactivé (date non dépassée)
        if (resultatCombo.isDisable()) {
            resultatErrorLabel.setVisible(false);
            resultatErrorLabel.setManaged(false);
            resultatErrorLabel.setText("");
            resultatCombo.setStyle("");
            return;
        }

        // Valider uniquement si le champ est actif
        boolean isValid = resultatCombo.getValue() != null;
        resultatErrorLabel.setVisible(!isValid);
        resultatErrorLabel.setManaged(!isValid);
        resultatErrorLabel.setText(isValid ? "" : "Le résultat est obligatoire");
        resultatCombo.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void clearErrorMessages() {
        dateExamenErrorLabel.setText("");
        dateExamenErrorLabel.setVisible(false);
        dateExamenErrorLabel.setManaged(false);
        heureExamenErrorLabel.setText("");
        heureExamenErrorLabel.setVisible(false);
        heureExamenErrorLabel.setManaged(false);
        resultatErrorLabel.setText("");
        resultatErrorLabel.setVisible(false);
        resultatErrorLabel.setManaged(false);

        // Réinitialiser les styles
        dateExamenPicker.setStyle("");
        heureExamenCombo.setStyle("");
        resultatCombo.setStyle("");
    }

    @FXML
    public void close(ActionEvent actionEvent) {
        dialogStage.close();
    }
}