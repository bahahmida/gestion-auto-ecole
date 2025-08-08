package org.example;

import Service.SeanceService;
import Persistance.models.Seance;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.List;

public class MapViewController {
    @FXML
    private WebView webView;
    @FXML
    private Button backButton;
    @FXML
    private Button refreshButton;

    private SeanceService seanceService;

    public void initialize() {
        seanceService = new SeanceService();
        loadMap();
    }

    private void loadMap() {
        String mapHtml = generateMapHtml();
        webView.getEngine().loadContent(mapHtml);
        displaySeances();
    }

    private String generateMapHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Carte des Rendez-vous</title>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                <style>
                    #map { height: 100vh; width: 100%; }
                </style>
            </head>
            <body style="margin:0;">
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([36.81897, 10.16579], 6); // Centre sur la Tunisie
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors'
                    }).addTo(map);

                    // Function to fetch place name from Nominatim API
                    async function getPlaceName(lat, lng) {
                        const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`;
                        try {
                            const response = await fetch(url);
                            const data = await response.json();
                            return data.display_name || 'Unknown location';
                        } catch (error) {
                            console.error('Error fetching place name:', error);
                            return 'Unknown location';
                        }
                    }

                    // Add click event to place a marker with place name
                    map.on('click', async function(e) {
                        const lat = e.latlng.lat;
                        const lng = e.latlng.lng;
                        const placeName = await getPlaceName(lat, lng);
                        L.marker([lat, lng]).addTo(map).bindPopup(placeName).openPopup();
                    });
                </script>
            </body>
            </html>
            """;
    }

    private void displaySeances() {
        List<Seance> seances = seanceService.getAllSeances();
        for (Seance seance : seances) {
            addMarker(seance);
        }
    }

    private void addMarker(Seance seance) {
        String script = String.format(
                "L.marker([%f, %f]).addTo(map).bindPopup('%s<br>Date: %s<br>Type: %s');",
                seance.getLatitude(),
                seance.getLongitude(),
                seance.getLocation(),
                seance.getDateTime().toString(),
                seance.getType()
        );
        webView.getEngine().executeScript(script);
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRefresh() {
        loadMap();
    }
}