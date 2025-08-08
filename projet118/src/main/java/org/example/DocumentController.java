package org.example;

import Persistance.models.Candidat;
import Persistance.models.CandidatDocument;
import Service.CandidatDocumentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showSuccessAlert;

public class DocumentController {
    @FXML private ListView<CandidatDocument> documentList;
    @FXML private TextField docTypeField;
    @FXML private TextField docDescField;
    @FXML private Button uploadDocButton;
    @FXML private Button viewDocButton;
    @FXML private Button deleteDocButton;
    @FXML private Button closeButton;

    private Candidat candidat;
    private ObservableList<CandidatDocument> documentsList;

    @FXML
    public void initialize() {
        setupButtonActions();
    }

    private void setupButtonActions() {
        uploadDocButton.setOnAction(event -> handleUploadDocument());
        viewDocButton.setOnAction(event -> handleViewDocument());
        deleteDocButton.setOnAction(event -> handleDeleteDocument());
        closeButton.setOnAction(event -> close());
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
        loadDocuments();
    }

    private void loadDocuments() {
        documentsList = FXCollections.observableArrayList(CandidatDocumentService.findByCandidatCin(candidat.getCin()));
        documentList.setItems(documentsList);
        documentList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CandidatDocument item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (%s)",
                            item.getDocument_type(),
                            item.getDescription() != null ? item.getDescription() : "No description",
                            item.getUpload_date() != null ? item.getUpload_date().toString() : "No date"));
                }
            }
        });
    }

    private void handleUploadDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images and PDFs", "*.png", "*.jpg", "*.jpeg", "*.pdf")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                String documentType = docTypeField.getText();
                String description = docDescField.getText();

                if (documentType.isEmpty()) {
                    showErrorAlert("Erreur", "Veuillez remplir le type de document.");
                    return;
                }

                byte[] fileContent = readFileToByteArray(file);

                String fileExtension = detectFileType(fileContent);
                if (fileExtension == null) {
                    showErrorAlert("Erreur", "Seuls les fichiers JPG, PNG et PDF sont supportés.");
                    return;
                }

                CandidatDocument document = new CandidatDocument(
                        documentType,
                        description,
                        fileContent,
                        candidat.getCin()
                );

                CandidatDocumentService.save(document);
                loadDocuments();
                clearFields();
                showSuccessAlert("Succès", "Document ajouté avec succès.");
            } catch (IOException e) {
                showErrorAlert("Erreur", "Échec de la lecture du fichier : " + e.getMessage());
            }
        }
    }

    private void handleViewDocument() {
        CandidatDocument selectedDoc = documentList.getSelectionModel().getSelectedItem();
        if (selectedDoc != null) {
            try {
                byte[] fileContent = selectedDoc.getFile_content();

                String fileExtension = detectFileType(fileContent);
                if (fileExtension == null) {
                    showErrorAlert("Erreur", "Le document sélectionné n'est pas dans un format supporté (JPG, PNG, PDF).");
                    return;
                }

                Path tempFilePath = Files.createTempFile("document_", "." + fileExtension);
                Files.write(tempFilePath, fileContent);

                if (tempFilePath.toFile().exists()) {
                    java.awt.Desktop.getDesktop().open(tempFilePath.toFile());
                    tempFilePath.toFile().deleteOnExit();
                } else {
                    showErrorAlert("Erreur", "Le fichier du document n'a pas pu être créé.");
                }
            } catch (Exception e) {
                showErrorAlert("Erreur", "Échec de l'ouverture du document : " + e.getMessage());
            }
        } else {
            showErrorAlert("Attention", "Veuillez sélectionner un document à visualiser.");
        }
    }

    private void handleDeleteDocument() {
        CandidatDocument selectedDoc = documentList.getSelectionModel().getSelectedItem();
        if (selectedDoc != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le document");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer ce document ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    CandidatDocumentService.deleteDocument(selectedDoc.getId());
                    loadDocuments();
                    clearFields();
                    showSuccessAlert("Succès", "Document supprimé avec succès.");
                }
            });
        } else {
            showErrorAlert("Attention", "Veuillez sélectionner un document à supprimer.");
        }
    }

    private void clearFields() {
        docTypeField.clear();
        docDescField.clear();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private byte[] readFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileContent = new byte[(int) file.length()];
            fis.read(fileContent);
            return fileContent;
        }
    }

    private String detectFileType(byte[] content) {
        if (content == null || content.length < 4) {
            return null;
        }

        if (content[0] == (byte) 0xFF && content[1] == (byte) 0xD8) {
            return "jpg";
        } else if (content[0] == (byte) 0x89 && content[1] == (byte) 0x50 &&
                content[2] == (byte) 0x4E && content[3] == (byte) 0x47) {
            return "png";
        } else if (content[0] == (byte) 0x25 && content[1] == (byte) 0x50 &&
                content[2] == (byte) 0x44 && content[3] == (byte) 0x46) {
            return "pdf";
        }

        return null;
    }
}