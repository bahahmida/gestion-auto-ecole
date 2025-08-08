package org.example;

import Persistance.models.DocumentVehicule;
import Persistance.models.Vehicule;
import Service.VehiculeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DocumentVehiculeController {

    @FXML
    private TableView<DocumentVehicule> documentVehiculeTable;

    @FXML
    private TableColumn<DocumentVehicule, String> documentColumn;

    @FXML
    private TableColumn<DocumentVehicule, String> echeanceColumn;

    @FXML
    private Button closeButton;

    private ObservableList<DocumentVehicule> documentList;
    private Vehicule vehicule; // Ajout de la référence au véhicule
    private Stage dialogStage; // Pour gérer la fenêtre modale

    // Setter pour le véhicule (appelé par VehiculeController)
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        loadDocuments(); // Charger les documents après avoir défini le véhicule
    }

    // Setter pour le stage (appelé par VehiculeController)
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        // Initialiser la liste observable
        documentList = FXCollections.observableArrayList();

        // Configurer les colonnes avec des CellValueFactory personnalisés
        documentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getDocumentName(cellData.getValue())));
        echeanceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getEcheanceValue(cellData.getValue())));

        // Lier la liste à la TableView
        documentVehiculeTable.setItems(documentList);

        // Gestion du bouton de fermeture
        closeButton.setOnAction(event -> close());
    }

    // Méthode pour fermer la fenêtre
    @FXML
    private void close() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Méthode pour charger les documents réels depuis la base de données
    private void loadDocuments() {
        if (vehicule == null) {
            return;
        }

        // Récupérer les documents associés au véhicule via un service
        List<DocumentVehicule> documents = VehiculeService.getDocumentsForVehicule(vehicule.getIdVehicule());
        documentList.clear();
        if (documents != null && !documents.isEmpty()) {
            documentList.addAll(documents);
        } else {
            System.out.println("Aucun document trouvé pour le véhicule ID: " + vehicule.getIdVehicule());
            loadSampleData(); // Charger des données d'exemple si aucune donnée réelle
        }
    }

    // Méthode pour charger des données d'exemple (en secours ou pour tests)
    private void loadSampleData() {
        documentList.clear();
        DocumentVehicule vignette = new DocumentVehicule();
        vignette.setIdTypeDocument(1); // Vignette
        vignette.setDateEcheance(LocalDate.of(2025, 12, 31));
        vignette.setKilometrageEcheance(0);
        vignette.setIdVehicule(vehicule != null ? vehicule.getIdVehicule() : 0);

        DocumentVehicule visiteTechnique = new DocumentVehicule();
        visiteTechnique.setIdTypeDocument(2); // Visite technique
        visiteTechnique.setDateEcheance(LocalDate.of(2025, 6, 15));
        visiteTechnique.setKilometrageEcheance(0);
        visiteTechnique.setIdVehicule(vehicule != null ? vehicule.getIdVehicule() : 0);

        DocumentVehicule assurance = new DocumentVehicule();
        assurance.setIdTypeDocument(3); // Assurance
        assurance.setDateEcheance(LocalDate.of(2025, 9, 1));
        assurance.setKilometrageEcheance(0);
        assurance.setIdVehicule(vehicule != null ? vehicule.getIdVehicule() : 0);

        DocumentVehicule vidange = new DocumentVehicule();
        vidange.setIdTypeDocument(4); // Vidange
        vidange.setDateEcheance(null);
        vidange.setKilometrageEcheance(150000);
        vidange.setIdVehicule(vehicule != null ? vehicule.getIdVehicule() : 0);

        documentList.addAll(vignette, visiteTechnique, assurance, vidange);
    }

    // Méthode utilitaire pour obtenir le nom du document
    public String getDocumentName(DocumentVehicule doc) {
        switch (doc.getIdTypeDocument()) {
            case 1: return "Vignette";
            case 2: return "Visite Technique";
            case 3: return "Assurance";
            case 4: return "Vidange";
            default: return "Inconnu";
        }
    }

    // Méthode utilitaire pour obtenir la valeur d'échéance
    public String getEcheanceValue(DocumentVehicule doc) {
        if (doc.getDateEcheance() != null) {
            return doc.getDateEcheance().toString(); // Format date
        } else if (doc.getKilometrageEcheance() > 0) {
            return doc.getKilometrageEcheance() + " km"; // Format kilométrage
        } else {
            return "N/A";
        }
    }


}