package org.example;

import Persistance.models.AutoEcole;
import Persistance.models.Candidat;
import Persistance.models.Paiement;
import Service.CandidatService;
import Service.PaiementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class AddPaimentController {

    @FXML private TextField montantField;
    @FXML private TextArea descriptionField;
    @FXML private Label montantErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private CheckBox printReceiptCheckBox; // Ajout de la CheckBox

    private Stage dialogStage;
    private int cinCandidat;
    private Candidat candidat;
    private PaiementService paiementService;
    private CandidatService candidatService;
    private boolean okClicked = false;

    // Méthode pour initialiser le CIN du candidat
    public void setCinCandidat(int cinCandidat) {
        this.cinCandidat = cinCandidat;
        try {
            this.candidat = candidatService.getCandidat(cinCandidat); // Utiliser CandidatService
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec du chargement du candidat");
            alert.setContentText("Erreur lors de la récupération des informations du candidat : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        // Initialiser les services
        paiementService = new PaiementService();
        candidatService = new CandidatService();

        // Ajouter un écouteur pour valider le montant en temps réel
        montantField.textProperty().addListener((observable, oldValue, newValue) -> validateMontant());
    }

    @FXML
    private void handleSave() {
        validateMontant();

        if (isInputValid()) {
            Paiement paiement = new Paiement(
                    0,
                    cinCandidat,
                    Double.parseDouble(montantField.getText().trim()),
                    LocalDate.now(),
                    descriptionField.getText().trim()
            );

            try {
                paiementService.save(paiement);

                double nouveauMontantPaye = candidat.getMontant_paye() + paiement.getMontant();
                candidat.setMontant_paye(nouveauMontantPaye);
                candidatService.updateCandidat(candidat);

                if (printReceiptCheckBox.isSelected()) {
                    // Définir un chemin fixe pour le fichier PDF
                    String directoryPath = "./files_pdf"; // Chemin relatif au projet
                    File directory = new File(directoryPath);
                    if (!directory.exists()) {
                        directory.mkdirs(); // Créer le dossier s'il n'existe pas
                    }
                    String filePath = directoryPath + "/recu_paiement_" + cinCandidat + "_" + LocalDate.now().toString() + ".pdf";
                    File file = new File(filePath);

                    PaiementService.generateRecuPDF(PaiementService.find(), paiement, filePath);
                } else {
                    showSuccessAlert("Succès", "Le paiement a été ajouté avec succès et le candidat a été mis à jour. Aucun reçu n'a été généré (case non cochée).");
                }

                okClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de l'ajout du paiement, de la mise à jour du candidat ou de la génération du reçu : " + e.getMessage());
            }
        } else {
            showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void close() {
        dialogStage.close();
    }

    // Validation du montant en temps réel
    private void validateMontant() {
        String montantText = montantField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";

        // Vérifier si le montant est vide
        if (montantText.isEmpty()) {
            isValid = false;
            errorMessage = "Le montant est requis";
        } else {
            try {
                double montant = Double.parseDouble(montantText);
                if (montant <= 0) {
                    isValid = false;
                    errorMessage = "Le montant doit être supérieur à 0";
                } else if (candidat != null && montant > (candidat.getMontant_total() - candidat.getMontant_paye())) {
                    isValid = false;
                    errorMessage = "Montant trop élevé (solde restant : " + (candidat.getMontant_total() - candidat.getMontant_paye()) + ")";
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage = "Le montant doit être un nombre valide";
            }
        }

        montantErrorLabel.setVisible(!isValid);
        montantErrorLabel.setManaged(!isValid);
        montantErrorLabel.setText(errorMessage);
        montantField.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    // Vérifier si tous les champs sont valides
    private boolean isInputValid() {
        // La description est facultative, donc pas de validation stricte
        descriptionErrorLabel.setText("");
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        return !montantErrorLabel.isVisible();
    }


}