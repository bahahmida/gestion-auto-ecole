package org.example;

import Persistance.models.PasserExamen;
import Service.PasserExamenService;
import Service.CandidatService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.Modality;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import static Persistance.utils.Alert.*;

public class PasserExamenController {
    private static MenuController menuController;
    private ObservableList<PasserExamen> allExams;
    private FilteredList<PasserExamen> filteredExams;

    @FXML private TextField cinField;
    @FXML private ComboBox<String> examTypeCombo;
    @FXML private DatePicker examDate;
    @FXML private ComboBox<LocalTime> examTime;
    @FXML private Button nextButton;
    @FXML private StackPane formContainer;

    @FXML private Label cinErrorLabel;
    @FXML private Label examTypeErrorLabel;
    @FXML private Label examDateErrorLabel;

    @FXML private TextField headerSearchField;
    @FXML private HBox searchBox;
    @FXML private TableView<PasserExamen> examsTable;
    @FXML private TableColumn<PasserExamen, ImageView> iconColumn;
    @FXML private TableColumn<PasserExamen, Integer> idColumn;
    @FXML private TableColumn<PasserExamen, String> typeColumn;
    @FXML private TableColumn<PasserExamen, LocalDateTime> dateColumn;
    @FXML private TableColumn<PasserExamen, Integer> candidatCinColumn;
    @FXML private TableColumn<PasserExamen, Float> prixColumn;
    @FXML private TableColumn<PasserExamen, String> resultatColumn;
    @FXML private TableColumn<PasserExamen, PasserExamen> editColumn;
    @FXML private TableColumn<PasserExamen, PasserExamen> deleteColumn;

    @FXML private ToggleButton filterCodeButton;
    @FXML private ToggleButton filterTypeAButton;
    @FXML private ToggleButton filterTypeBButton;
    @FXML private ToggleButton filterTypeCButton;
    @FXML private ToggleButton filterPendingButton;
    @FXML private ToggleButton filterFailedButton;
    @FXML private ToggleButton filterPassedButton;
    @FXML private ToggleButton filterThisWeekButton;
    @FXML private ToggleButton resetFilterButton;

    @FXML private TabPane mainTabPane;

    public static MenuController getMenuController() {
        return menuController;
    }

    @FXML
    public void initialize() {
        examTypeCombo.getStyleClass().add("form-input");
        examDate.getStyleClass().add("form-datepicker");
        examTime.getStyleClass().add("form-input");
        cinField.getStyleClass().add("form-input");
        nextButton.getStyleClass().add("primary-button");

        examTypeCombo.setItems(FXCollections.observableArrayList("Code", "Conduite"));
        examTime.setItems(FXCollections.observableArrayList(
                LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30),
                LocalTime.of(12, 0), LocalTime.of(12, 30)
        ));

        cinField.textProperty().addListener((obs, old, newVal) -> validateCIN());
        examTypeCombo.valueProperty().addListener((obs, old, newVal) -> validateExamType());
        examDate.valueProperty().addListener((obs, old, newVal) -> validateDateAndTime());
        examTime.valueProperty().addListener((obs, old, newVal) -> validateDateAndTime());

        setupIconColumn();
        configureDataColumns();
        setupEditDeleteColumns();
        setupFilterButtons();
        loadExams();

        searchBox.setVisible(false);

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                searchBox.setVisible(newTab.getText().equals("Liste des Examens"));
                if (!newTab.getText().equals("Liste des Examens")) {
                    headerSearchField.clear();
                    filteredExams.setPredicate(examen -> true);
                    applyCurrentFilter();
                }
            }
        });
    }

    private void validateCIN() {
        String cin = cinField.getText().trim();
        boolean isValidFormat = cin.matches("\\d{8}");
        boolean exists = false;

        if (!isValidFormat) {
            cinErrorLabel.setText(cin.isEmpty() ? "Le CIN est requis" : "Le CIN doit contenir 8 chiffres");
            cinErrorLabel.setVisible(true);
            cinErrorLabel.setManaged(true);
            cinField.setStyle("-fx-border-color: red;");
            return;
        }

        try {
            int cinValue = Integer.parseInt(cin);
            exists = CandidatService.existCandidat(cinValue);
            if (!exists) {
                cinErrorLabel.setText("Aucun candidat trouvé avec ce CIN");
                cinErrorLabel.setVisible(true);
                cinErrorLabel.setManaged(true);
                cinField.setStyle("-fx-border-color: red;");
            } else {
                cinErrorLabel.setText("");
                cinErrorLabel.setVisible(false);
                cinErrorLabel.setManaged(false);
                cinField.setStyle("");
            }
        } catch (SQLException e) {
            cinErrorLabel.setText("Erreur lors de la vérification du CIN : " + e.getMessage());
            cinErrorLabel.setVisible(true);
            cinErrorLabel.setManaged(true);
            cinField.setStyle("-fx-border-color: red;");
        } catch (NumberFormatException e) {
            cinErrorLabel.setText("Le CIN doit être un nombre valide");
            cinErrorLabel.setVisible(true);
            cinErrorLabel.setManaged(true);
            cinField.setStyle("-fx-border-color: red;");
        }
    }

    private void validateExamType() {
        boolean isValid = examTypeCombo.getValue() != null;
        examTypeErrorLabel.setText(isValid ? "" : "Le type d'examen est requis");
        examTypeErrorLabel.setVisible(!isValid);
        examTypeErrorLabel.setManaged(isValid);
        examTypeCombo.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void validateDateAndTime() {
        LocalDate date = examDate.getValue();
        LocalTime time = examTime.getValue();
        boolean dateIsValid = date != null && !date.isBefore(LocalDate.now());
        boolean timeIsValid = time != null;

        if (!dateIsValid || !timeIsValid) {
            examDateErrorLabel.setVisible(true);
            examDateErrorLabel.setManaged(true);
            if (date == null && time == null) {
                examDateErrorLabel.setText("La date et l'heure sont requises");
                examDate.setStyle("-fx-border-color: red;");
                examTime.setStyle("-fx-border-color: red;");
            } else if (date == null) {
                examDateErrorLabel.setText("La date est requise");
                examDate.setStyle("-fx-border-color: red;");
                examTime.setStyle("");
            } else if (!dateIsValid) {
                examDateErrorLabel.setText("La date ne peut pas être antérieure à aujourd'hui");
                examDate.setStyle("-fx-border-color: red;");
                examTime.setStyle(timeIsValid ? "" : "-fx-border-color: red;");
            } else {
                examDateErrorLabel.setText("L'heure est requise");
                examDate.setStyle("");
                examTime.setStyle("-fx-border-color: red;");
            }
        } else {
            examDateErrorLabel.setText("");
            examDateErrorLabel.setVisible(false);
            examDateErrorLabel.setManaged(false);
            examDate.setStyle("");
            examTime.setStyle("");
        }
    }

    @FXML
    private void handleNext() {
        validateCIN();
        validateExamType();
        validateDateAndTime();

        if (cinErrorLabel.getText().isEmpty() &&
                examTypeErrorLabel.getText().isEmpty() &&
                examDateErrorLabel.getText().isEmpty()) {
            if ("Code".equals(examTypeCombo.getValue())) {
                handleCodeExam();
            } else {
                handleConduiteExam();
            }
        } else {
            showErrorAlert("Erreur", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    private void handleCodeExam() {
        try {
            int cinCondidat = Integer.parseInt(cinField.getText());
            int idExamen = 1; // Examen de code
            LocalDateTime dateHeureExamen = LocalDateTime.of(examDate.getValue(), examTime.getValue());

            if (PasserExamenService.examenExists(cinCondidat)) {
                showErrorAlert("Erreur", "Un examen avec une date non dépassée existe déjà pour ce candidat. Il doit d'abord le passer.");
                return;
            } else if (!PasserExamenService.tousExamensTermines(cinCondidat)) {
                showErrorAlert("Erreur", "Ce candidat a encore des examens en attente. Ils doivent être terminés avant d'enregistrer un nouvel examen.");
                return;
            } else if (PasserExamenService.examenReussiExists(cinCondidat, idExamen)) {
                showErrorAlert("Erreur", "Ce candidat a déjà réussi l'examen. Il ne peut pas être inscrit à nouveau.");
                return;
            }

            float prixExamen = PasserExamenService.getExamenInfoById(1).getPrix();
            PasserExamen exam = new PasserExamen(
                    cinCondidat,
                    idExamen,
                    dateHeureExamen,
                    "En attente",
                    PasserExamenService.getExamenInfoById(1).getNomExamen(),
                    prixExamen
            );

            PasserExamenService.save(exam);
            showSuccessAlert("Succès", "Inscription à l'examen de code enregistrée !");
            clearForm();
            loadExams();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de la vérification ou de l'enregistrement : " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur inattendue : " + e.getMessage());
        }
    }

    private void handleConduiteExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PasserExamenConduite.fxml"));
            Parent form = loader.load();

            PasserExamenConduiteController controller = loader.getController();
            controller.initData(
                    Integer.parseInt(cinField.getText()),
                    examDate.getValue(),
                    examTime.getValue()
            );

            formContainer.setPrefHeight(500);
            formContainer.setMinHeight(500);
            formContainer.getChildren().setAll(form);
        } catch (IOException e) {
            showErrorAlert("Erreur", "Problème de chargement : " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur inattendue : " + e.getMessage());
        }
    }

    private void clearForm() {
        cinField.clear();
        examTypeCombo.getSelectionModel().clearSelection();
        examDate.setValue(null);
        examTime.getSelectionModel().clearSelection();

        cinErrorLabel.setText("");
        cinErrorLabel.setVisible(false);
        cinErrorLabel.setManaged(false);
        examTypeErrorLabel.setText("");
        examTypeErrorLabel.setVisible(false);
        examTypeErrorLabel.setManaged(false);
        examDateErrorLabel.setText("");
        examDateErrorLabel.setVisible(false);
        examDateErrorLabel.setManaged(false);

        cinField.setStyle("");
        examTypeCombo.setStyle("");
        examDate.setStyle("");
        examTime.setStyle("");
    }

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    private void setupIconColumn() {
        iconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/exam.jpg")));
            {
                imageView.setFitHeight(35);
                imageView.setFitWidth(35);
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void configureDataColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdExamen()).asObject()
        );

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNomExamen())
        );

        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateExamen())
        );
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });

        candidatCinColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCinCondidat()).asObject()
        );

        prixColumn.setCellValueFactory(cellData ->
                new SimpleFloatProperty(cellData.getValue().getPrix()).asObject()
        );

        resultatColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getResultatExamen())
        );
    }

    private void setupEditDeleteColumns() {
        editColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton("/images/edit.png");
            {
                editButton.setOnAction(event -> {
                    PasserExamen examen = getTableView().getItems().get(getIndex());
                    handleUpdateExamen(examen);
                });
            }

            @Override
            protected void updateItem(PasserExamen examen, boolean empty) {
                super.updateItem(examen, empty);
                setGraphic(empty ? null : editButton);
                setAlignment(Pos.CENTER);
            }
        });

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = createIconButton("/images/delete.png");
            {
                deleteButton.setOnAction(event -> {
                    PasserExamen examen = getTableView().getItems().get(getIndex());
                    handleDeleteExamen(examen);
                });
            }

            @Override
            protected void updateItem(PasserExamen examen, boolean empty) {
                super.updateItem(examen, empty);
                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private Button createIconButton(String imagePath) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        icon.setFitHeight(28);
        icon.setFitWidth(28);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private void loadExams() {
        try {
            allExams = FXCollections.observableArrayList(PasserExamenService.findAll());
            filteredExams = new FilteredList<>(allExams);
            examsTable.setItems(filteredExams);
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les examens : " + e.getMessage());
        }
    }

    @FXML
    private void openAddExamForm() {
        menuController.loadView("PasserExamenConduite.fxml");
    }



    private void handleUpdateExamen(PasserExamen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateExamen.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Examen");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(examsTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UpdateExamenController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setExamen(examen);

            dialogStage.showAndWait();
            loadExams();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleDeleteExamen(PasserExamen examen) {
        showConfirmationAlert("Confirmation de suppression", "Êtes-vous sûr de vouloir supprimer cet examen ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            PasserExamenService.delete(examen.getCinCondidat(), examen.getIdExamen(), examen.getDateExamen());
                            loadExams();
                            showSuccessAlert("Succès", "Examen supprimé avec succès !");
                        } catch (SQLException e) {
                            showErrorAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                        }
                    }
                });
    }

    @FXML
    private void filterByCode() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByCode());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Code' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByTypeA() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByTypeA());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Type A' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByTypeB() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByTypeB());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Type B' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByTypeC() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByTypeC());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Type C' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByPending() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByPending());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'En attente' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByFailed() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByFailed());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Échoué' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByPassed() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByPassed());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens 'Réussi' : " + e.getMessage());
        }
    }

    @FXML
    private void filterByThisWeek() {
        try {
            ObservableList<PasserExamen> filteredList = FXCollections.observableArrayList(PasserExamenService.filterByThisWeek());
            examsTable.setItems(filteredList);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage des examens de cette semaine : " + e.getMessage());
        }
    }

    @FXML
    private void resetFilters() {
        examsTable.setItems(allExams);
    }

    private void setupFilterButtons() {
        String defaultStyle = "-fx-background-color: linear-gradient(to bottom, rgba(255, 255, 255, 0.95), rgba(245, 248, 252, 0.95));" +
                "-fx-text-fill: #1e5b9c;" +
                "-fx-border-color: rgba(30, 91, 156, 0.6);" +
                "-fx-border-width: 1.2px;" +
                "-fx-border-radius: 22px;" +
                "-fx-background-radius: 22px;" +
                "-fx-padding: 12px 22px;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: 500;" +
                "-fx-font-size: 14px;" +
                "-fx-min-width: 110px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);" +
                "-fx-transition: background-color 0.25s, text-fill 0.25s, effect 0.25s;";
        String selectedStyle = "-fx-text-fill: white;" +
                "-fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                "-fx-border-color: #1e5b9c;" +
                "-fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        filterCodeButton.setStyle(defaultStyle);
        filterTypeAButton.setStyle(defaultStyle);
        filterTypeBButton.setStyle(defaultStyle);
        filterTypeCButton.setStyle(defaultStyle);
        filterPendingButton.setStyle(defaultStyle);
        filterFailedButton.setStyle(defaultStyle);
        filterPassedButton.setStyle(defaultStyle);
        filterThisWeekButton.setStyle(defaultStyle);
        resetFilterButton.setStyle(selectedStyle);

        filterCodeButton.setOnAction(event -> {
            filterByCode();
            updateButtonStyles(filterCodeButton);
        });

        filterTypeAButton.setOnAction(event -> {
            filterByTypeA();
            updateButtonStyles(filterTypeAButton);
        });

        filterTypeBButton.setOnAction(event -> {
            filterByTypeB();
            updateButtonStyles(filterTypeBButton);
        });

        filterTypeCButton.setOnAction(event -> {
            filterByTypeC();
            updateButtonStyles(filterTypeCButton);
        });

        filterPendingButton.setOnAction(event -> {
            filterByPending();
            updateButtonStyles(filterPendingButton);
        });

        filterFailedButton.setOnAction(event -> {
            filterByFailed();
            updateButtonStyles(filterFailedButton);
        });

        filterPassedButton.setOnAction(event -> {
            filterByPassed();
            updateButtonStyles(filterPassedButton);
        });

        filterThisWeekButton.setOnAction(event -> {
            filterByThisWeek();
            updateButtonStyles(filterThisWeekButton);
        });

        resetFilterButton.setOnAction(event -> {
            resetFilters();
            updateButtonStyles(resetFilterButton);
        });
    }

    private void updateButtonStyles(ToggleButton selectedButton) {
        String defaultStyle = "-fx-background-color: linear-gradient(to bottom, rgba(255, 255, 255, 0.95), rgba(245, 248, 252, 0.95));" +
                "-fx-text-fill: #1e5b9c;" +
                "-fx-border-color: rgba(30, 91, 156, 0.6);" +
                "-fx-border-width: 1.2px;" +
                "-fx-border-radius: 22px;" +
                "-fx-background-radius: 22px;" +
                "-fx-padding: 12px 22px;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: 500;" +
                "-fx-font-size: 14px;" +
                "-fx-min-width: 110px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);" +
                "-fx-transition: background-color 0.25s, text-fill 0.25s, effect 0.25s;";
        String selectedStyle = "-fx-text-fill: white;" +
                "-fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                "-fx-border-color: #1e5b9c;" +
                "-fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        filterCodeButton.setStyle(defaultStyle);
        filterTypeAButton.setStyle(defaultStyle);
        filterTypeBButton.setStyle(defaultStyle);
        filterTypeCButton.setStyle(defaultStyle);
        filterPendingButton.setStyle(defaultStyle);
        filterFailedButton.setStyle(defaultStyle);
        filterPassedButton.setStyle(defaultStyle);
        filterThisWeekButton.setStyle(defaultStyle);
        resetFilterButton.setStyle(defaultStyle);

        selectedButton.setStyle(selectedStyle);
    }

    private void applyCurrentFilter() {
        if (filterCodeButton.isSelected()) {
            filterByCode();
        } else if (filterTypeAButton.isSelected()) {
            filterByTypeA();
        } else if (filterTypeBButton.isSelected()) {
            filterByTypeB();
        } else if (filterTypeCButton.isSelected()) {
            filterByTypeC();
        } else if (filterPendingButton.isSelected()) {
            filterByPending();
        } else if (filterFailedButton.isSelected()) {
            filterByFailed();
        } else if (filterPassedButton.isSelected()) {
            filterByPassed();
        } else if (filterThisWeekButton.isSelected()) {
            filterByThisWeek();
        } else {
            resetFilters();
        }
    }

    @FXML
    private void handleHeaderSearch() {
        String searchText = headerSearchField.getText().trim();

        if (searchText.isEmpty()) {
            filteredExams.setPredicate(examen -> true);
            applyCurrentFilter();
            return;
        }

        try {
            int searchCin = Integer.parseInt(searchText);
            filteredExams.setPredicate(examen -> examen.getCinCondidat() == searchCin);
            if (filteredExams.isEmpty()) {
                showInformationAlert("Information", "Aucun examen trouvé avec le CIN du candidat : " + searchCin);
            }
        } catch (NumberFormatException e) {
            showInformationAlert("Information", "Veuillez entrer un numéro CIN valide.");
            headerSearchField.setText("");
        }
    }



}