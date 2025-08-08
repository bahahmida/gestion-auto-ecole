package org.example;

import Persistance.models.DocumentVehicule;
import Persistance.models.MaintenanceVehicule;
import Persistance.models.NotificationVehicule;
import Persistance.models.Vehicule;
import Persistance.utils.Alert;
import Service.MaintenanceVehiculeService;
import Service.NotificationService;
import Service.VehiculeService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class VehiculeController {
    private MenuController menuController;
    private ObservableList<Vehicule> allVehicules;
    private FilteredList<Vehicule> filteredVehicules;
    private ObservableList<MaintenanceVehicule> allMaintenances;
    private VehiculeService vehiculeService;

    @FXML private TextField searchField;
    @FXML private Button addVehicleButton;
    @FXML private ToggleButton typeAFilter;
    @FXML private ToggleButton typeBFilter;
    @FXML private ToggleButton typeCFilter;
    @FXML private ToggleButton allFilter;
    @FXML private ImageView notificationIcon;
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, ImageView> iconColumn;
    @FXML private TableColumn<Vehicule, String> immatriculationColumn;
    @FXML private TableColumn<Vehicule, String> marqueColumn;
    @FXML private TableColumn<Vehicule, String> modeleColumn;
    @FXML private TableColumn<Vehicule, Integer> anneeFabricationColumn;
    @FXML private TableColumn<Vehicule, Integer> kmActuelColumn;
    @FXML private TableColumn<Vehicule, String> categorieColumn;
    @FXML private TableColumn<Vehicule, String> documentColumn;
    @FXML private TableColumn<Vehicule, String> historiqueColumn;
    @FXML private TableColumn<Vehicule, Vehicule> actionColumn;

    @FXML private TableView<MaintenanceVehicule> entretienTable;
    @FXML private TableColumn<MaintenanceVehicule, ImageView> iconMaintenanceColumn;
    @FXML private TableColumn<MaintenanceVehicule, Integer> idEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, String> idVehiculeColumn;
    @FXML private TableColumn<MaintenanceVehicule, String> typeEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, LocalDate> dateEntretienColumn;
    @FXML private TableColumn<MaintenanceVehicule, LocalDate> dateEntretienColumn1;
    @FXML private TableColumn<MaintenanceVehicule, Double> coutColumn;
    @FXML private TableColumn<MaintenanceVehicule, Blob> factureColumn;
    @FXML private TableColumn<MaintenanceVehicule, MaintenanceVehicule> actionMaintenanceColumn;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    public void insertNotificationsForDueDates() {
        try {
            List<DocumentVehicule> documents = VehiculeService.getAllDocuments();
            List<NotificationVehicule> existingNotifications = NotificationService.getNotifications();

            final int ASSURANCE_TYPE = 3;
            final int VIGNETTE_TYPE = 1;
            final int VISITETECHNIQUE_TYPE = 2;
            final int VIDANGE_TYPE = 4;

            LocalDate today = LocalDate.now();

            for (DocumentVehicule document : documents) {
                int idVehicule = document.getIdVehicule();
                int idTypeDocument = document.getIdTypeDocument();
                String type = "";
                String message = "";

                if (idTypeDocument == ASSURANCE_TYPE || idTypeDocument == VIGNETTE_TYPE || idTypeDocument == VISITETECHNIQUE_TYPE) {
                    LocalDate dateEcheance = document.getDateEcheance();
                    if (dateEcheance == null) {
                        continue;
                    }
                    long daysUntilDue = ChronoUnit.DAYS.between(today, dateEcheance);
                    if (today.isAfter(dateEcheance)) {
                        switch (idTypeDocument) {
                            case ASSURANCE_TYPE:
                                type = "assurance";
                                message = "Assurance expirée";
                                break;
                            case VIGNETTE_TYPE:
                                type = "vignette";
                                message = "Vignette expirée";
                                break;
                            case VISITETECHNIQUE_TYPE:
                                type = "visite_technique";
                                message = "Visite technique expirée";
                                break;
                            default:
                                continue;
                        }
                        String finalType = type;
                        boolean notificationExists = existingNotifications.stream()
                                .anyMatch(n -> n.getIdVehicule() == idVehicule &&
                                        n.getType().equals(finalType));

                        if (!notificationExists) {
                            NotificationVehicule notification = new NotificationVehicule();
                            notification.setIdVehicule(idVehicule);
                            notification.setType(type);
                            notification.setMessage(message);
                            notification.setDateNotification(LocalDate.now());
                            notification.setEtat(false);

                            NotificationService.save(notification);
                            System.out.println("Inserted notification for vehicle ID " + idVehicule + ": " + message);
                        }
                    }

                    if (daysUntilDue <= 7 && daysUntilDue >= 0) {
                        switch (idTypeDocument) {
                            case ASSURANCE_TYPE:
                                type = "assurance";
                                message = "Assurance expire bientôt";
                                break;
                            case VIGNETTE_TYPE:
                                type = "vignette";
                                message = "Vignette expire bientôt";
                                break;
                            case VISITETECHNIQUE_TYPE:
                                type = "visite_technique";
                                message = "Visite technique expire bientôt";
                                break;
                            default:
                                continue;
                        }

                        String finalType = type;
                        boolean notificationExists = existingNotifications.stream()
                                .anyMatch(n -> n.getIdVehicule() == idVehicule &&
                                        n.getType().equals(finalType));

                        if (!notificationExists) {
                            NotificationVehicule notification = new NotificationVehicule();
                            notification.setIdVehicule(idVehicule);
                            notification.setType(type);
                            notification.setMessage(message);
                            notification.setDateNotification(LocalDate.now());
                            notification.setEtat(false);

                            NotificationService.save(notification);
                            System.out.println("Inserted notification for vehicle ID " + idVehicule + ": " + message);
                        }
                    }
                } else if (idTypeDocument == VIDANGE_TYPE) {
                    Integer kilometrageEcheance = document.getKilometrageEcheance();
                    if (kilometrageEcheance == null) {
                        continue;
                    }

                    Vehicule vehicule = VehiculeService.getVehicule(idVehicule);
                    if (vehicule == null) {
                        continue;
                    }

                    int kilometrageActuel = vehicule.getKmActuel();
                    if (kilometrageActuel >= kilometrageEcheance) {
                        type = "vidange";
                        message = "Vidange nécessaire";

                        String finalType1 = type;
                        boolean notificationExists = existingNotifications.stream()
                                .anyMatch(n -> n.getIdVehicule() == idVehicule &&
                                        n.getType().equals(finalType1));

                        if (!notificationExists) {
                            NotificationVehicule notification = new NotificationVehicule();
                            notification.setIdVehicule(idVehicule);
                            notification.setType(type);
                            notification.setMessage(message);
                            notification.setDateNotification(LocalDate.now());
                            notification.setEtat(false);

                            NotificationService.save(notification);
                            System.out.println("Inserted notification for vehicle ID " + idVehicule + ": " + message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de l'insertion des notifications : " + e.getMessage());
        }
    }

    public void updateNotifications() {
        if (menuController != null) {
            try {
                updateNotificationIcon();
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour des notifications : " + e.getMessage());
            }
        }
    }

    @FXML
    private void initialize() {
        vehiculeService = new VehiculeService();
        VehiculeService.UpdateDateVinietteForAllVehicule();
        setupIconColumn();
        configureDataColumns();
        setupDocumentColumn();
        setupHistoriqueColumn();
        setupActionColumn();
        configureMaintenanceColumns();
        loadMaintenances();
        setupFilters();
        loadVehicules();
        setupSearchListener();
        updateNotificationIcon();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.trim().toLowerCase();
            try {
                if (searchText.isEmpty()) {
                    filteredVehicules.setPredicate(vehicule -> true);
                } else {
                    filteredVehicules.setPredicate(vehicule ->
                            (vehicule.getMarque() != null && vehicule.getMarque().toLowerCase().contains(searchText)) ||
                                    (vehicule.getModele() != null && vehicule.getModele().toLowerCase().contains(searchText))
                    );
                }
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Échec de la recherche des véhicules : " + e.getMessage());
            }
        });
    }

    private void setupFilters() {
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
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);";
        String selectedStyle = "-fx-text-fill: white;" +
                "-fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                "-fx-border-color: #1e5b9c;" +
                "-fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        typeAFilter.setStyle(defaultStyle);
        typeBFilter.setStyle(defaultStyle);
        typeCFilter.setStyle(defaultStyle);
        allFilter.setStyle(selectedStyle);

        typeAFilter.setOnAction(event -> {
            try {
                allVehicules.setAll(vehiculeService.filterVehicules("A"));
                filteredVehicules.setPredicate(vehicule -> true);
                updateButtonStyles(typeAFilter);
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Échec du filtrage des véhicules : " + e.getMessage());
            }
        });

        typeBFilter.setOnAction(event -> {
            try {
                allVehicules.setAll(vehiculeService.filterVehicules("B"));
                filteredVehicules.setPredicate(vehicule -> true);
                updateButtonStyles(typeBFilter);
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Échec du filtrage des véhicules : " + e.getMessage());
            }
        });

        typeCFilter.setOnAction(event -> {
            try {
                allVehicules.setAll(vehiculeService.filterVehicules("C"));
                filteredVehicules.setPredicate(vehicule -> true);
                updateButtonStyles(typeCFilter);
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Échec du filtrage des véhicules : " + e.getMessage());
            }
        });

        allFilter.setOnAction(event -> {
            try {
                allVehicules.setAll(vehiculeService.filterVehicules("ALL"));
                filteredVehicules.setPredicate(vehicule -> true);
                updateButtonStyles(allFilter);
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Échec du filtrage des véhicules : " + e.getMessage());
            }
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
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0.1, 0, 1);";
        String selectedStyle = "-fx-text-fill: white;" +
                "-fx-background-color: linear-gradient(to left, #1e5b9c, #134673);" +
                "-fx-border-color: #1e5b9c;" +
                "-fx-effect: dropshadow(gaussian, rgba(30, 91, 156, 0.25), 4, 0.1, 0, 1);";

        typeAFilter.setStyle(defaultStyle);
        typeBFilter.setStyle(defaultStyle);
        typeCFilter.setStyle(defaultStyle);
        allFilter.setStyle(defaultStyle);
        selectedButton.setStyle(selectedStyle);
    }

    private void loadVehicules() {
        List<Vehicule> vehiculeList = vehiculeService.getVehicules();
        allVehicules = vehiculeList != null ? FXCollections.observableArrayList(vehiculeList) : FXCollections.observableArrayList();
        filteredVehicules = new FilteredList<>(allVehicules, vehicule -> true);
        vehiculesTable.setItems(filteredVehicules);
        allFilter.fire(); // Appliquer le filtre "ALL" par défaut
    }

    private void setupIconColumn() {
        iconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView;

            {
                InputStream imageStream = getClass().getResourceAsStream("/images/vehicule.jpg");
                imageView = imageStream != null ? new ImageView(new Image(imageStream)) : new ImageView();
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
        immatriculationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getImmatriculation()));
        marqueColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMarque()));
        modeleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getModele()));
        anneeFabricationColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAnneeFabrication()).asObject());
        kmActuelColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getKmActuel()).asObject());

        categorieColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getCategorie())));
        categorieColumn.setCellFactory(column -> new TableCell<Vehicule, String>() {
            private final Circle circle = new Circle(12);
            private final Label label = new Label();
            private final StackPane stackPane = new StackPane(circle, label);
            private final LinearGradient blueGradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.web("#1e5b9c")),
                    new Stop(1, javafx.scene.paint.Color.web("#174e85"))
            );

            {
                circle.setFill(blueGradient);
                label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                stackPane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null || category.isEmpty()) {
                    setGraphic(null);
                } else {
                    label.setText(category.toUpperCase());
                    setGraphic(stackPane);
                }
            }
        });
    }

    private void setupDocumentColumn() {
        documentColumn.setCellFactory(column -> new TableCell<>() {
            private final Button documentButton = createIconButton("/images/document.png");

            {
                documentButton.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    openDocumentVehiculeForm(vehicule);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : documentButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupHistoriqueColumn() {
        historiqueColumn.setCellFactory(column -> new TableCell<>() {
            private final Button historiqueButton = createIconButton("/images/historique.jpg");

            {
                historiqueButton.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    openHistoriqueVehiculeMaintenance(vehicule);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : historiqueButton);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton("/images/edit.png");
            private final Button deleteButton = createIconButton("/images/delete.png");
            private final Button maintenanceButton = createIconButton("/images/ajouterMaintenance.jpg");
            private final HBox actionBox;

            {
                actionBox = new HBox(10, editButton, deleteButton, maintenanceButton);
                actionBox.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    openUpdateVehiculeForm(vehicule);
                });
                deleteButton.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    handleDeleteVehicule(vehicule);
                });
                maintenanceButton.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    openMaintenanceForm(vehicule);
                });
            }

            @Override
            protected void updateItem(Vehicule vehicule, boolean empty) {
                super.updateItem(vehicule, empty);
                setGraphic(empty ? null : actionBox);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void configureMaintenanceColumns() {
        iconMaintenanceColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView;

            {
                InputStream imageStream = getClass().getResourceAsStream("/images/maintenance.jpg");
                imageView = imageStream != null ? new ImageView(new Image(imageStream)) : new ImageView();
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

        idEntretienColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdMaintenance()).asObject());
        idVehiculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(VehiculeService.getImmatriculationById(cellData.getValue().getIdVehicule())));
        typeEntretienColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeMaintenance()));
        dateEntretienColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateDebut()));
        dateEntretienColumn1.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateFin()));
        coutColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCout()).asObject());

        factureColumn.setCellFactory(column -> new TableCell<>() {
            private final Button factureButton = createIconButton("/images/facture.jpg");

            {
                factureButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    openFactureFile(maintenance);
                });
            }

            @Override
            protected void updateItem(Blob item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                setText(null);

                if (empty) {
                    return;
                }

                MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                if (maintenance == null) {
                    return;
                }

                Blob facture = maintenance.getFacture();
                if (facture == null) {
                    setText("Aucune facture");
                    return;
                }

                setGraphic(factureButton);
                setAlignment(Pos.CENTER);
            }
        });

        actionMaintenanceColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton("/images/edit.png");
            private final Button deleteButton = createIconButton("/images/delete.png");
            private final HBox actionBox;

            {
                actionBox = new HBox(10, editButton, deleteButton);
                actionBox.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    openUpdateMaintenanceForm(maintenance);
                });

                deleteButton.setOnAction(event -> {
                    MaintenanceVehicule maintenance = getTableView().getItems().get(getIndex());
                    handleDeleteMaintenance(maintenance);
                });
            }

            @Override
            protected void updateItem(MaintenanceVehicule maintenance, boolean empty) {
                super.updateItem(maintenance, empty);
                setGraphic(empty ? null : actionBox);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void loadMaintenances() {
        try {
            List<MaintenanceVehicule> maintenanceList = MaintenanceVehiculeService.getCurrentMaintenances();
            allMaintenances = maintenanceList != null ? FXCollections.observableArrayList(maintenanceList) : FXCollections.observableArrayList();
            entretienTable.setItems(allMaintenances);
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement des maintenances : " + e.getMessage());
            allMaintenances = FXCollections.observableArrayList();
            entretienTable.setItems(allMaintenances);
        }
    }

    private Button createIconButton(String imagePath) {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        ImageView icon = imageStream != null ? new ImageView(new Image(imageStream)) : new ImageView();
        icon.setFitHeight(28);
        icon.setFitWidth(28);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private void updateNotificationIcon() {
        try {
            long unreadCount = NotificationService.getNotifications().stream()
                    .filter(notification -> !notification.isEtat())
                    .count();

            String iconPath = unreadCount > 0 ? "/images/notification+.png" : "/images/notification.png";
            InputStream imageStream = getClass().getResourceAsStream(iconPath);
            if (imageStream == null) {
                return;
            }
            Image image = new Image(imageStream);
            notificationIcon.setImage(image);
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour de l'icône de notification : " + e.getMessage());
        }
    }

    @FXML
    private void openNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/notification.fxml"));
            Parent root = loader.load();

            NotificationController controller = loader.getController();
            controller.setMenuController(menuController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Notifications Véhicules");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setDialogStage(stage);
            stage.showAndWait();

            updateNotificationIcon();
            if (menuController != null) {
                menuController.checkForNotifications();
            }
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de l'ouverture de l'interface des notifications : " + e.getMessage());
        }
    }

    private void openFactureFile(MaintenanceVehicule maintenance) {
        try {
            Blob facture = maintenance.getFacture();
            if (facture == null) {
                Alert.showInformationAlert("Aucune facture", "Cette maintenance n'a pas de facture associée.");
                return;
            }

            byte[] factureBytes = facture.getBytes(1, (int) facture.length());
            if (isPDF(factureBytes)) {
                File tempFile = File.createTempFile("facture_" + maintenance.getIdMaintenance(), ".pdf");
                Files.write(tempFile.toPath(), factureBytes);
                java.awt.Desktop.getDesktop().open(tempFile);
                tempFile.deleteOnExit();
            } else if (isImage(factureBytes)) {
                Image image = new Image(new ByteArrayInputStream(factureBytes));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(600);
                imageView.setFitHeight(400);
                imageView.setPreserveRatio(true);

                Stage stage = new Stage();
                stage.setTitle("Facture - Visualisation");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(new HBox(imageView)));
                stage.showAndWait();
            } else {
                Alert.showErrorAlert("Erreur", "Format de facture non supporté.");
            }
        } catch (SQLException | IOException e) {
            Alert.showErrorAlert("Erreur", "Impossible d'ouvrir la facture : " + e.getMessage());
        }
    }

    private boolean isPDF(byte[] bytes) {
        return bytes.length > 4 && bytes[0] == (byte)0x25 && bytes[1] == (byte)0x50 &&
                bytes[2] == (byte)0x44 && bytes[3] == (byte)0x46;
    }

    private boolean isImage(byte[] bytes) {
        boolean isJPEG = bytes.length > 2 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8;
        boolean isPNG = bytes.length > 4 && bytes[0] == (byte)0x89 && bytes[1] == (byte)0x50 &&
                bytes[2] == (byte)0x4E && bytes[3] == (byte)0x47;
        return isJPEG || isPNG;
    }

    private void openDocumentVehiculeForm(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/documentsVehicule.fxml"));
            Parent root = loader.load();
            DocumentVehiculeController controller = loader.getController();
            controller.setVehicule(vehicule);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Documents du Véhicule - " + vehicule.getImmatriculation());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setDialogStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement des documents : " + e.getMessage());
        }
    }

    private void openUpdateVehiculeForm(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateVehicule.fxml"));
            Parent root = loader.load();
            UpdateVehiculeController controller = loader.getController();
            controller.setVehicule(vehicule);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le Véhicule");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setDialogStage(stage);
            stage.showAndWait();

            if (controller.isOkClicked()) {
                loadVehicules();
            }
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement du formulaire de modification : " + e.getMessage());
        }
    }

    private void openMaintenanceForm(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addMaintenance.fxml"));
            Parent root = loader.load();
            AddMaintenanceController controller = loader.getController();
            controller.setVehicule(vehicule);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter à la Maintenance");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            loadMaintenances();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement du formulaire de maintenance : " + e.getMessage());
        }
    }

    private void openUpdateMaintenanceForm(MaintenanceVehicule maintenance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/updateMaintenanceActuelle.fxml"));
            Parent root = loader.load();
            UpdateMaintenanceActuelleController controller = loader.getController();
            controller.setMaintenance(maintenance);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier la Maintenance - ID " + maintenance.getIdMaintenance());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setDialogStage(stage);
            stage.showAndWait();

            if (controller.isOkClicked()) {
                loadMaintenances();
            }
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement du formulaire de modification de maintenance : " + e.getMessage());
        }
    }

    private void openHistoriqueVehiculeMaintenance(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/historiqueMaintenanceVehicule.fxml"));
            Parent root = loader.load();
            HistoriqueMaintenanceVehiculeController controller = loader.getController();
            controller.setVehicule(vehicule);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Historique Maintenance - " + vehicule.getImmatriculation());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setDialogStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors du chargement de l'historique : " + e.getMessage());
        }
    }

    private void handleDeleteVehicule(Vehicule vehicule) {
        Alert.showConfirmationAlert("Confirmation de suppression", "Êtes-vous sûr de vouloir supprimer ce véhicule ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            VehiculeService.delete(vehicule.getIdVehicule());
                            loadVehicules();
                            loadMaintenances();
                            Alert.showSuccessAlert("Succès", "Véhicule supprimé avec succès !");
                        } catch (SQLException e) {
                            Alert.showErrorAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                        }
                    }
                });
    }

    private void handleDeleteMaintenance(MaintenanceVehicule maintenance) {
        Alert.showConfirmationAlert("Confirmation de suppression", "Êtes-vous sûr de vouloir supprimer cette maintenance ?")
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            MaintenanceVehiculeService.delete(maintenance.getIdMaintenance());
                            loadMaintenances();
                            Alert.showSuccessAlert("Succès", "Maintenance supprimée avec succès !");
                        } catch (SQLException e) {
                            Alert.showErrorAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                        }
                    }
                });
    }

    @FXML
    private void openAddVehicleForm() {
        menuController.loadView("addVehicule.fxml");
    }
}