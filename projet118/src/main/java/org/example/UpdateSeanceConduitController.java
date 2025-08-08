package org.example;

import Persistance.models.Seance;
import Persistance.utils.Alert; // Import ajouté
import Service.SeanceService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import javafx.concurrent.Worker;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import org.json.JSONObject;

public class UpdateSeanceConduitController implements Initializable {

    @FXML private TextField cinMoniteurField;
    @FXML private Label cinMoniteurErrorLabel;
    @FXML private TextField cinCandidatField;
    @FXML private Label cinCandidatErrorLabel;
    @FXML private DatePicker dateSeancePicker;
    @FXML private Label dateSeanceErrorLabel;
    @FXML private ComboBox<String> heureSeanceCombo;
    @FXML private Label heureSeanceErrorLabel;
    @FXML private WebView mapView;
    @FXML private Label mapErrorLabel;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private double latitude;
    private double longitude;
    private String selectedLocation; // To store the place name
    private Seance seanceToUpdate;
    private Timestamp originalDateTime;
    private boolean isMapLoaded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des heures disponibles (8h-18h)
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 18; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureSeanceCombo.setItems(heures);

        // Initialisation de la carte
        initializeMap(mapView);

        // Ajout des listeners pour validation en temps réel
        dateSeancePicker.valueProperty().addListener((obs, old, newV) -> validateFieldsConduite());
        heureSeanceCombo.valueProperty().addListener((obs, old, newV) -> validateFieldsConduite());
        cinMoniteurField.textProperty().addListener((obs, old, newV) -> validateFieldsConduite());
    }

    public void setSeance(Seance seance) {
        this.seanceToUpdate = seance;
        this.originalDateTime = seance.getDateTime();
        cinMoniteurField.setText(String.valueOf(seance.getMoniteurId()));
        cinCandidatField.setText(String.valueOf(seance.getCandidatId()));
        dateSeancePicker.setValue(seance.getDateTime().toLocalDateTime().toLocalDate());
        heureSeanceCombo.setValue(seance.getDateTime().toLocalDateTime().toLocalTime().toString().substring(0, 5));
        this.latitude = seance.getLatitude();
        this.longitude = seance.getLongitude();
        this.selectedLocation = seance.getLocalisation(); // Load the existing localisation

        // Fetch the place name for the initial coordinates if localisation is not set
        if (latitude != 0 && longitude != 0 && (selectedLocation == null || selectedLocation.isEmpty())) {
            fetchPlaceName(latitude, longitude);
        }

        // Update the map marker if the map is loaded
        if (isMapLoaded) {
            updateMapMarker();
        }
    }

    private void initializeMap(WebView mapView) {
        try {
            String mapHtml = """
                <html>
                <body>
                    <div id='map' style='width: 100%; height: 100%;'></div>
                    <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>
                    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />
                    <script>
                        // Redirect console logs to alerts for visibility in WebView
                        console.log = function(msg) { alert('Log: ' + msg); };
                        console.error = function(msg) { alert('Error: ' + msg); };
                        console.warn = function(msg) { alert('Warn: ' + msg); };

                        var map = L.map('map').setView([36.81897, 10.16579], 6); // Center on Tunisia
                        var marker;
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(map);

                        map.on('click', function(e) {
                            var lat = e.latlng.lat;
                            var lng = e.latlng.lng;
                            console.log('Map clicked at: ' + lat + ', ' + lng);
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

            mapView.getEngine().setOnAlert(event -> System.out.println("WebView Alert: " + event.getData()));
            mapView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) mapView.getEngine().executeScript("window");
                    window.setMember("app", this);
                    isMapLoaded = true;
                    if (latitude != 0 && longitude != 0) {
                        updateMapMarker();
                    }
                } else if (newValue == Worker.State.FAILED) {
                    setErrorLabel(mapErrorLabel, "Erreur lors du chargement de la carte");
                }
            });

            // Add WebView error listener
            mapView.getEngine().setOnError(event -> {
                setErrorLabel(mapErrorLabel, "Erreur WebView: " + event.getMessage());
            });
        } catch (Exception e) {
            e.printStackTrace();
            setErrorLabel(mapErrorLabel, "Erreur lors de l'initialisation de la carte: " + e.getMessage());
        }
    }

    private void updateMapMarker() {
        if (latitude != 0 && longitude != 0) {
            try {
                String popupContent = selectedLocation != null && !selectedLocation.isEmpty() ? selectedLocation : "Fetching location...";
                String script = String.format(
                        "if (typeof marker !== 'undefined') { map.removeLayer(marker); }" +
                                "marker = L.marker([%f, %f]).addTo(map).bindPopup('%s').openPopup();" +
                                "map.setView([%f, %f], 13);",
                        latitude, longitude, popupContent.replace("'", "\\'"), latitude, longitude
                );
                mapView.getEngine().executeScript(script);
            } catch (Exception e) {
                setErrorLabel(mapErrorLabel, "Erreur lors de la mise à jour de la carte");
            }
        }
    }

    public void handleMapClick(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
        fetchPlaceName(lat, lng);
        validateFieldsConduite();
    }

    private void fetchPlaceName(double lat, double lng) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lng + "&format=json"))
                    .header("User-Agent", "UpdateSeanceConduitController/1.0 (your.email@example.com)") // Replace with your app/email
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


        // Update the marker's popup with the place name
        if (isMapLoaded) {
            updateMapMarker();
        }
    }

    @FXML
    private void handleSave() {
        if (validateFieldsConduite()) {
            long moniteurId = Long.parseLong(cinMoniteurField.getText());
            LocalDate date = dateSeancePicker.getValue();
            String heure = heureSeanceCombo.getValue();
            Timestamp newDateTime = Timestamp.valueOf(date.toString() + " " + heure + ":00");

            // Update the Seance object with the new values
            seanceToUpdate.setMoniteurId(moniteurId);
            seanceToUpdate.setDateTime(newDateTime);
            seanceToUpdate.setLatitude(latitude);
            seanceToUpdate.setLongitude(longitude);
            seanceToUpdate.setLocalisation(selectedLocation); // Update the localisation field



            try {
                SeanceService.updateSeance(seanceToUpdate, originalDateTime);
                Alert.showSuccessAlert("Succès", "La séance de conduite a été mise à jour avec succès");
                handleCancel();
            } catch (Exception e) {
                Alert.showErrorAlert("Erreur", "Échec de la mise à jour de la séance : " + e.getMessage());
            }
        } else {
            Alert.showErrorAlert("Erreur de saisie", "Veuillez corriger les erreurs avant de continuer.");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateFieldsConduite() {
        boolean isValid = true;

        // Reset error labels
        clearErrorsConduite();

        // Date
        if (dateSeancePicker.getValue() == null) {
            setErrorLabel(dateSeanceErrorLabel, "Veuillez sélectionner une date");
            isValid = false;
        } else if (dateSeancePicker.getValue().isBefore(LocalDate.now())) {
            setErrorLabel(dateSeanceErrorLabel, "Il faut choisir une date future");
            isValid = false;
        }

        // Heure
        if (heureSeanceCombo.getValue() == null) {
            setErrorLabel(heureSeanceErrorLabel, "Veuillez sélectionner une heure");
            isValid = false;
        }

        // CIN Moniteur
        String cinMoniteurText = cinMoniteurField.getText().trim();
        int cinMoniteur;
        if (cinMoniteurText.isEmpty()) {
            setErrorLabel(cinMoniteurErrorLabel, "Veuillez entrer le CIN du moniteur");
            isValid = false;
        } else {
            try {
                cinMoniteur = Integer.parseInt(cinMoniteurText);
                if (SeanceService.getMoniteur(cinMoniteur) == null) {
                    setErrorLabel(cinMoniteurErrorLabel, "Ce moniteur n'existe pas");
                    isValid = false;
                } else {
                    List<Seance> seances = SeanceService.getSeancesByMoniteurId(cinMoniteur);
                    List<Seance> seancesCode = SeanceService.getSeancesCodeByMoniteurId(cinMoniteur);
                    Timestamp dateTime = getTimestamp(dateSeancePicker.getValue(), heureSeanceCombo.getValue());
                    if (dateTime != null) {
                        if (isMoniteurBusy(seances, dateTime) && !dateTime.equals(originalDateTime)) {
                            setErrorLabel(cinMoniteurErrorLabel, "Moniteur occupé (conduite)");
                            isValid = false;
                        } else if (isMoniteurBusy(seancesCode, dateTime) && !dateTime.equals(originalDateTime)) {
                            setErrorLabel(cinMoniteurErrorLabel, "Moniteur occupé (code)");
                            isValid = false;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                setErrorLabel(cinMoniteurErrorLabel, "CIN doit être un nombre");
                isValid = false;
            }
        }

        // Localisation
        if (latitude == 0 && longitude == 0) {
            setErrorLabel(mapErrorLabel, "Veuillez sélectionner un point sur la carte");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrorsConduite() {
        // Clear text and hide labels
        cinMoniteurErrorLabel.setText("");
        cinMoniteurErrorLabel.setVisible(false);
        cinMoniteurErrorLabel.setManaged(false);

        cinCandidatErrorLabel.setText("");
        cinCandidatErrorLabel.setVisible(false);
        cinCandidatErrorLabel.setManaged(false);

        dateSeanceErrorLabel.setText("");
        dateSeanceErrorLabel.setVisible(false);
        dateSeanceErrorLabel.setManaged(false);

        heureSeanceErrorLabel.setText("");
        heureSeanceErrorLabel.setVisible(false);
        heureSeanceErrorLabel.setManaged(false);

        mapErrorLabel.setText("");
        mapErrorLabel.setVisible(false);
        mapErrorLabel.setManaged(false);
    }

    private void setErrorLabel(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
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