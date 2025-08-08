package org.example;

import Persistance.models.Candidat;
import Persistance.models.Moniteur;
import Persistance.models.Seance;
import Persistance.models.Vehicule;
import Persistance.utils.Alert;
import Service.SeanceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.json.JSONObject;

public class SeanceController implements Initializable {
    private ObservableList<Seance> codeSeancesList;
    private FilteredList<Seance> filteredCodeSeances;
    private ObservableList<Seance> conduiteSeancesList;
    private FilteredList<Seance> filteredConduiteSeances;
    // Onglet Code
    @FXML private DatePicker dateSeanceCode;
    @FXML private Label dateErrorCode;
    @FXML private ComboBox<String> heureSeanceCode;
    @FXML private Label heureErrorCode;
    @FXML private TextField cinMoniteurCode;
    @FXML private Label cinMoniteurErrorCode;
    @FXML private TextField cinCandidatCode;
    @FXML private Label cinCandidatErrorCode;
    @FXML private TextArea commentaireSeanceCode;
    @FXML private Button annulerBtnCode;
    @FXML private Button sauvegarderBtnCode;

    // Onglet Conduite
    @FXML private DatePicker dateSeanceConduite;
    @FXML private Label dateErrorConduite;
    @FXML private ComboBox<String> heureSeanceConduite;
    @FXML private Label heureErrorConduite;
    @FXML private TextField cinMoniteurConduite;
    @FXML private Label cinMoniteurErrorConduite;
    @FXML private TextField cinCandidatConduite;
    @FXML private Label cinCandidatErrorConduite;
    @FXML private ComboBox<String> vehiculeSeanceConduite;
    @FXML private Label vehiculeErrorConduite;
    @FXML private TextArea commentaireSeanceConduite;
    @FXML private WebView mapViewConduite;
    @FXML private Label mapErrorConduite;
    @FXML private Button annulerBtnConduite;
    @FXML private Button sauvegarderBtnConduite;

    // Table and Filter Fields
    private ObservableList<Seance> allExams;
    private FilteredList<Seance> filteredExams;
    @FXML private TabPane seanceTabPane;
    @FXML private TableView<Seance> tableSeancesConduite;
    @FXML private TableColumn<Seance, String> conduitIconColumn;
    @FXML private TableColumn<Seance, String> colonneDateHeureConduite;
    @FXML private TableColumn<Seance, String> colonneVehiculeConduite;
    @FXML private TableColumn<Seance, String> colonneLocalisation;
    @FXML private TableColumn<Seance, String> colonneCandidatConduite;
    @FXML private TableColumn<Seance, String> colonneMoniteurConduite;
    @FXML private TableColumn<Seance, Seance> conduitActionsColumn;
    @FXML private TableView<Seance> tableSeancesCode;
    @FXML private TableColumn<Seance, String> codeIconColumn;
    @FXML private TableColumn<Seance, String> colonneDateCode;
    @FXML private TableColumn<Seance, String> colonneMoniteurCode;
    @FXML private TableColumn<Seance, String> colonneCandidatCode;
    @FXML private TableColumn<Seance, Seance> codeActionColumn;
    @FXML private TextField filtreMoniteurCode;
    @FXML private TextField filtreCandidatCode;
    @FXML private DatePicker filtreDateCode;
    @FXML private TextField filtreMoniteurConduite;
    @FXML private TextField filtreCandidatConduite;
    @FXML private ComboBox<String> filtreVehiculeConduite;
    @FXML private DatePicker filtreDateConduite;

    private double latitude;
    private double longitude;
    private String selectedLocation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des dates à aujourd'hui
        dateSeanceCode.setValue(LocalDate.now());
        dateSeanceConduite.setValue(LocalDate.now());

        // Initialisation des heures disponibles (8h-18h)
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 18; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureSeanceCode.setItems(heures);
        heureSeanceConduite.setItems(heures);

        // Initialisation des véhicules
        List<Vehicule> vehiculess = SeanceService.findAllVehicules();
        ObservableList<String> marquesModeles = FXCollections.observableArrayList(
                vehiculess.stream().map(v -> v.getMarque() + " " + v.getModele()).collect(Collectors.toList())
        );
        vehiculeSeanceConduite.setItems(marquesModeles);
        filtreVehiculeConduite.setItems(marquesModeles);

        // Initialisation de la carte
        initializeMapConduite();

        // Set up icon column for conduite
        conduitIconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/seance_conduite_icon.png")));
            {
                imageView.setFitHeight(35);
                imageView.setFitWidth(35);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(imageView);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        // Set up actions column for conduite
        conduitActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox actionsBox = new HBox(10);

            {
                editIcon.setFitHeight(28);
                editIcon.setFitWidth(28);
                deleteIcon.setFitHeight(28);
                deleteIcon.setFitWidth(28);
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                actionsBox.getChildren().addAll(editButton, deleteButton);
                actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

                editButton.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    handleUpdateSeanceConduite(seance);
                });

                deleteButton.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    handleDeleteSeance(seance);
                });
            }

            @Override
            protected void updateItem(Seance seance, boolean empty) {
                super.updateItem(seance, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsBox);
                }
            }
        });

        // Mise à jour des colonnes pour la table de conduite
        colonneDateHeureConduite.setCellValueFactory(cellData -> {
            Timestamp dateTime = cellData.getValue().getDateTime();
            return new SimpleStringProperty(dateTime != null ? dateTime.toString() : "");
        });
        colonneVehiculeConduite.setCellValueFactory(cellData -> {
            String loca = cellData.getValue().getLocation();
            return new SimpleStringProperty(loca != null ? loca : "Non défini");
        });
        colonneLocalisation.setCellValueFactory(cellData -> {
            String localisation = cellData.getValue().getLocalisation();
            return new SimpleStringProperty(localisation != null ? localisation : "Non défini");
        });
        colonneMoniteurConduite.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getMoniteurId())));
        colonneCandidatConduite.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCandidatId())));

        // Placeholder pour table vide
        tableSeancesConduite.setPlaceholder(new Label("Aucune séance de conduite disponible"));

        // Set up icon column for code
        codeIconColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/seance_code_icon.png")));
            {
                imageView.setFitHeight(35);
                imageView.setFitWidth(35);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(imageView);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        // Set up actions column for code
        codeActionColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox actionsBox = new HBox(10);

            {
                editIcon.setFitHeight(28);
                editIcon.setFitWidth(28);
                deleteIcon.setFitHeight(28);
                deleteIcon.setFitWidth(28);
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                actionsBox.getChildren().addAll(editButton, deleteButton);
                actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

                editButton.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    handleUpdateSeanceCode(seance);
                });

                deleteButton.setOnAction(event -> {
                    Seance seance = getTableView().getItems().get(getIndex());
                    handleDeleteSeance(seance);
                });
            }

            @Override
            protected void updateItem(Seance seance, boolean empty) {
                super.updateItem(seance, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsBox);
                }
            }
        });

        // Ajout des colonnes pour la table "code"
        colonneDateCode.setCellValueFactory(cellData -> {
            Timestamp dateTime = cellData.getValue().getDateTime();
            return new SimpleStringProperty(dateTime != null ? dateTime.toString() : "");
        });
        colonneMoniteurCode.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getMoniteurId())));
        colonneCandidatCode.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCandidatId())));

        // Placeholder pour table vide
        tableSeancesCode.setPlaceholder(new Label("Aucune séance de code disponible"));

        // Chargement initial des séances pour les deux tables
        try {
            // Load conduite sessions
            List<Seance> allSeances = SeanceService.getAllSeances();
            conduiteSeancesList = FXCollections.observableArrayList(
                    allSeances.stream().filter(s -> s.getLocation() != null || s.getLatitude() != 0 || s.getLongitude() != 0).collect(Collectors.toList())
            );
            filteredConduiteSeances = new FilteredList<>(conduiteSeancesList, p -> true);
            tableSeancesConduite.setItems(filteredConduiteSeances);

            // Load code sessions
            List<Seance> seancesCode = SeanceService.getAllSeancesCode();
            codeSeancesList = FXCollections.observableArrayList(seancesCode);
            filteredCodeSeances = new FilteredList<>(codeSeancesList, p -> true);
            tableSeancesCode.setItems(filteredCodeSeances);
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Impossible de charger les séances: " + e.getMessage());
        }

        // Ajout des filtres pour les séances de conduite
        filtreMoniteurConduite.textProperty().addListener((observable, oldValue, newValue) -> handleFiltresConduite());
        filtreCandidatConduite.textProperty().addListener((observable, oldValue, newValue) -> handleFiltresConduite());
        filtreVehiculeConduite.valueProperty().addListener((observable, oldValue, newValue) -> handleFiltresConduite());
        filtreDateConduite.valueProperty().addListener((observable, oldValue, newValue) -> handleFiltresConduite());

        // Ajout des filtres pour les séances de code
        filtreMoniteurCode.textProperty().addListener((observable, oldValue, newValue) -> handleFiltresCode());
        filtreCandidatCode.textProperty().addListener((observable, oldValue, newValue) -> handleFiltresCode());
        filtreDateCode.valueProperty().addListener((observable, oldValue, newValue) -> handleFiltresCode());

        // Ajout des listeners pour validation en temps réel
        dateSeanceCode.valueProperty().addListener((obs, old, newV) -> validateFieldsCode());
        heureSeanceCode.valueProperty().addListener((obs, old, newV) -> validateFieldsCode());
        cinMoniteurCode.textProperty().addListener((obs, old, newV) -> validateFieldsCode());
        cinCandidatCode.textProperty().addListener((obs, old, newV) -> validateFieldsCode());

        dateSeanceConduite.valueProperty().addListener((obs, old, newV) -> {
            try {
                validateFieldsConduite();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        });
        heureSeanceConduite.valueProperty().addListener((obs, old, newV) -> {
            try {
                validateFieldsConduite();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        });
        cinMoniteurConduite.textProperty().addListener((obs, old, newV) -> {
            try {
                validateFieldsConduite();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        });
        cinCandidatConduite.textProperty().addListener((obs, old, newV) -> {
            try {
                validateFieldsConduite();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        });
        vehiculeSeanceConduite.valueProperty().addListener((obs, old, newV) -> {
            try {
                validateFieldsConduite();
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        });

        // Added logic from second controller to update seancepasse and seance_effectue
        try {
            List<Candidat> l = SeanceService.getAllCandidats();
            UpdateCandidatController ucc = new UpdateCandidatController();
            for (Candidat c : l) {
                List<Seance> s = SeanceService.getAllSeancesByCandidatId(c.getCin());
                List<Seance> se = SeanceService.getAllSeancesCodeByCandidatId(c.getCin());
                for (Seance ss : s) {
                    if (ss.getDateTime().toLocalDateTime().toLocalDate().equals(LocalDate.now()) || ss.getDateTime().toLocalDateTime().isBefore(LocalDateTime.now())) {
                        if (!SeanceService.rechercherSeanceEffectue(ss.getDateTime(), c.getCin())) {
                            ucc.updateSeancepasse(c.getCin());
                            SeanceService.insererSeanceEffectue(ss.getDateTime(), c.getCin());
                        }
                    }
                }
                for (Seance ss : se) {
                    if (ss.getDateTime().toLocalDateTime().toLocalDate().equals(LocalDate.now()) || ss.getDateTime().toLocalDateTime().isBefore(LocalDateTime.now())) {
                        if (!SeanceService.rechercherSeanceEffectue(ss.getDateTime(), c.getCin())) {
                            ucc.updateSeancepasse(c.getCin());
                            SeanceService.insererSeanceEffectue(ss.getDateTime(), c.getCin());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de la mise à jour des séances: " + e.getMessage());
        }

        // Ensure all error labels are invisible at startup
        dateErrorCode.setVisible(false);
        heureErrorCode.setVisible(false);
        cinMoniteurErrorCode.setVisible(false);
        cinCandidatErrorCode.setVisible(false);

        dateErrorConduite.setVisible(false);
        heureErrorConduite.setVisible(false);
        cinMoniteurErrorConduite.setVisible(false);
        cinCandidatErrorConduite.setVisible(false);
        vehiculeErrorConduite.setVisible(false);
        mapErrorConduite.setVisible(false);
    }

    private void initializeMapConduite() {
        initializeMap(mapViewConduite);
    }

    private void initializeMap(WebView mapView) {
        String mapHtml = """
        <html>
        <body>
            <div id='map' style='width: 100%; height: 100%;'></div>
            <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>
            <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />
            <script>
                console.log = function(msg) { alert('Log: ' + msg); };
                console.error = function(msg) { alert('Error: ' + msg); };
                console.warn = function(msg) { alert('Warn: ' + msg); };

                var map = L.map('map').setView([36.81897, 10.16579], 6);
                var marker;
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);

                map.on('click', function(e) {
                    var lat = e.latlng.lat;
                    var lng = e.latlng.lng;
                    if (marker) {
                        map.removeLayer(marker);
                    }
                    marker = L.marker([lat, lng]).addTo(map).bindPopup('Fetching location...').openPopup();
                    window.app.handleMapClick(lat, lng);
                });
            </script>
        </body>
        </html>
    """;
        mapView.getEngine().loadContent(mapHtml);

        mapView.getEngine().setOnAlert(event -> Alert.showErrorAlert("WebView Alert", event.getData()));
        mapView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) mapView.getEngine().executeScript("window");
                window.setMember("app", this);
            }
        });
    }

    public void handleMapClick(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lng + "&format=json"))
                    .header("User-Agent", "SeanceController/1.0 (your.email@example.com)")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                this.selectedLocation = json.optString("display_name", "Unknown location");
            } else {
                this.selectedLocation = "Unknown location (HTTP " + response.statusCode() + ")";
            }
        } catch (Exception e) {
            this.selectedLocation = "Unknown location (error: " + e.getMessage() + ")";
        }

        String script = String.format(
                "if (marker) { marker.setPopupContent('%s').openPopup(); }",
                this.selectedLocation.replace("'", "\\'")
        );
        mapViewConduite.getEngine().executeScript(script);

        try {
            validateFieldsConduite();
        } catch (SQLException e) {
            Alert.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
        }
    }

    @FXML
    private void handleSauvegarderCode() throws SQLException {
        if (validateFieldsCode()) {
            long moniteurId = Long.parseLong(cinMoniteurCode.getText());
            long candidatId = Long.parseLong(cinCandidatCode.getText());
            LocalDate date = dateSeanceCode.getValue();
            String heure = heureSeanceCode.getValue();
            Timestamp dateTime = Timestamp.valueOf(date.toString() + " " + heure + ":00");

            UpdateCandidatController ucc = new UpdateCandidatController();
            ucc.updateSeancetot(Integer.parseInt(cinCandidatCode.getText()));
            ucc.updatecouttotcode(Integer.parseInt(cinCandidatCode.getText()));
            Seance seance = new Seance(dateTime, moniteurId, candidatId);
            SeanceService.saveSeanceCode(seance);
            codeSeancesList.add(seance);

            Alert.showSuccessAlert("Succès", "La séance de code a été bien enregistrée");
            handleCodeAnnuler();
        }
    }

    @FXML
    private void handleSauvegarderConduite() throws SQLException {
        if (validateFieldsConduite()) {
            double latitudeValue = this.latitude;
            double longitudeValue = this.longitude;
            long moniteurId = Long.parseLong(cinMoniteurConduite.getText());
            long candidatId = Long.parseLong(cinCandidatConduite.getText());
            LocalDate date = dateSeanceConduite.getValue();
            String heure = heureSeanceConduite.getValue();
            Timestamp dateTime = Timestamp.valueOf(date.toString() + " " + heure + ":00");
            UpdateCandidatController ucc = new UpdateCandidatController();
            ucc.updateSeancetot(Integer.parseInt(cinCandidatConduite.getText()));
            ucc.updatecouttot(Integer.parseInt(cinCandidatConduite.getText()));
            Seance seance = new Seance(dateTime, vehiculeSeanceConduite.getValue(), latitudeValue, longitudeValue, selectedLocation, moniteurId, candidatId);
            SeanceService.saveSeance(seance);

            conduiteSeancesList.add(seance);

            Alert.showSuccessAlert("Succès", "La séance de conduite a été bien enregistrée");

            handleAnnuler();
        }
    }

    private void handleUpdateSeanceConduite(Seance seance) {
        LocalDateTime seanceDateTime = seance.getDateTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (seanceDateTime.isBefore(now)) {
            Alert.showErrorAlert("Modification impossible", "La séance est déjà dépassée et ne peut pas être modifiée.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("updateSeanceConduit.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Séance de Conduite");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableSeancesConduite.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            UpdateSeanceConduitController controller = loader.getController();
            controller.setSeance(seance);
            dialogStage.showAndWait();
            refreshSeancesConduite();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification: " + e.getMessage());
        }
    }

    private void handleUpdateSeanceCode(Seance seance) {
        LocalDateTime seanceDateTime = seance.getDateTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (seanceDateTime.isBefore(now)) {
            Alert.showErrorAlert("Modification impossible", "La séance est déjà dépassée et ne peut pas être modifiée.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("updateSeanceCode.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Séance de Code");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableSeancesCode.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UpdateSeanceCodeController controller = loader.getController();
            controller.setSeance(seance);

            dialogStage.showAndWait();

            refreshSeancesCode();
        } catch (IOException e) {
            Alert.showErrorAlert("Erreur", "Impossible de charger la fenêtre de modification: " + e.getMessage());
        }
    }

    private void handleDeleteSeance(Seance seance) {
        // Use showConfirmationAlert to get user confirmation
        Optional<ButtonType> result = Alert.showConfirmationAlert(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer cette séance ?"
        );

        // Proceed only if the user clicks OK
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long candidatId = (long) seance.getCandidatId();
                Timestamp dateTime = seance.getDateTime();
                if (candidatId == null || dateTime == null) {
                    throw new IllegalStateException("Candidat ID or DateTime is null");
                }

                boolean isConduiteSession = seance.getLatitude() != 0 || seance.getLongitude() != 0 || seance.getLocation() != null;

                if (isConduiteSession) {
                    SeanceService.deleteSeanceConduite(candidatId, dateTime);
                    UpdateCandidatController ucc = new UpdateCandidatController();
                    if (seance.getDateTime().toLocalDateTime().isAfter(LocalDateTime.now())) {
                        ucc.updateSeancetot1(seance.getCandidatId());
                        ucc.updatecouttot1(seance.getCandidatId());
                    }
                    conduiteSeancesList.remove(seance);
                } else {
                    SeanceService.deleteSeanceCode(candidatId, dateTime);
                    UpdateCandidatController ucc = new UpdateCandidatController();
                    if (seance.getDateTime().toLocalDateTime().isAfter(LocalDateTime.now())) {
                        ucc.updateSeancetot1(seance.getCandidatId());
                        ucc.updatecouttotcode1(seance.getCandidatId());
                    }
                    codeSeancesList.remove(seance);
                }

                Alert.showSuccessAlert("Succès", "La séance a été supprimée avec succès.");
            } catch (SQLException e) {
                Alert.showErrorAlert("Erreur", "Impossible de supprimer la séance: " + e.getMessage());
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Une erreur s'est produite: " + e.getMessage());
            }
        }
    }
    private void refreshSeancesConduite() {
        try {
            List<Seance> seances = SeanceService.getAllSeances();
            conduiteSeancesList = FXCollections.observableArrayList(
                    seances.stream().filter(s -> s.getLocation() != null || s.getLatitude() != 0 || s.getLongitude() != 0).collect(Collectors.toList())
            );
            filteredConduiteSeances = new FilteredList<>(conduiteSeancesList, p -> true);
            tableSeancesConduite.setItems(filteredConduiteSeances);
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Impossible de rafraîchir les séances de conduite: " + e.getMessage());
        }
    }

    private void refreshSeancesCode() {
        try {
            List<Seance> seancesCode = SeanceService.getAllSeancesCode();
            codeSeancesList = FXCollections.observableArrayList(seancesCode);
            filteredCodeSeances = new FilteredList<>(codeSeancesList, p -> true);
            tableSeancesCode.setItems(filteredCodeSeances);
        } catch (Exception e) {
            Alert.showErrorAlert("Erreur", "Impossible de rafraîchir les séances de code: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        dateSeanceConduite.setValue(null);
        heureSeanceConduite.setValue(null);
        cinMoniteurConduite.clear();
        cinCandidatConduite.clear();
        vehiculeSeanceConduite.setValue(null);
        commentaireSeanceConduite.clear();
        clearErrorsConduite();
    }

    @FXML
    private void handleCodeAnnuler() {
        dateSeanceCode.setValue(null);
        heureSeanceCode.setValue(null);
        cinMoniteurCode.clear();
        cinCandidatCode.clear();
        commentaireSeanceCode.clear();
        clearErrorsCode();
    }

    private boolean validateFieldsCode() {
        boolean isValid = true;

        clearErrorsCode();

        if (dateSeanceCode.getValue() == null) {
            dateErrorCode.setText("Veuillez sélectionner une date");
            dateErrorCode.setVisible(true);
            dateSeanceCode.setStyle("-fx-border-color: red");
            isValid = false;
        } else if (dateSeanceCode.getValue().isBefore(LocalDate.now())) {
            dateErrorCode.setText("Il faut choisir une date future");
            dateErrorCode.setVisible(true);
            dateSeanceCode.setStyle("-fx-border-color: red");
            isValid = false;
        }

        if (heureSeanceCode.getValue() == null) {
            heureErrorCode.setText("Veuillez sélectionner une heure");
            heureErrorCode.setVisible(true);
            heureSeanceCode.setStyle("-fx-border-color: red");
            isValid = false;
        }

        String cinMoniteurText = cinMoniteurCode.getText().trim();
        int cinMoniteur;
        if (cinMoniteurText.isEmpty()) {
            cinMoniteurErrorCode.setText("Veuillez entrer le CIN du moniteur");
            cinMoniteurErrorCode.setVisible(true);
            cinMoniteurCode.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            try {
                cinMoniteur = Integer.parseInt(cinMoniteurText);
                if (SeanceService.getMoniteur(cinMoniteur) == null) {
                    cinMoniteurErrorCode.setText("Ce moniteur n'existe pas");
                    cinMoniteurErrorCode.setVisible(true);
                    cinMoniteurCode.setStyle("-fx-border-color: red");
                    isValid = false;
                } else {
                    List<Seance> seances = SeanceService.getSeancesByMoniteurId(cinMoniteur);
                    List<Seance> seancesCode = SeanceService.getSeancesCodeByMoniteurId(cinMoniteur);
                    Timestamp dateTime = getTimestamp(dateSeanceCode.getValue(), heureSeanceCode.getValue());
                    if (dateTime != null) {
                        if (isMoniteurBusy(seances, dateTime)) {
                            cinMoniteurErrorCode.setText("Moniteur occupé (conduite)");
                            cinMoniteurErrorCode.setVisible(true);
                            cinMoniteurCode.setStyle("-fx-border-color: red");
                            isValid = false;
                        } else if (isMoniteurBusy(seancesCode, dateTime)) {
                            cinMoniteurErrorCode.setText("Moniteur occupé (code)");
                            cinMoniteurErrorCode.setVisible(true);
                            cinMoniteurCode.setStyle("-fx-border-color: red");
                            isValid = false;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                cinMoniteurErrorCode.setText("CIN doit être un nombre");
                cinMoniteurErrorCode.setVisible(true);
                cinMoniteurCode.setStyle("-fx-border-color: red");
                isValid = false;
            }
        }

        String cinCandidatText = cinCandidatCode.getText().trim();
        if (cinCandidatText.isEmpty()) {
            cinCandidatErrorCode.setText("Veuillez entrer le CIN du candidat");
            cinCandidatErrorCode.setVisible(true);
            cinCandidatCode.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            try {
                int cinCandidat = Integer.parseInt(cinCandidatText);
                if (SeanceService.getCandidat(cinCandidat) == null) {
                    cinCandidatErrorCode.setText("Ce candidat n'existe pas");
                    cinCandidatErrorCode.setVisible(true);
                    cinCandidatCode.setStyle("-fx-border-color: red");
                    isValid = false;
                } else if (SeanceService.getMoniteur(cinCandidat) != null) {
                    cinCandidatErrorCode.setText("Ce candidat est un moniteur");
                    cinCandidatErrorCode.setVisible(true);
                    cinCandidatCode.setStyle("-fx-border-color: red");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                cinCandidatErrorCode.setText("CIN doit être un nombre");
                cinCandidatErrorCode.setVisible(true);
                cinCandidatCode.setStyle("-fx-border-color: red");
                isValid = false;
            } catch (SQLException e) {
                cinCandidatErrorCode.setText("Erreur base de données");
                cinCandidatErrorCode.setVisible(true);
                cinCandidatCode.setStyle("-fx-border-color: red");
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateFieldsConduite() throws SQLException {
        boolean isValid = true;

        clearErrorsConduite();

        if (dateSeanceConduite.getValue() == null) {
            dateErrorConduite.setText("Veuillez sélectionner une date");
            dateErrorConduite.setVisible(true);
            dateSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }
        if (dateSeanceConduite.getValue().isBefore(LocalDate.now())) {
            dateErrorConduite.setText("Il faut choisir une date future");
            dateErrorConduite.setVisible(true);
            dateSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }

        if (heureSeanceConduite.getValue() == null) {
            heureErrorConduite.setText("Veuillez sélectionner une heure");
            heureErrorConduite.setVisible(true);
            heureSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }

        Timestamp dateTime = getTimestamp(dateSeanceConduite.getValue(), heureSeanceConduite.getValue());
        if (dateTime == null) {
            dateErrorConduite.setText("Erreur lors de la conversion de la date/heure");
            dateErrorConduite.setVisible(true);
            dateSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }

        String cinMoniteurText = cinMoniteurConduite.getText().trim();
        int cinMoniteur;
        if (cinMoniteurText.isEmpty()) {
            cinMoniteurErrorConduite.setText("Veuillez entrer le CIN du moniteur");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }
        try {
            cinMoniteur = Integer.parseInt(cinMoniteurText);
        } catch (NumberFormatException e) {
            cinMoniteurErrorConduite.setText("CIN doit être un nombre");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }

        Moniteur moniteur = SeanceService.getMoniteur(cinMoniteur);
        if (moniteur == null) {
            cinMoniteurErrorConduite.setText("Ce moniteur n'existe pas");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }
        List<Seance> seances = SeanceService.getSeancesByMoniteurId(cinMoniteur);
        List<Seance> seancesCode = SeanceService.getSeancesCodeByMoniteurId(cinMoniteur);
        if (isMoniteurBusy(seances, dateTime)) {
            cinMoniteurErrorConduite.setText("Moniteur occupé (conduite)");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }
        if (isMoniteurBusy(seancesCode, dateTime)) {
            cinMoniteurErrorConduite.setText("Moniteur occupé (code)");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }

        String cinCandidatText = cinCandidatConduite.getText().trim();
        int cinCandidat;
        if (cinCandidatText.isEmpty()) {
            cinCandidatErrorConduite.setText("Veuillez entrer le CIN du candidat");
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }
        try {
            cinCandidat = Integer.parseInt(cinCandidatText);
        } catch (NumberFormatException e) {
            cinCandidatErrorConduite.setText("CIN doit être un nombre");
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }

        Candidat candidat = SeanceService.getCandidat(cinCandidat);
        if (candidat == null) {
            cinCandidatErrorConduite.setText("Ce candidat n'existe pas");
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }
        if (SeanceService.getMoniteur(cinCandidat) != null) {
            cinCandidatErrorConduite.setText("Ce candidat est un moniteur");
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }

        List<Character> categoriesMoniteur = SeanceService.getCategoriesByCin(cinMoniteurText);
        String categorieCandidat = candidat.getCategorie();
        if (categorieCandidat == null) {
            cinCandidatErrorConduite.setText("Le candidat n'a pas de catégorie définie");
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }
        if (!categoriesMoniteur.contains(categorieCandidat.charAt(0))) {
            cinMoniteurErrorConduite.setText("Ce moniteur n'est pas compatible avec la catégorie du permis de ce candidat");
            cinMoniteurErrorConduite.setVisible(true);
            cinMoniteurConduite.setStyle("-fx-border-color: red");
            return false;
        }

        Seance existingSeance = SeanceService.getSeanceByCandidatId(cinCandidat);
        if (existingSeance != null && !Objects.equals(existingSeance.getLocation(), vehiculeSeanceConduite.getValue())) {
            cinCandidatErrorConduite.setText("Voiture convenable: " + existingSeance.getLocation());
            cinCandidatErrorConduite.setVisible(true);
            cinCandidatConduite.setStyle("-fx-border-color: red");
            return false;
        }

        if (vehiculeSeanceConduite.getValue() == null) {
            vehiculeErrorConduite.setText("Veuillez sélectionner un véhicule");
            vehiculeErrorConduite.setVisible(true);
            vehiculeSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }
        if (!SeanceService.getSeancesByDateAndVehicule(dateTime, vehiculeSeanceConduite.getValue()).isEmpty()) {
            vehiculeErrorConduite.setText("Voiture non disponible");
            vehiculeErrorConduite.setVisible(true);
            vehiculeSeanceConduite.setStyle("-fx-border-color: red");
            return false;
        }

        if (latitude == 0 && longitude == 0) {
            mapErrorConduite.setText("Veuillez sélectionner un point sur la carte");
            mapErrorConduite.setVisible(true);
            return false;
        }

        return isValid;
    }

    @FXML
    private void handleFiltresConduite() {
        String moniteur = filtreMoniteurConduite.getText().trim();
        String candidat = filtreCandidatConduite.getText().trim();
        String vehicule = filtreVehiculeConduite.getValue();
        LocalDate date = filtreDateConduite.getValue();

        filteredConduiteSeances.setPredicate(seance -> {
            boolean matches = true;

            if (moniteur != null && !moniteur.isEmpty()) {
                try {
                    long moniteurId = Long.parseLong(moniteur);
                    matches = matches && seance.getMoniteurId() == moniteurId;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (candidat != null && !candidat.isEmpty()) {
                try {
                    long candidatId = Long.parseLong(candidat);
                    matches = matches && seance.getCandidatId() == candidatId;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (vehicule != null && !vehicule.isEmpty()) {
                matches = matches && seance.getLocation().equalsIgnoreCase(vehicule);
            }

            if (date != null) {
                LocalDate seanceDate = seance.getDateTime().toLocalDateTime().toLocalDate();
                matches = matches && seanceDate.equals(date);
            }

            return matches;
        });
    }

    @FXML
    private void handleResetFiltresConduite() {
        filtreMoniteurConduite.clear();
        filtreCandidatConduite.clear();
        filtreVehiculeConduite.setValue(null);
        filtreDateConduite.setValue(null);

        filteredConduiteSeances.setPredicate(seance -> true);
    }

    @FXML
    private void handleFiltresCode() {
        String moniteur = filtreMoniteurCode.getText().trim();
        String candidat = filtreCandidatCode.getText().trim();
        LocalDate date = filtreDateCode.getValue();

        filteredCodeSeances.setPredicate(seance -> {
            boolean matches = true;

            if (moniteur != null && !moniteur.isEmpty()) {
                try {
                    long moniteurId = Long.parseLong(moniteur);
                    matches = matches && seance.getMoniteurId() == moniteurId;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (candidat != null && !candidat.isEmpty()) {
                try {
                    long candidatId = Long.parseLong(candidat);
                    matches = matches && seance.getCandidatId() == candidatId;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (date != null) {
                LocalDate seanceDate = seance.getDateTime().toLocalDateTime().toLocalDate();
                matches = matches && seanceDate.equals(date);
            }

            return matches;
        });
    }

    @FXML
    private void handleResetFiltresCode() {
        filtreMoniteurCode.clear();
        filtreCandidatCode.clear();
        filtreDateCode.setValue(null);

        filteredCodeSeances.setPredicate(seance -> true);
    }

    private void clearErrorsCode() {
        dateErrorCode.setVisible(false);
        heureErrorCode.setVisible(false);
        cinMoniteurErrorCode.setVisible(false);
        cinCandidatErrorCode.setVisible(false);

        dateSeanceCode.setStyle("-fx-border-color: null");
        heureSeanceCode.setStyle("-fx-border-color: null");
        cinMoniteurCode.setStyle("-fx-border-color: null");
        cinCandidatCode.setStyle("-fx-border-color: null");
    }

    private void clearErrorsConduite() {
        dateErrorConduite.setVisible(false);
        heureErrorConduite.setVisible(false);
        cinMoniteurErrorConduite.setVisible(false);
        cinCandidatErrorConduite.setVisible(false);
        vehiculeErrorConduite.setVisible(false);
        mapErrorConduite.setVisible(false);

        dateSeanceConduite.setStyle("-fx-border-color: null");
        heureSeanceConduite.setStyle("-fx-border-color: null");
        cinMoniteurConduite.setStyle("-fx-border-color: null");
        cinCandidatConduite.setStyle("-fx-border-color: null");
        vehiculeSeanceConduite.setStyle("-fx-border-color: null");
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