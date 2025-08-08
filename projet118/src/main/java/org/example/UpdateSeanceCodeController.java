package org.example;

import Persistance.models.Seance;
import Persistance.utils.Alert; // Import ajouté
import Service.SeanceService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateSeanceCodeController implements Initializable {

    @FXML private TextField cinMoniteurField;
    @FXML private Label cinMoniteurErrorLabel;
    @FXML private TextField cinCandidatField;
    @FXML private Label cinCandidatErrorLabel;
    @FXML private DatePicker dateSeancePicker;
    @FXML private Label dateSeanceErrorLabel;
    @FXML private ComboBox<String> heureSeanceCombo;
    @FXML private Label heureSeanceErrorLabel;
    @FXML private Button saveButton;
    @FXML private Button closeButton; // Ajouté pour référence, si nécessaire

    private Seance seanceToUpdate;
    private Timestamp originalDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 18; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureSeanceCombo.setItems(heures);

        dateSeancePicker.valueProperty().addListener((obs, old, newV) -> validateFieldsCode());
        heureSeanceCombo.valueProperty().addListener((obs, old, newV) -> validateFieldsCode());
        cinMoniteurField.textProperty().addListener((obs, old, newV) -> validateFieldsCode());
    }

    public void setSeance(Seance seance) {
        this.seanceToUpdate = seance;
        this.originalDateTime = seance.getDateTime();
        cinMoniteurField.setText(String.valueOf(seance.getMoniteurId()));
        cinCandidatField.setText(String.valueOf(seance.getCandidatId()));
        dateSeancePicker.setValue(seance.getDateTime().toLocalDateTime().toLocalDate());
        heureSeanceCombo.setValue(seance.getDateTime().toLocalDateTime().toLocalTime().toString().substring(0, 5));
    }

    @FXML
    private void handleSave() throws SQLException {
        if (validateFieldsCode()) {
            long moniteurId = Long.parseLong(cinMoniteurField.getText());
            LocalDate date = dateSeancePicker.getValue();
            String heure = heureSeanceCombo.getValue();
            Timestamp newDateTime = Timestamp.valueOf(date.toString() + " " + heure + ":00");

            seanceToUpdate.setMoniteurId(moniteurId);
            seanceToUpdate.setDateTime(newDateTime);

            SeanceService.updateSeanceCode(seanceToUpdate, originalDateTime);
            Alert.showSuccessAlert("Succès", "La séance de code a été mise à jour avec succès");
            handleCancel();
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow(); // Utiliser saveButton ou closeButton
        stage.close();
    }

    private boolean validateFieldsCode() {
        boolean isValid = true;
        clearErrorsCode();

        if (dateSeancePicker.getValue() == null) {
            dateSeanceErrorLabel.setText("Veuillez sélectionner une date");
            dateSeanceErrorLabel.setVisible(true);
            isValid = false;
        } else if (dateSeancePicker.getValue().isBefore(LocalDate.now())) {
            dateSeanceErrorLabel.setText("Il faut choisir une date future");
            dateSeanceErrorLabel.setVisible(true);
            isValid = false;
        }

        if (heureSeanceCombo.getValue() == null) {
            heureSeanceErrorLabel.setText("Veuillez sélectionner une heure");
            heureSeanceErrorLabel.setVisible(true);
            isValid = false;
        }

        String cinMoniteurText = cinMoniteurField.getText().trim();
        int cinMoniteur;
        if (cinMoniteurText.isEmpty()) {
            cinMoniteurErrorLabel.setText("Veuillez entrer le CIN du moniteur");
            cinMoniteurErrorLabel.setVisible(true);
            isValid = false;
        } else {
            try {
                cinMoniteur = Integer.parseInt(cinMoniteurText);
                if (SeanceService.getMoniteur(cinMoniteur) == null) {
                    cinMoniteurErrorLabel.setText("Ce moniteur n'existe pas");
                    cinMoniteurErrorLabel.setVisible(true);
                    isValid = false;
                } else {
                    List<Seance> seances = SeanceService.getSeancesByMoniteurId(cinMoniteur);
                    List<Seance> seancesCode = SeanceService.getSeancesCodeByMoniteurId(cinMoniteur);
                    Timestamp dateTime = getTimestamp(dateSeancePicker.getValue(), heureSeanceCombo.getValue());
                    if (dateTime != null) {
                        if (isMoniteurBusy(seances, dateTime) && !dateTime.equals(originalDateTime)) {
                            cinMoniteurErrorLabel.setText("Moniteur occupé (conduite)");
                            cinMoniteurErrorLabel.setVisible(true);
                            isValid = false;
                        } else if (isMoniteurBusy(seancesCode, dateTime) && !dateTime.equals(originalDateTime)) {
                            cinMoniteurErrorLabel.setText("Moniteur occupé (code)");
                            cinMoniteurErrorLabel.setVisible(true);
                            isValid = false;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                cinMoniteurErrorLabel.setText("CIN doit être un nombre");
                cinMoniteurErrorLabel.setVisible(true);
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrorsCode() {
        dateSeanceErrorLabel.setText("");
        dateSeanceErrorLabel.setVisible(false);
        heureSeanceErrorLabel.setText("");
        heureSeanceErrorLabel.setVisible(false);
        cinMoniteurErrorLabel.setText("");
        cinMoniteurErrorLabel.setVisible(false);
        cinCandidatErrorLabel.setText("");
        cinCandidatErrorLabel.setVisible(false);
    }

    private Timestamp getTimestamp(LocalDate date, String heure) {
        if (date != null && heure != null) {
            return Timestamp.valueOf(date.toString() + " " + heure + ":00");
        }
        return null;
    }

    private boolean isMoniteurBusy(List<Seance> seances, Timestamp dateTime) {
        return seances.stream().anyMatch(s -> s.getDateTime().equals(dateTime));
    }
}