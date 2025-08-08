package org.example;

import Persistance.models.DocumentVehicule;
import Persistance.models.Vehicule;
import Persistance.utils.Alert; // Import ajouté
import Service.VehiculeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpdateVehiculeController {
    private Stage dialogStage;
    private Vehicule vehicule;
    private boolean okClicked = false;
    private VehiculeService vehiculeService = new VehiculeService();
    private int ancienKmEcheance; // Variable pour stocker l'ancienne valeur du kilométrage d'échéance

    // FXML elements
    @FXML private TextField immatriculationField;
    @FXML private TextField kmActuelField;
    @FXML private Label kmActuelErrorLabel;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // Nouveaux éléments FXML
    @FXML private DatePicker echeanceVignetteField;
    @FXML private DatePicker echeanceVisiteTechniqueField;
    @FXML private DatePicker echeanceAssuranceField;
    @FXML private TextField kmEcheanceField;
    @FXML private Label echeanceVignetteErrorLabel;
    @FXML private Label echeanceVisiteTechniqueErrorLabel;
    @FXML private Label echeanceAssuranceErrorLabel;
    @FXML private Label kmEcheanceErrorLabel;

    // Set the dialog stage
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Set the vehicle to update
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        populateFields(); // Remplir les champs avec les anciennes valeurs
    }

    // Check if the user clicked "Enregistrer"
    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        // Désactiver les champs qui ne doivent pas être modifiés
        immatriculationField.setDisable(true);
        echeanceVignetteField.setDisable(true);

        // Add listener for real-time validation of kilometrage
        kmActuelField.textProperty().addListener((obs, old, newVal) -> validateKmActuel());
        kmEcheanceField.textProperty().addListener((obs, old, newVal) -> validateKmEcheance());

        // Add listeners for date validation
        echeanceVisiteTechniqueField.valueProperty().addListener((obs, old, newVal) -> validateDateVisiteTechnique());
        echeanceAssuranceField.valueProperty().addListener((obs, old, newVal) -> validateDateAssurance());

        // Initialiser les labels d'erreur à désactivés
        clearErrorLabels();
    }

    // Populate the fields with the vehicle's current data
    private void populateFields() {
        if (vehicule != null) {
            immatriculationField.setText(vehicule.getImmatriculation());
            kmActuelField.setText(String.valueOf(vehicule.getKmActuel()));


            List<DocumentVehicule> documentVehicules = VehiculeService.getDocumentsForVehicule(vehicule.getIdVehicule());

            for (DocumentVehicule doc : documentVehicules) {
                switch (doc.getIdTypeDocument()) {
                    case 1: // Vignette
                        if (doc.getDateEcheance() != null) {
                            echeanceVignetteField.setValue(doc.getDateEcheance());
                        }
                        break;
                    case 2: // Visite technique
                        if (doc.getDateEcheance() != null) {
                            echeanceVisiteTechniqueField.setValue(doc.getDateEcheance());
                        }
                        break;
                    case 3: // Assurance
                        if (doc.getDateEcheance() != null) {
                            echeanceAssuranceField.setValue(doc.getDateEcheance());
                        }
                        break;
                    case 4: // Vidange
                        if (doc.getKilometrageEcheance() > 0) {
                            kmEcheanceField.setText(String.valueOf(doc.getKilometrageEcheance()));
                            ancienKmEcheance = doc.getKilometrageEcheance(); // Stocker l'ancienne valeur
                        }
                        break;
                }
            }

            validateKmActuel();
            validateKmEcheance();
            validateDateVisiteTechnique();
            validateDateAssurance();
        }
    }

    // Validate the kilometrage field
    private void validateKmActuel() {
        String kmText = kmActuelField.getText().trim();
        boolean isValid = true;

        if (kmText.isEmpty()) {
            kmActuelErrorLabel.setText("Le kilométrage est obligatoire");
            kmActuelErrorLabel.setVisible(true);
            kmActuelErrorLabel.setManaged(true);
            isValid = false;
        } else {
            try {
                int km = Integer.parseInt(kmText);
                if (km < 0) {
                    kmActuelErrorLabel.setText("Le kilométrage doit être positif");
                    kmActuelErrorLabel.setVisible(true);
                    kmActuelErrorLabel.setManaged(true);
                    isValid = false;
                } else if (vehicule != null && km < vehicule.getKmActuel()) {
                    kmActuelErrorLabel.setText("Le kilométrage doit être supérieur à la valeur actuelle");
                    kmActuelErrorLabel.setVisible(true);
                    kmActuelErrorLabel.setManaged(true);
                    isValid = false;
                } else {
                    kmActuelErrorLabel.setText("");
                    kmActuelErrorLabel.setVisible(false);
                    kmActuelErrorLabel.setManaged(false);
                }
            } catch (NumberFormatException e) {
                kmActuelErrorLabel.setText("Nombre valide requis");
                kmActuelErrorLabel.setVisible(true);
                kmActuelErrorLabel.setManaged(true);
                isValid = false;
            }
        }

        kmActuelField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validate kilométrage d'échéance
    private void validateKmEcheance() {
        String kmText = kmEcheanceField.getText().trim();
        boolean isValid = true;

        if (kmText.isEmpty()) {
            kmEcheanceErrorLabel.setText("Le kilométrage d'échéance est obligatoire");
            kmEcheanceErrorLabel.setVisible(true);
            kmEcheanceErrorLabel.setManaged(true);
            isValid = false;
        } else {
            try {
                int km = Integer.parseInt(kmText);
                if (km < 0) {
                    kmEcheanceErrorLabel.setText("Le kilométrage d'échéance doit être positif");
                    kmEcheanceErrorLabel.setVisible(true);
                    kmEcheanceErrorLabel.setManaged(true);
                    isValid = false;
                } else if (km < ancienKmEcheance) {
                    kmEcheanceErrorLabel.setText("Le kilométrage d'échéance doit être supérieur ou égal à l'ancienne valeur (" + ancienKmEcheance + ")");
                    kmEcheanceErrorLabel.setVisible(true);
                    kmEcheanceErrorLabel.setManaged(true);
                    isValid = false;
                } else {
                    kmEcheanceErrorLabel.setText("");
                    kmEcheanceErrorLabel.setVisible(false);
                    kmEcheanceErrorLabel.setManaged(false);
                }
            } catch (NumberFormatException e) {
                kmEcheanceErrorLabel.setText("Nombre valide requis");
                kmEcheanceErrorLabel.setVisible(true);
                kmEcheanceErrorLabel.setManaged(true);
                isValid = false;
            }
        }

        kmEcheanceField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validate date d'échéance vignette (non utilisé car désactivé)
    private void validateDateVignette() {
        // Pas de validation car le champ est désactivé
        echeanceVignetteErrorLabel.setText("");
        echeanceVignetteErrorLabel.setVisible(false);
        echeanceVignetteErrorLabel.setManaged(false);
    }

    // Validate date d'échéance visite technique
    private void validateDateVisiteTechnique() {
        LocalDate date = echeanceVisiteTechniqueField.getValue();
        boolean isValid = true;

        if (date == null) {
            echeanceVisiteTechniqueErrorLabel.setText("La date d'échéance est obligatoire");
            echeanceVisiteTechniqueErrorLabel.setVisible(true);
            echeanceVisiteTechniqueErrorLabel.setManaged(true);
            isValid = false;
        } else if (date.isBefore(LocalDate.now())) {
            echeanceVisiteTechniqueErrorLabel.setText("La date d'échéance doit être dans le futur");
            echeanceVisiteTechniqueErrorLabel.setVisible(true);
            echeanceVisiteTechniqueErrorLabel.setManaged(true);
            isValid = false;
        } else {
            echeanceVisiteTechniqueErrorLabel.setText("");
            echeanceVisiteTechniqueErrorLabel.setVisible(false);
            echeanceVisiteTechniqueErrorLabel.setManaged(false);
        }

        echeanceVisiteTechniqueField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Validate date d'échéance assurance
    private void validateDateAssurance() {
        LocalDate date = echeanceAssuranceField.getValue();
        boolean isValid = true;

        if (date == null) {
            echeanceAssuranceErrorLabel.setText("La date d'échéance est obligatoire");
            echeanceAssuranceErrorLabel.setVisible(true);
            echeanceAssuranceErrorLabel.setManaged(true);
            isValid = false;
        } else if (date.isBefore(LocalDate.now())) {
            echeanceAssuranceErrorLabel.setText("La date d'échéance doit être dans le futur");
            echeanceAssuranceErrorLabel.setVisible(true);
            echeanceAssuranceErrorLabel.setManaged(true);
            isValid = false;
        } else {
            echeanceAssuranceErrorLabel.setText("");
            echeanceAssuranceErrorLabel.setVisible(false);
            echeanceAssuranceErrorLabel.setManaged(false);
        }

        echeanceAssuranceField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Check if all fields are valid
    private boolean areAllFieldsValid() {
        validateKmActuel();
        validateKmEcheance();
        validateDateVisiteTechnique();
        validateDateAssurance();

        return kmActuelErrorLabel.getText().isEmpty() &&
                kmEcheanceErrorLabel.getText().isEmpty() &&
                echeanceVisiteTechniqueErrorLabel.getText().isEmpty() &&
                echeanceAssuranceErrorLabel.getText().isEmpty();
    }

    @FXML
    private void updateVehicule() {
        if (areAllFieldsValid()) {
            try {
                // Mettre à jour les informations de base du véhicule
                vehicule.setKmActuel(Integer.parseInt(kmActuelField.getText().trim()));

                // Créer une liste de DocumentVehicule pour les documents
                List<DocumentVehicule> documents = new ArrayList<>();

                // Document 2 : Visite technique
                DocumentVehicule visiteTechnique = new DocumentVehicule();
                visiteTechnique.setIdTypeDocument(2); // ID 2 pour visite technique
                visiteTechnique.setDateEcheance(echeanceVisiteTechniqueField.getValue());
                visiteTechnique.setKilometrageEcheance(0); // Pas de kilométrage pour la visite technique
                documents.add(visiteTechnique);

                // Document 3 : Assurance
                DocumentVehicule assurance = new DocumentVehicule();
                assurance.setIdTypeDocument(3); // ID 3 pour assurance
                assurance.setDateEcheance(echeanceAssuranceField.getValue());
                assurance.setKilometrageEcheance(0); // Pas de kilométrage pour l'assurance
                documents.add(assurance);

                // Document 4 : Vidange
                DocumentVehicule vidange = new DocumentVehicule();
                vidange.setIdTypeDocument(4); // ID 4 pour vidange
                vidange.setDateEcheance(null); // Pas de date pour la vidange
                vidange.setKilometrageEcheance(Integer.parseInt(kmEcheanceField.getText().trim()));
                documents.add(vidange);

                // Appeler la méthode combinée pour mettre à jour le véhicule et ses documents
                VehiculeService.updateVehicule(vehicule, documents);

                okClicked = true;
                Alert.showSuccessAlert("Succès", "Le véhicule et ses documents ont été modifiés avec succès !");
                dialogStage.close();
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour du véhicule : " + e.getMessage());
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void cancel() {
        dialogStage.close();
    }

    private void clearErrorLabels() {
        kmActuelErrorLabel.setText("");
        kmActuelErrorLabel.setVisible(false);
        kmActuelErrorLabel.setManaged(false);
        echeanceVignetteErrorLabel.setText("");
        echeanceVignetteErrorLabel.setVisible(false);
        echeanceVignetteErrorLabel.setManaged(false);
        echeanceVisiteTechniqueErrorLabel.setText("");
        echeanceVisiteTechniqueErrorLabel.setVisible(false);
        echeanceVisiteTechniqueErrorLabel.setManaged(false);
        echeanceAssuranceErrorLabel.setText("");
        echeanceAssuranceErrorLabel.setVisible(false);
        echeanceAssuranceErrorLabel.setManaged(false);
        kmEcheanceErrorLabel.setText("");
        kmEcheanceErrorLabel.setVisible(false);
        kmEcheanceErrorLabel.setManaged(false);

        // Réinitialiser les styles des champs
        kmActuelField.setStyle("");
        echeanceVisiteTechniqueField.setStyle("");
        echeanceAssuranceField.setStyle("");
        kmEcheanceField.setStyle("");
    }
}