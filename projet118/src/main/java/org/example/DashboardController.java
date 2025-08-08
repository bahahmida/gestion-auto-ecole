package org.example;

import Persistance.dao.*;
import Persistance.models.*;
import Service.DashboardService;
import Service.MaintenanceVehiculeService;
import Service.PasserExamenService;
import Service.VehiculeService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Persistance.utils.Alert.showErrorAlert;
import static Persistance.utils.Alert.showConfirmationAlert;

public class DashboardController {

    private MenuController menuController;
    @FXML private Label visiteTechnique;
    @FXML private Label VehiculeVisiteTechnique;
    @FXML private Label Assurance;
    @FXML private Label VehiculeAssurance;
    @FXML private Label VignetteFiscale;
    @FXML private Label VehiculeVignette;
    @FXML private Label candidatTotal;
    @FXML private Label candidatInactif;
    @FXML private Label moniteurTotal;
    @FXML private Label vehiculeTotale;
    @FXML private Label vehiculeEnPanne;
    @FXML private Label examenTotal;
    @FXML private Label examenEnAttente;
    @FXML private StackPane graphe1;
    @FXML private StackPane graphe2;
    @FXML private StackPane graphe3;
    @FXML private Label nbrExamen;
    @FXML private Label nbrSeanceCode;
    @FXML private Label nbrSeanceConduite;
    @FXML private Button exporterButton;
    @FXML private Button nouveauCandidatButton;
    @FXML private ComboBox<String> yearSelector;
    @FXML private StackPane candidateIcon;
    @FXML private StackPane instructorIcon;
    @FXML private StackPane vehicleIcon;
    @FXML private StackPane examIcon;

    // Setter for MenuController
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML
    private void initialize() {
        try {
            // Examens à venir
            long examenAvenir = DashboardService.countUpcomingExamsThisWeek();
            nbrExamen.setText(examenAvenir + " Examens à venir");

            // Séances de code à venir
            long seanceAvenir = DashboardService.countUpcomingCodeSessionsThisWeek();
            nbrSeanceCode.setText(seanceAvenir + " Seances Code à venir ");

            // Séances de conduite à venir
            long seanceConduite = DashboardService.countUpcomingDrivingSessionsThisWeek();
            nbrSeanceConduite.setText(String.valueOf(seanceConduite) + " Seance Conduite à venir");

            // Fetch and display total candidates
            List<Candidat> allCandidats = DashboardService.getAllCandidats();
            if (candidatTotal != null) {
                candidatTotal.setText(String.valueOf(allCandidats.size()));
            } else {
                showErrorAlert("Erreur", "candidatTotal Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Count inactive candidates
            long inactifCount = DashboardService.countInactiveCandidates(allCandidats);
            if (candidatInactif != null) {
                candidatInactif.setText(inactifCount + " inactifs");
            } else {
                showErrorAlert("Erreur", "candidatInactif Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Fetch and display total instructors
            List<Moniteur> allMoniteurs = DashboardService.findAll();
            if (moniteurTotal != null) {
                moniteurTotal.setText(String.valueOf(allMoniteurs.size()));
            } else {
                showErrorAlert("Erreur", "moniteurTotal Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Fetch and display total vehicles
            List<Vehicule> allVehicules = DashboardService.findAllVehicules();
            if (vehiculeTotale != null) {
                vehiculeTotale.setText(String.valueOf(allVehicules.size()));
            } else {
                showErrorAlert("Erreur", "vehiculeTotale Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Count vehicles in maintenance
            List<MaintenanceVehicule> maintenanceList = MaintenanceVehiculeService.getCurrentMaintenances();
            int x = maintenanceList.size();
            if (vehiculeEnPanne != null) {
                vehiculeEnPanne.setText(x + " en maintenance");
            } else {
                showErrorAlert("Erreur", "vehiculeEnPanne Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Fetch and display total exams
            List<PasserExamen> allExamens = DashboardService.findAllExamen();
            if (examenTotal != null) {
                examenTotal.setText(String.valueOf(allExamens.size()));
            } else {
                showErrorAlert("Erreur", "examenTotal Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Count pending exams
            long examensEnAttente = DashboardService.countPendingExams(allExamens);
            if (examenEnAttente != null) {
                examenEnAttente.setText(examensEnAttente + " en attente");
            } else {
                showErrorAlert("Erreur", "examenEnAttente Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            // Initialize charts for current month
            initializePerformanceCharts();
            pluprocheVisitetechniqueetsonvehiculeetnombredejour();
            pluprocheAssuranceEtSonVehiculeEtNombreDeJour();
            pluprocheVignetteEtSonVehiculeEtNombreDeJour();

            // Add shake animations and click handlers to each icon
            addShakeAnimationAndClickHandler(candidateIcon, this::showCandidat);
            addShakeAnimationAndClickHandler(instructorIcon, this::showMoniteur);
            addShakeAnimationAndClickHandler(vehicleIcon, this::showVehicules);
            addShakeAnimationAndClickHandler(examIcon, this::showExams);

        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de l'initialisation du tableau de bord: " + e.getMessage());
        }
    }

    private void addShakeAnimationAndClickHandler(StackPane iconContainer, Runnable action) {
        Label iconLabel = (Label) iconContainer.getChildren().stream()
                .filter(node -> node instanceof Label)
                .findFirst()
                .orElse(null);

        if (iconLabel == null) return;

        Timeline timeline = new Timeline();
        KeyFrame shakeLeft = new KeyFrame(Duration.seconds(0.2),
                new KeyValue(iconLabel.rotateProperty(), -15));
        KeyFrame shakeRight = new KeyFrame(Duration.seconds(0.4),
                new KeyValue(iconLabel.rotateProperty(), 15));
        KeyFrame returnToCenter = new KeyFrame(Duration.seconds(0.6),
                new KeyValue(iconLabel.rotateProperty(), 0));
        timeline.getKeyFrames().addAll(shakeLeft, shakeRight, returnToCenter);
        timeline.setCycleCount(Timeline.INDEFINITE);

        iconContainer.setOnMouseEntered(event -> timeline.play());
        iconContainer.setOnMouseExited(event -> {
            timeline.stop();
            iconLabel.setRotate(0);
        });

        iconContainer.setOnMouseClicked(event -> action.run());
    }

    @FXML
    private void showCandidat() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadCandidates();
    }

    @FXML
    private void showMoniteur() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadMoniteur();
    }

    @FXML
    private void showVehicules() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadVehicles();
    }

    @FXML
    private void showExams() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadExams();
    }

    @FXML
    private void addCandidat() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadView("addCandidat.fxml");
        menuController.setActiveButton(menuController.getCandidatesBtn());
    }

    @FXML
    private void showAllExamsCetteSemaine() {
        try {
            List<PasserExamen> examens = PasserExamenService.filterByThisWeek();
            if (examens == null || examens.isEmpty()) {
                return; // Ne rien faire si la liste est vide
            }
            openNewWindow("/org/example/examenAvenir.fxml", "Examens à Venir Cette Semaine");
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du chargement des examens: " + e.getMessage());
        }
    }

    @FXML
    private void showAllSeanceCode() {
        List<Seance> seances = DashboardService.filterCodeByThisWeek();
        if (seances == null || seances.isEmpty()) {
            return; // Ne rien faire si la liste est vide
        }
        openNewWindow("/org/example/seanceCodeAvenir.fxml", "Séances de Code à Venir Cette Semaine");
    }

    @FXML
    private void ShowAllSeanceConduit() {
        List<Seance> seances = DashboardService.filterConduiteByThisWeek();
        if (seances == null || seances.isEmpty()) {
            return; // Ne rien faire si la liste est vide
        }
        openNewWindow("/org/example/seanceConduitAvenir.fxml", "Séances de Conduite à Venir Cette Semaine");
    }

    private void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    @FXML
    public void showPaiement() {
        if (menuController == null) {
            showErrorAlert("Erreur", "MenuController n'est pas initialisé.");
            return;
        }
        menuController.loadPayments();
    }

    private void initializePerformanceCharts() {
        try {
            // --- First Chart: Revenue and Total Expense (Maintenance + Salaries) Evolution for Current Month ---
            CategoryAxis xAxis1 = new CategoryAxis();
            NumberAxis yAxis1 = new NumberAxis();
            LineChart<String, Number> lineChart1 = new LineChart<>(xAxis1, yAxis1);

            // Apply the CSS file
            lineChart1.getStylesheets().add(getClass().getResource("graphe1.css").toExternalForm());

            // Get current month and year
            LocalDate today = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(today);
            String monthName = yearMonth.getMonth().toString();
            int year = yearMonth.getYear();

            xAxis1.setLabel("Jour");
            yAxis1.setLabel("Montant (MAD)");
            lineChart1.setTitle("Évolution des revenus et dépenses (maintenance + salaires) - " + monthName + " " + year);

            // Fetch payments for the current month
            List<Paiement> paiements = PaiementDAO.getPaiementsForMonth(yearMonth.getYear(), yearMonth.getMonthValue());

            // Aggregate revenue by day
            Map<Integer, Double> revenueByDay = paiements.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getDatePaiement().getDayOfMonth(),
                            Collectors.summingDouble(Paiement::getMontant)
                    ));

            // Fetch maintenance expenses for the current month
            List<MaintenanceVehicule> maintenances = MaintenanceVehiculeDAO.getMaintenancesForMonth(yearMonth.getYear(), yearMonth.getMonthValue());

            // Aggregate maintenance expenses by day
            Map<Integer, Double> expenseByDay = maintenances.stream()
                    .filter(m -> m.getDateDebut() != null)
                    .collect(Collectors.groupingBy(
                            m -> m.getDateDebut().getDayOfMonth(),
                            Collectors.summingDouble(MaintenanceVehicule::getCout)
                    ));

            // Fetch total salary for the month and assign to the second day
            double totalSalary = MoniteurDAO.getTotalSalaryForMonth(yearMonth.getYear(), yearMonth.getMonthValue());
            expenseByDay.merge(2, totalSalary, Double::sum); // Add salaries to day 2

            // Create series for revenue
            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenus");

            // Create series for expenses
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Dépenses (maintenance + salaires)");

            // Add data points for each day of the month
            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                Double revenue = revenueByDay.getOrDefault(day, 0.0);
                Double expense = expenseByDay.getOrDefault(day, 0.0);
                revenueSeries.getData().add(new XYChart.Data<>(String.valueOf(day), revenue));
                expenseSeries.getData().add(new XYChart.Data<>(String.valueOf(day), expense));
            }

            // Add series to chart
            lineChart1.getData().addAll(revenueSeries, expenseSeries);
            lineChart1.setCreateSymbols(true);
            lineChart1.setLegendVisible(true);

            // Disable default animation for smoother appearance
            lineChart1.setAnimated(false);

            // Handle empty data
            boolean hasRevenueData = revenueSeries.getData().stream().anyMatch(d -> d.getYValue().doubleValue() > 0);
            boolean hasExpenseData = expenseSeries.getData().stream().anyMatch(d -> d.getYValue().doubleValue() > 0);
            if (!hasRevenueData && !hasExpenseData) {
                Label noDataLabel = new Label("Aucune donnée de revenus ou dépenses disponible pour " + monthName + " " + year);
                graphe1.getChildren().clear();
                graphe1.getChildren().add(noDataLabel);
            } else {
                graphe1.getChildren().clear();
                graphe1.getChildren().add(lineChart1);
            }

            // --- Second Chart: Exam Results as PieChart with Percentage Labels ---
            PieChart pieChart2 = new PieChart();
            pieChart2.setTitle("Répartition des résultats d'examens");

            List<PasserExamen> allExamens = PasserExamenDAO.findAll();
            long reussiCount = allExamens.stream()
                    .filter(e -> "RÉUSSI".equalsIgnoreCase(e.getResultatExamen()))
                    .count();
            long echoueCount = allExamens.stream()
                    .filter(e -> "éCHOUÉ".equalsIgnoreCase(e.getResultatExamen()))
                    .count();
            long enAttenteCount = allExamens.stream()
                    .filter(e -> "EN ATTENTE".equalsIgnoreCase(e.getResultatExamen()))
                    .count();
            long totalExamens = allExamens.size();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            if (totalExamens > 0) {
                pieChartData.add(new PieChart.Data("Réussi", reussiCount));
                pieChartData.add(new PieChart.Data("Échoué", echoueCount));
                pieChartData.add(new PieChart.Data("En Attente", enAttenteCount));
            } else {
                pieChartData.add(new PieChart.Data("Aucune donnée", 100));
            }

            pieChart2.setData(pieChartData);
            if (totalExamens > 0) {
                for (PieChart.Data data : pieChart2.getData()) {
                    double percentage = (data.getPieValue() / totalExamens) * 100;
                    data.setName(String.format("%s: %.1f%%", data.getName(), percentage));
                }
            }

            pieChart2.setLabelsVisible(true);
            pieChart2.setLabelLineLength(10);
            pieChart2.setLegendSide(Side.BOTTOM);
            pieChart2.setStartAngle(90);
            pieChart2.getStylesheets().add(getClass().getResource("graphe2.css").toExternalForm());

            if (totalExamens == 0) {
                Label noDataLabel = new Label("Aucune donnée d'examen disponible");
                graphe2.getChildren().clear();
                graphe2.getChildren().add(noDataLabel);
            } else {
                graphe2.getChildren().clear();
                graphe2.getChildren().add(pieChart2);
            }

            // --- Third Chart: Payment Status of Candidates as PieChart ---
            if (graphe3 == null) {
                System.err.println("Error: graphe3 StackPane is null. Check fx:id in dashboard.fxml.");
                return;
            }
            PieChart pieChart3 = new PieChart();
            pieChart3.setTitle("Répartition des statuts de paiement des candidats");

            List<Candidat> allCandidats = CandidatDAO.getAllCandidats();
            long fullyPaidCount = 0;
            long notPaidCount = 0;
            long partiallyPaidCount = 0;
            long totalCandidatss = 0;
            for (Candidat candidat : allCandidats) {
                double totalPaid = candidat.getMontant_paye();
                double totalRequested = candidat.getMontant_total();
                if (totalPaid >= totalRequested && totalRequested > 0.0 && totalPaid > 0.0) {
                    fullyPaidCount++;
                    totalCandidatss++;
                } else if (totalPaid == 0.0 && totalRequested > 0.0) {
                    notPaidCount++;
                    totalCandidatss++;
                } else if (totalPaid > 0 && totalPaid < totalRequested) {
                    partiallyPaidCount++;
                    totalCandidatss++;
                }
            }

            ObservableList<PieChart.Data> pieChartData3 = FXCollections.observableArrayList();
            if (totalCandidatss > 0) {
                pieChartData3.add(new PieChart.Data("Payé complètement", fullyPaidCount));
                pieChartData3.add(new PieChart.Data("Non payé", notPaidCount));
                pieChartData3.add(new PieChart.Data("Payé partiellement", partiallyPaidCount));
            } else {
                pieChartData3.add(new PieChart.Data("Aucune donnée", 100));
            }

            pieChart3.setData(pieChartData3);
            if (totalCandidatss > 0) {
                for (PieChart.Data data : pieChart3.getData()) {
                    double percentage = (data.getPieValue() / totalCandidatss) * 100;
                    data.setName(String.format("%s: %.1f%%", data.getName(), percentage));
                }
            }

            pieChart3.setLabelsVisible(true);
            pieChart3.setLabelLineLength(10);
            pieChart3.setLegendSide(Side.BOTTOM);
            pieChart3.setStartAngle(90);
            pieChart3.getStylesheets().add(getClass().getResource("graphe3.css") != null
                    ? getClass().getResource("graphe3.css").toExternalForm()
                    : getClass().getResource("graphe2.css").toExternalForm());

            if (totalCandidatss == 0) {
                Label noDataLabel = new Label("Aucune donnée de candidats disponible");
                graphe3.getChildren().clear();
                graphe3.getChildren().add(noDataLabel);
            } else {
                graphe3.getChildren().clear();
                graphe3.getChildren().add(pieChart3);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation des graphiques: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des données");
            if (graphe1 != null) graphe1.getChildren().clear();
            if (graphe1 != null) graphe1.getChildren().add(errorLabel);
            if (graphe2 != null) graphe2.getChildren().clear();
            if (graphe2 != null) graphe2.getChildren().add(errorLabel);
            if (graphe3 != null) graphe3.getChildren().clear();
            if (graphe3 != null) graphe3.getChildren().add(errorLabel);
        }
    }


    @FXML
    private void pluprocheVisitetechniqueetsonvehiculeetnombredejour() {
        try {
            List<DocumentVehicule> documents = VehiculeService.getAllDocuments();
            if (documents == null || documents.isEmpty()) {
                updateLabelsWithNoVisit();
                return;
            }

            LocalDate today = LocalDate.now();
            final int VISITE_TECHNIQUE_TYPE = 2;

            DocumentVehicule prochaineVisite = null;
            for (DocumentVehicule d : documents) {
                if (d.getIdTypeDocument() == VISITE_TECHNIQUE_TYPE &&
                        d.getDateEcheance() != null &&
                        !d.getDateEcheance().isBefore(today)) {
                    if (prochaineVisite == null ||
                            d.getDateEcheance().isBefore(prochaineVisite.getDateEcheance())) {
                        prochaineVisite = d;
                    }
                }
            }

            if (prochaineVisite == null) {
                updateLabelsWithNoVisit();
                return;
            }

            long daysUntilVisit = ChronoUnit.DAYS.between(today, prochaineVisite.getDateEcheance());
            Vehicule vehicule = DashboardService.findVehiculeById(prochaineVisite.getIdVehicule());
            if (vehicule == null) {
                updateLabelsWithError("Véhicule introuvable pour ID: ", String.valueOf(prochaineVisite.getIdVehicule()));
                return;
            }
            String vehiculeInfo = vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")";

            if (visiteTechnique != null) {
                visiteTechnique.setText(daysUntilVisit + " jour" + (daysUntilVisit != 1 ? "s" : ""));
                if (daysUntilVisit < 3) {
                    visiteTechnique.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2);\n" +
                            "    -fx-text-fill: #EF4444;\n" +
                            "    -fx-border-color: rgba(239, 68, 68, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else if (daysUntilVisit <= 7) {
                    visiteTechnique.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2);\n" +
                            "    -fx-text-fill: #F59E0B;\n" +
                            "    -fx-border-color: rgba(245, 158, 11, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else {
                    visiteTechnique.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2);\n" +
                            "    -fx-text-fill: #10B981;\n" +
                            "    -fx-border-color: rgba(16, 185, 129, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                }
            } else {
                showErrorAlert("Erreur", "visiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            if (VehiculeVisiteTechnique != null) {
                VehiculeVisiteTechnique.setText(vehiculeInfo);
                if (daysUntilVisit < 3) {
                    VehiculeVisiteTechnique.setStyle("-fx-text-fill: red;");
                } else if (daysUntilVisit <= 7) {
                    VehiculeVisiteTechnique.setStyle("-fx-text-fill: orange;");
                } else {
                    VehiculeVisiteTechnique.setStyle("-fx-text-fill: green;");
                }
            } else {
                showErrorAlert("Erreur", "VehiculeVisiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

        } catch (Exception e) {
            updateLabelsWithError("Erreur: ", e.getMessage());
        }
    }

    private void updateLabelsWithNoVisit() {
        if (visiteTechnique != null) {
            visiteTechnique.setText("Aucune visite prévue");
            visiteTechnique.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "visiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeVisiteTechnique != null) {
            VehiculeVisiteTechnique.setText("N/A");
            VehiculeVisiteTechnique.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeVisiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }
    }

    private void updateLabelsWithError(String title, String message) {
        if (visiteTechnique != null) {
            visiteTechnique.setText("Erreur de chargement");
            visiteTechnique.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "visiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeVisiteTechnique != null) {
            VehiculeVisiteTechnique.setText("N/A");
            VehiculeVisiteTechnique.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeVisiteTechnique Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        showErrorAlert(title, message);
    }

    @FXML
    private void pluprocheAssuranceEtSonVehiculeEtNombreDeJour() {
        try {
            List<DocumentVehicule> documents = VehiculeService.getAllDocuments();
            if (documents == null || documents.isEmpty()) {
                updateLabelsWithNoAssurance();
                return;
            }

            LocalDate today = LocalDate.now();
            final int ASSURANCE_TYPE = 3;

            DocumentVehicule prochaineAssurance = null;
            for (DocumentVehicule d : documents) {
                if (d.getIdTypeDocument() == ASSURANCE_TYPE &&
                        d.getDateEcheance() != null &&
                        !d.getDateEcheance().isBefore(today)) {
                    if (prochaineAssurance == null ||
                            d.getDateEcheance().isBefore(prochaineAssurance.getDateEcheance())) {
                        prochaineAssurance = d;
                    }
                }
            }

            if (prochaineAssurance == null) {
                updateLabelsWithNoAssurance();
                return;
            }

            long daysUntilAssurance = ChronoUnit.DAYS.between(today, prochaineAssurance.getDateEcheance());
            Vehicule vehicule = DashboardService.findVehiculeById(prochaineAssurance.getIdVehicule());
            if (vehicule == null) {
                updateLabelsWithErrorAssurance("Véhicule introuvable pour ID: " + prochaineAssurance.getIdVehicule());
                return;
            }
            String vehiculeInfo = vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")";

            if (Assurance != null) {
                Assurance.setText(daysUntilAssurance + " jour" + (daysUntilAssurance != 1 ? "s" : ""));
                if (daysUntilAssurance < 3) {
                    Assurance.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2);\n" +
                            "    -fx-text-fill: #EF4444;\n" +
                            "    -fx-border-color: rgba(239, 68, 68, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else if (daysUntilAssurance <= 7) {
                    Assurance.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2);\n" +
                            "    -fx-text-fill: #F59E0B;\n" +
                            "    -fx-border-color: rgba(245, 158, 11, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else {
                    Assurance.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2);\n" +
                            "    -fx-text-fill: #10B981;\n" +
                            "    -fx-border-color: rgba(16, 185, 129, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                }
            } else {
                showErrorAlert("Erreur", "assurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            if (VehiculeAssurance != null) {
                VehiculeAssurance.setText(vehiculeInfo);
                if (daysUntilAssurance < 3) {
                    VehiculeAssurance.setStyle("-fx-text-fill: red;");
                } else if (daysUntilAssurance <= 7) {
                    VehiculeAssurance.setStyle("-fx-text-fill: orange;");
                } else {
                    VehiculeAssurance.setStyle("-fx-text-fill: green;");
                }
            } else {
                showErrorAlert("Erreur", "VehiculeAssurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

        } catch (Exception e) {
            updateLabelsWithErrorAssurance("Erreur: " + e.getMessage());
        }
    }

    private void updateLabelsWithNoAssurance() {
        if (Assurance != null) {
            Assurance.setText("Aucune assurance prévue");
            Assurance.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "assurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeAssurance != null) {
            VehiculeAssurance.setText("N/A");
            VehiculeAssurance.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeAssurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }
    }

    private void updateLabelsWithErrorAssurance(String message) {
        if (Assurance != null) {
            Assurance.setText("Erreur de chargement");
            Assurance.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "assurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeAssurance != null) {
            VehiculeAssurance.setText("N/A");
            VehiculeAssurance.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeAssurance Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        showErrorAlert("Erreur", message);
    }

    @FXML
    private void pluprocheVignetteEtSonVehiculeEtNombreDeJour() {
        try {
            List<DocumentVehicule> documents = VehiculeService.getAllDocuments();
            if (documents == null || documents.isEmpty()) {
                updateLabelsWithNoVignette();
                return;
            }

            LocalDate today = LocalDate.now();
            final int VIGNETTE_TYPE = 1;

            DocumentVehicule prochaineVignette = null;
            for (DocumentVehicule d : documents) {
                if (d.getIdTypeDocument() == VIGNETTE_TYPE &&
                        d.getDateEcheance() != null &&
                        !d.getDateEcheance().isBefore(today)) {
                    if (prochaineVignette == null ||
                            d.getDateEcheance().isBefore(prochaineVignette.getDateEcheance())) {
                        prochaineVignette = d;
                    }
                }
            }

            if (prochaineVignette == null) {
                updateLabelsWithNoVignette();
                return;
            }

            long daysUntilVignette = ChronoUnit.DAYS.between(today, prochaineVignette.getDateEcheance());
            Vehicule vehicule = DashboardService.findVehiculeById(prochaineVignette.getIdVehicule());
            if (vehicule == null) {
                updateLabelsWithErrorVignette("Véhicule introuvable pour ID: " + prochaineVignette.getIdVehicule());
                return;
            }
            String vehiculeInfo = vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")";

            if (VignetteFiscale != null) {
                VignetteFiscale.setText(daysUntilVignette + " jour" + (daysUntilVignette != 1 ? "s" : ""));
                if (daysUntilVignette < 3) {
                    VignetteFiscale.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2);\n" +
                            "    -fx-text-fill: #EF4444;\n" +
                            "    -fx-border-color: rgba(239, 68, 68, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else if (daysUntilVignette <= 7) {
                    VignetteFiscale.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2);\n" +
                            "    -fx-text-fill: #F59E0B;\n" +
                            "    -fx-border-color: rgba(245, 158, 11, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                } else {
                    VignetteFiscale.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2);\n" +
                            "    -fx-text-fill: #10B981;\n" +
                            "    -fx-border-color: rgba(16, 185, 129, 0.3);\n" +
                            "    -fx-border-radius: 16px;\n" +
                            "    -fx-border-width: 1px;");
                }
            } else {
                showErrorAlert("Erreur", "VignetteFiscale Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

            if (VehiculeVignette != null) {
                VehiculeVignette.setText(vehiculeInfo);
                if (daysUntilVignette < 3) {
                    VehiculeVignette.setStyle("-fx-text-fill: red;");
                } else if (daysUntilVignette <= 7) {
                    VehiculeVignette.setStyle("-fx-text-fill: orange;");
                } else {
                    VehiculeVignette.setStyle("-fx-text-fill: green;");
                }
            } else {
                showErrorAlert("Erreur", "VehiculeVignette Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
            }

        } catch (Exception e) {
            updateLabelsWithErrorVignette("Erreur: " + e.getMessage());
        }
    }

    private void updateLabelsWithNoVignette() {
        if (VignetteFiscale != null) {
            VignetteFiscale.setText("Aucune vignette prévue");
            VignetteFiscale.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VignetteFiscale Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeVignette != null) {
            VehiculeVignette.setText("N/A");
            VehiculeVignette.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeVignette Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }
    }

    private void updateLabelsWithErrorVignette(String message) {
        if (VignetteFiscale != null) {
            VignetteFiscale.setText("Erreur de chargement");
            VignetteFiscale.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VignetteFiscale Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        if (VehiculeVignette != null) {
            VehiculeVignette.setText("N/A");
            VehiculeVignette.setStyle("-fx-text-fill: black;");
        } else {
            showErrorAlert("Erreur", "VehiculeVignette Label introuvable. Vérifiez fx:id dans dashboard.fxml.");
        }

        showErrorAlert("Erreur", message);
    }
}