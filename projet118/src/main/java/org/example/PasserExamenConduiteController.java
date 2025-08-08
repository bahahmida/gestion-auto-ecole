package org.example;

import Persistance.models.PasserExamen;
import Service.PasserExamenService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;


public class PasserExamenConduiteController implements Initializable {

    @FXML private VBox rootContainer;
    @FXML private Label titleLabel;
    @FXML private Label examTypeErrorLabel;
    @FXML private Button backButton;
    @FXML private Button saveButton;
    @FXML private ComboBox<String> examTypeComboBox;

    private int candidateCin;
    private LocalDate examDate;
    private LocalTime examTime;
    private Consumer<PasserExamen> onExamSaved;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        examTypeComboBox.getItems().addAll("Conduite", "Parking");
        examTypeComboBox.setValue("Conduite");

        setupStyles();
        examTypeComboBox.valueProperty().addListener((obs, old, newVal) -> validateExamType());
        validateFXMLInjection();
    }

    public void setOnExamSaved(Consumer<PasserExamen> onExamSaved) {
        this.onExamSaved = onExamSaved;
    }

    private void validateFXMLInjection() {
        Objects.requireNonNull(rootContainer, "rootContainer n'a pas été injecté par FXML");
        Objects.requireNonNull(titleLabel, "titleLabel n'a pas été injecté par FXML");
        Objects.requireNonNull(examTypeErrorLabel, "examTypeErrorLabel n'a pas été injecté par FXML");
        Objects.requireNonNull(backButton, "backButton n'a pas été injecté par FXML");
        Objects.requireNonNull(saveButton, "saveButton n'a pas été injecté par FXML");
        Objects.requireNonNull(examTypeComboBox, "examTypeComboBox n'a pas été injecté par FXML");
    }

    private void setupStyles() {
        try {
            rootContainer.getStyleClass().add("form-container");
            titleLabel.getStyleClass().add("h1");
            backButton.getStyleClass().add("back-button");
            saveButton.getStyleClass().add("primary-button");
            examTypeComboBox.getStyleClass().add("form-combo");
            examTypeErrorLabel.getStyleClass().add("error-label");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'application des styles", e);
        }
    }

    public void initData(int cin, LocalDate date, LocalTime time) {
        this.candidateCin = cin;
        this.examDate = date;
        this.examTime = time;
    }

    private void validateExamType() {
        boolean isValid = examTypeComboBox.getValue() != null;
        examTypeErrorLabel.setVisible(!isValid);
        examTypeErrorLabel.setText(isValid ? "" : "Le type d'examen est requis");
        examTypeComboBox.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    @FXML
    private void handleBack() {
        PasserExamenController.getMenuController().loadView("PasserExamen.fxml");
    }

    @FXML
    private void handleSave() {
        validateExamType();

        if (examTypeErrorLabel.getText().isEmpty()) {
            saveConduiteExam();
        } else {
            showErrorAlert("Erreur", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    private void saveConduiteExam() {
        try {
            String categoriePermis = PasserExamenService.getCandidatCategorie(candidateCin);
            if (categoriePermis == null || categoriePermis.isEmpty()) {
                showErrorAlert("Erreur", "Aucune catégorie de permis trouvée pour ce candidat.");
                return;
            }

            String typeExamen = examTypeComboBox.getValue();
            int idExamen = PasserExamenService.genererIdExamen(categoriePermis, typeExamen);
            LocalDateTime dateHeureExamen = LocalDateTime.of(examDate, examTime);

            if (PasserExamenService.examenExists(candidateCin)) {
                showErrorAlert("Erreur", "Un examen avec une date non dépassée existe déjà pour ce candidat.");
                return;
            } else if (!PasserExamenService.verifierExamenPrecedent(candidateCin, idExamen)) {
                showErrorAlert("Erreur", "L'examen précédent doit être réussi pour enregistrer cet examen.");
                return;
            } else if (!PasserExamenService.tousExamensTermines(candidateCin)) {
                showErrorAlert("Erreur", "Ce candidat a encore des examens en attente.");
                return;
            } else if (PasserExamenService.examenReussiExists(candidateCin, idExamen)) {
                showErrorAlert("Erreur", "Ce candidat a déjà réussi cet examen.");
                return;
            }

            PasserExamen passerExamen = new PasserExamen(
                    this.candidateCin,
                    idExamen,
                    dateHeureExamen,
                    "en attente",
                    PasserExamenService.getExamenInfoById(idExamen).getNomExamen(),
                    PasserExamenService.getExamenInfoById(idExamen).getPrix()
            );
            PasserExamenService.save(passerExamen);

            if (onExamSaved != null) {
                onExamSaved.accept(passerExamen);
            }

            showSuccessAlert("Succès", "Examen enregistré avec succès !");
            handleBack();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Échec de l'enregistrement : " + e.getMessage());
        }
    }


}