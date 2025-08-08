package org.example;

import Persistance.models.DocumentVehicule;
import Persistance.models.Vehicule;
import Service.VehiculeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class AddVehiculeController {
    private MenuController menuController;
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML private TextField marqueField;
    @FXML private Label marqueErrorLabel;
    @FXML private TextField modeleField;
    @FXML private Label modeleErrorLabel;
    @FXML private TextField numeroSerieField;
    @FXML private TextField numeroSequentielField;
    @FXML private Label immatriculationErrorLabel;
    @FXML private TextField anneeField;
    @FXML private Label anneeErrorLabel;
    @FXML private TextField kmField;
    @FXML private Label kmErrorLabel;
    @FXML private RadioButton categorieA;
    @FXML private RadioButton categorieB;
    @FXML private RadioButton categorieC;
    @FXML private ToggleGroup categorieToggleGroup;
    @FXML private Label categorieErrorLabel;
    @FXML private DatePicker visiteTechniqueDatePicker;
    @FXML private Label visiteTechniqueErrorLabel;
    @FXML private DatePicker assuranceDatePicker;
    @FXML private Label assuranceErrorLabel;
    @FXML private TextField vidangeKmField;
    @FXML private Label vidangeKmErrorLabel;
    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Stage dialogStage;
    private boolean okClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        if (categorieToggleGroup == null) {
            categorieToggleGroup = new ToggleGroup();
            categorieA.setToggleGroup(categorieToggleGroup);
            categorieB.setToggleGroup(categorieToggleGroup);
            categorieC.setToggleGroup(categorieToggleGroup);
        }
        categorieB.setSelected(true);

        marqueField.textProperty().addListener((obs, old, newV) -> validateMarque());
        modeleField.textProperty().addListener((obs, old, newV) -> validateModele());
        numeroSerieField.textProperty().addListener((obs, old, newV) -> validateImmatriculation());
        numeroSequentielField.textProperty().addListener((obs, old, newV) -> validateImmatriculation());
        anneeField.textProperty().addListener((obs, old, newV) -> validateAnnee());
        kmField.textProperty().addListener((obs, old, newV) -> validateKm());
        categorieToggleGroup.selectedToggleProperty().addListener((obs, old, newV) -> validateCategorie());
        visiteTechniqueDatePicker.valueProperty().addListener((obs, old, newV) -> validateVisiteTechnique());
        assuranceDatePicker.valueProperty().addListener((obs, old, newV) -> validateAssurance());
        vidangeKmField.textProperty().addListener((obs, old, newV) -> validateVidangeKm());

        // Initialiser les labels d'erreur à désactivés
        clearFields();
    }

    @FXML
    private void close() {
        menuController.loadView("vehicule.fxml");
    }

    @FXML
    private void handleCancel() {
        clearFields(); // Vider les champs sans fermer dialogStage
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            try {
                Vehicule vehicule = createVehicule();
                List<DocumentVehicule> documents = createDocuments();
                int generatedId = VehiculeService.save(vehicule, documents);
                okClicked = true;

                // Afficher l'alerte de succès sans l'ID
                showSuccessAlert("Succès", "Véhicule ajouté avec succès.");

                // Vider tous les champs après le succès
                clearFields();
                VehiculeController vehiculeController = new VehiculeController();
                vehiculeController.insertNotificationsForDueDates();
                if (menuController != null) {
                    menuController.checkForNotifications();
                }
                close();

            } catch (SQLException e) {
                showErrorAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur", "Veuillez corriger les erreurs de saisie.");
        }
    }

    private boolean isInputValid() {
        validateMarque();
        validateModele();
        validateImmatriculation();
        validateAnnee();
        validateKm();
        validateCategorie();
        validateVisiteTechnique();
        validateAssurance();
        validateVidangeKm();

        return marqueErrorLabel.getText().isEmpty() &&
                modeleErrorLabel.getText().isEmpty() &&
                immatriculationErrorLabel.getText().isEmpty() &&
                anneeErrorLabel.getText().isEmpty() &&
                kmErrorLabel.getText().isEmpty() &&
                categorieErrorLabel.getText().isEmpty() &&
                visiteTechniqueErrorLabel.getText().isEmpty() &&
                assuranceErrorLabel.getText().isEmpty() &&
                vidangeKmErrorLabel.getText().isEmpty();
    }

    private void validateMarque() {
        String value = marqueField.getText().trim();
        boolean isValid = !value.isEmpty() && value.matches("[a-zA-Z0-9 ]+");
        marqueErrorLabel.setText(isValid ? "" : value.isEmpty() ? "Marque requise" : "Marque invalide");
        marqueErrorLabel.setVisible(!isValid);
        marqueErrorLabel.setManaged(!isValid);
        marqueField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateModele() {
        String value = modeleField.getText().trim();
        boolean isValid = !value.isEmpty() && value.matches("[a-zA-Z0-9 ]+");
        modeleErrorLabel.setText(isValid ? "" : value.isEmpty() ? "Modèle requis" : "Modèle invalide");
        modeleErrorLabel.setVisible(!isValid);
        modeleErrorLabel.setManaged(!isValid);
        modeleField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateImmatriculation() {
        String serie = numeroSerieField.getText().trim();
        String sequentiel = numeroSequentielField.getText().trim();
        String immatriculation = sequentiel + " تونس " + serie;

        boolean isFieldsValid = !serie.isEmpty() && !sequentiel.isEmpty();

        if (!isFieldsValid) {
            immatriculationErrorLabel.setText("Numéro série et séquentiel requis");
            immatriculationErrorLabel.setVisible(true);
            immatriculationErrorLabel.setManaged(true);
            numeroSerieField.setStyle(serie.isEmpty() ? "-fx-border-color: red;" : "");
            numeroSequentielField.setStyle(sequentiel.isEmpty() ? "-fx-border-color: red;" : "");
            return;
        }

        try {
            boolean exists = VehiculeService.VehiculeExists(immatriculation);
            if (exists) {
                immatriculationErrorLabel.setText("Ce numéro d'immatriculation existe déjà");
                immatriculationErrorLabel.setVisible(true);
                immatriculationErrorLabel.setManaged(true);
                numeroSerieField.setStyle("-fx-border-color: red;");
                numeroSequentielField.setStyle("-fx-border-color: red;");
            } else {
                immatriculationErrorLabel.setText("");
                immatriculationErrorLabel.setVisible(false);
                immatriculationErrorLabel.setManaged(false);
                numeroSerieField.setStyle("");
                numeroSequentielField.setStyle("");
            }
        } catch (SQLException e) {
            immatriculationErrorLabel.setText("Erreur lors de la vérification: " + e.getMessage());
            immatriculationErrorLabel.setVisible(true);
            immatriculationErrorLabel.setManaged(true);
            numeroSerieField.setStyle("-fx-border-color: red;");
            numeroSequentielField.setStyle("-fx-border-color: red;");
        }
    }

    private void validateAnnee() {
        try {
            String value = anneeField.getText().trim();
            if (value.isEmpty()) {
                anneeErrorLabel.setText("Année requise");
                anneeErrorLabel.setVisible(true);
                anneeErrorLabel.setManaged(true);
                anneeField.setStyle("-fx-border-color: red;");
            } else {
                int annee = Integer.parseInt(value);
                boolean isValid = annee >= 1900 && annee <= LocalDate.now().getYear();
                anneeErrorLabel.setText(isValid ? "" : "Année entre 1900 et " + LocalDate.now().getYear());
                anneeErrorLabel.setVisible(!isValid);
                anneeErrorLabel.setManaged(!isValid);
                anneeField.setStyle(isValid ? "" : "-fx-border-color: red;");
            }
        } catch (NumberFormatException e) {
            anneeErrorLabel.setText("Année doit être un nombre");
            anneeErrorLabel.setVisible(true);
            anneeErrorLabel.setManaged(true);
            anneeField.setStyle("-fx-border-color: red;");
        }
    }

    private void validateKm() {
        try {
            String value = kmField.getText().trim();
            if (value.isEmpty()) {
                kmErrorLabel.setText("Kilométrage requis");
                kmErrorLabel.setVisible(true);
                kmErrorLabel.setManaged(true);
                kmField.setStyle("-fx-border-color: red;");
            } else {
                int km = Integer.parseInt(value);
                boolean isValid = km >= 0;
                kmErrorLabel.setText(isValid ? "" : "Kilométrage positif requis");
                kmErrorLabel.setVisible(!isValid);
                kmErrorLabel.setManaged(!isValid);
                kmField.setStyle(isValid ? "" : "-fx-border-color: red;");
            }
        } catch (NumberFormatException e) {
            kmErrorLabel.setText("Kilométrage doit être un nombre");
            kmErrorLabel.setVisible(true);
            kmErrorLabel.setManaged(true);
            kmField.setStyle("-fx-border-color: red;");
        }
    }

    private void validateCategorie() {
        boolean isValid = categorieToggleGroup.getSelectedToggle() != null;
        categorieErrorLabel.setText(isValid ? "" : "Catégorie requise");
        categorieErrorLabel.setVisible(!isValid);
        categorieErrorLabel.setManaged(!isValid);
        categorieA.setStyle(isValid ? "" : "-fx-text-fill: red;");
        categorieB.setStyle(isValid ? "" : "-fx-text-fill: red;");
        categorieC.setStyle(isValid ? "" : "-fx-text-fill: red;");
    }

    private void validateVisiteTechnique() {
        LocalDate date = visiteTechniqueDatePicker.getValue();
        if (date == null) {
            visiteTechniqueErrorLabel.setText("Date visite technique requise");
            visiteTechniqueErrorLabel.setVisible(true);
            visiteTechniqueErrorLabel.setManaged(true);
            visiteTechniqueDatePicker.setStyle("-fx-border-color: red;");
        } else {
            boolean isValid = !date.isBefore(LocalDate.now());
            visiteTechniqueErrorLabel.setText(isValid ? "" : "Date doit être aujourd'hui ou future");
            visiteTechniqueErrorLabel.setVisible(!isValid);
            visiteTechniqueErrorLabel.setManaged(!isValid);
            visiteTechniqueDatePicker.setStyle(isValid ? "" : "-fx-border-color: red;");
        }
    }

    private void validateAssurance() {
        LocalDate date = assuranceDatePicker.getValue();
        if (date == null) {
            assuranceErrorLabel.setText("Date assurance requise");
            assuranceErrorLabel.setVisible(true);
            assuranceErrorLabel.setManaged(true);
            assuranceDatePicker.setStyle("-fx-border-color: red;");
        } else {
            boolean isValid = !date.isBefore(LocalDate.now());
            assuranceErrorLabel.setText(isValid ? "" : "Date doit être aujourd'hui ou future");
            assuranceErrorLabel.setVisible(!isValid);
            assuranceErrorLabel.setManaged(!isValid);
            assuranceDatePicker.setStyle(isValid ? "" : "-fx-border-color: red;");
        }
    }

    private void validateVidangeKm() {
        String value = vidangeKmField.getText().trim();
        if (value.isEmpty()) {
            vidangeKmErrorLabel.setText("Kilométrage vidange requis");
            vidangeKmErrorLabel.setVisible(true);
            vidangeKmErrorLabel.setManaged(true);
            vidangeKmField.setStyle("-fx-border-color: red;");
        } else {
            try {
                int kmEcheance = Integer.parseInt(value);
                int kmActuel = kmField.getText().trim().isEmpty() ? 0 : Integer.parseInt(kmField.getText().trim());
                boolean isValid = kmEcheance > kmActuel;
                vidangeKmErrorLabel.setText(isValid ? "" : "Doit être > km actuel");
                vidangeKmErrorLabel.setVisible(!isValid);
                vidangeKmErrorLabel.setManaged(!isValid);
                vidangeKmField.setStyle(isValid ? "" : "-fx-border-color: red;");
            } catch (NumberFormatException e) {
                vidangeKmErrorLabel.setText("Nombre invalide");
                vidangeKmErrorLabel.setVisible(true);
                vidangeKmErrorLabel.setManaged(true);
                vidangeKmField.setStyle("-fx-border-color: red;");
            }
        }
    }

    private Vehicule createVehicule() {
        String immatriculation = numeroSequentielField.getText().trim() + " تونس " + numeroSerieField.getText().trim();
        return new Vehicule(
                0,
                marqueField.getText().trim(),
                modeleField.getText().trim(),
                immatriculation,
                Integer.parseInt(anneeField.getText().trim()),
                Integer.parseInt(kmField.getText().trim()),
                getSelectedCategorie()
        );
    }

    private List<DocumentVehicule> createDocuments() {
        List<DocumentVehicule> documents = new ArrayList<>();

        DocumentVehicule viniette = new DocumentVehicule(0, 0, 1);
        viniette.setDateEcheance(VehiculeService.calculateDateEcheance(numeroSerieField.getText().trim()));
        documents.add(viniette);

        DocumentVehicule visite = new DocumentVehicule(0, 0, 2);
        visite.setDateEcheance(visiteTechniqueDatePicker.getValue());
        documents.add(visite);

        DocumentVehicule assurance = new DocumentVehicule(0, 0, 3);
        assurance.setDateEcheance(assuranceDatePicker.getValue());
        documents.add(assurance);

        DocumentVehicule vidange = new DocumentVehicule(0, 0, 4);
        vidange.setKilometrageEcheance(Integer.parseInt(vidangeKmField.getText().trim()));
        documents.add(vidange);

        return documents;
    }

    private char getSelectedCategorie() {
        RadioButton selected = (RadioButton) categorieToggleGroup.getSelectedToggle();
        if (selected == null) return 'B';
        if (selected.equals(categorieA)) return 'A';
        if (selected.equals(categorieB)) return 'B';
        return 'C';
    }

    private void clearFields() {
        marqueField.clear();
        modeleField.clear();
        numeroSerieField.clear();
        numeroSequentielField.clear();
        anneeField.clear();
        kmField.clear();
        categorieToggleGroup.selectToggle(null);
        visiteTechniqueDatePicker.setValue(null);
        assuranceDatePicker.setValue(null);
        vidangeKmField.clear();

        // Désactiver tous les labels d'erreur
        marqueErrorLabel.setText("");
        marqueErrorLabel.setVisible(false);
        marqueErrorLabel.setManaged(false);
        modeleErrorLabel.setText("");
        modeleErrorLabel.setVisible(false);
        modeleErrorLabel.setManaged(false);
        immatriculationErrorLabel.setText("");
        immatriculationErrorLabel.setVisible(false);
        immatriculationErrorLabel.setManaged(false);
        anneeErrorLabel.setText("");
        anneeErrorLabel.setVisible(false);
        anneeErrorLabel.setManaged(false);
        kmErrorLabel.setText("");
        kmErrorLabel.setVisible(false);
        kmErrorLabel.setManaged(false);
        categorieErrorLabel.setText("");
        categorieErrorLabel.setVisible(false);
        categorieErrorLabel.setManaged(false);
        visiteTechniqueErrorLabel.setText("");
        visiteTechniqueErrorLabel.setVisible(false);
        visiteTechniqueErrorLabel.setManaged(false);
        assuranceErrorLabel.setText("");
        assuranceErrorLabel.setVisible(false);
        assuranceErrorLabel.setManaged(false);
        vidangeKmErrorLabel.setText("");
        vidangeKmErrorLabel.setVisible(false);
        vidangeKmErrorLabel.setManaged(false);

        // Réinitialiser les styles des champs
        marqueField.setStyle("");
        modeleField.setStyle("");
        numeroSerieField.setStyle("");
        numeroSequentielField.setStyle("");
        anneeField.setStyle("");
        kmField.setStyle("");
        vidangeKmField.setStyle("");
        categorieA.setStyle("");
        categorieB.setStyle("");
        categorieC.setStyle("");
        visiteTechniqueDatePicker.setStyle("");
        assuranceDatePicker.setStyle("");
    }


}