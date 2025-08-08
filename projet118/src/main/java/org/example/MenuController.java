package org.example;

import Persistance.dao.CandidatDAO;
import Persistance.dao.SeanceDAO;
import Persistance.models.Candidat;
import Persistance.models.Seance;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MenuController {

    @FXML private StackPane contentArea;
    @FXML private Button vehiclesBtn;
    @FXML private Button settingsBtn;
    @FXML private Button candidatesBtn;
    @FXML private Button moniteurBtn;
    @FXML private Button sessionsBtn;
    @FXML private Button examsBtn;
    @FXML private Button paymentsBtn;
    @FXML private Button dashboardBtn;
    @FXML private ImageView logoImage;

    private VehiculeController vehiculeController;

    @FXML
    private void initialize() {
        Button[] navButtons = {settingsBtn, candidatesBtn, moniteurBtn, vehiclesBtn, sessionsBtn, examsBtn, paymentsBtn, dashboardBtn};
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().add("nav-button");
            } else {
                System.out.println("Warning: A navigation button is null during initialization.");
            }
        }

        addShakeAnimationToLogo();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("vehicule.fxml"));
            loader.load();
            vehiculeController = loader.getController();
            vehiculeController.setMenuController(this);
            vehiculeController.insertNotificationsForDueDates();
            checkForNotifications();
        } catch (Exception e) {
            System.err.println("Error during MenuController initialization: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            List<Candidat> l = CandidatDAO.getAllCandidats();
            UpdateCandidatController ucc = new UpdateCandidatController();
            for (Candidat c : l) {
                List<Seance> s = SeanceDAO.getAllSeancesByCandidatId(c.getCin());
                List<Seance> se = SeanceDAO.getAllSeancesCodeByCandidatId(c.getCin());
                for (Seance ss : s) {
                    if (ss.getDateTime().equals(LocalDate.now()) || ss.getDateTime().toLocalDateTime().isBefore(LocalDateTime.now())) {
                        if (!SeanceDAO.rechercherSeanceEffectue(ss.getDateTime(), c.getCin())) {
                            ucc.updateSeancepasse(c.getCin());
                            SeanceDAO.insererSeanceEffectue(ss.getDateTime(), c.getCin());
                        }
                    }
                }
                for (Seance ss : se) {
                    if (ss.getDateTime().equals(LocalDate.now()) || ss.getDateTime().toLocalDateTime().isBefore(LocalDateTime.now())) {
                        if (!SeanceDAO.rechercherSeanceEffectue(ss.getDateTime(), c.getCin())) {
                            ucc.updateSeancepasse(c.getCin());
                            SeanceDAO.insererSeanceEffectue(ss.getDateTime(), c.getCin());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadDashboard();
        setActiveButton(dashboardBtn);
    }

    private void addShakeAnimationToLogo() {
        if (logoImage == null) {
            System.out.println("Warning: logoImage is null, cannot apply animation.");
            return;
        }

        Timeline timeline = new Timeline();
        KeyFrame shakeLeft = new KeyFrame(Duration.seconds(0.2),
                new KeyValue(logoImage.rotateProperty(), -15));
        KeyFrame shakeRight = new KeyFrame(Duration.seconds(0.4),
                new KeyValue(logoImage.rotateProperty(), 15));
        KeyFrame returnToCenter = new KeyFrame(Duration.seconds(0.6),
                new KeyValue(logoImage.rotateProperty(), 0));
        timeline.getKeyFrames().addAll(shakeLeft, shakeRight, returnToCenter);
        timeline.setCycleCount(Timeline.INDEFINITE);

        logoImage.setOnMouseEntered(event -> timeline.play());
        logoImage.setOnMouseExited(event -> {
            timeline.stop();
            logoImage.setRotate(0);
        });
    }

    // Make setActiveButton public so it can be called from DashboardController
    public void setActiveButton(Button activeButton) {
        Button[] navButtons = {settingsBtn, candidatesBtn, moniteurBtn, vehiclesBtn, sessionsBtn, examsBtn, paymentsBtn, dashboardBtn};
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-button-active");
            } else {
                System.out.println("Warning: A navigation button is null in setActiveButton.");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");
            System.out.println("Set active button: " + activeButton.getText());
        } else {
            System.out.println("Warning: Attempted to set a null button as active.");
        }
    }

    // Add getters for the buttons so DashboardController can access them
    public Button getCandidatesBtn() {
        return candidatesBtn;
    }

    public Button getPaymentsBtn() {
        return paymentsBtn;
    }

    public Button getMoniteurBtn() {
        return moniteurBtn;
    }

    public Button getVehiclesBtn() {
        return vehiclesBtn;
    }

    public Button getExamsBtn() {
        return examsBtn;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void checkForNotifications() {
        try {
            long unreadCount = Service.NotificationService.getNotifications().stream()
                    .filter(notification -> !notification.isEtat())
                    .count();

            if (unreadCount > 0 && vehiclesBtn != null) {
                Circle notificationIndicator = new Circle(6);
                notificationIndicator.setFill(javafx.scene.paint.Color.RED);
                notificationIndicator.setTranslateX(-3);
                notificationIndicator.setTranslateY(-5);

                StackPane container = new StackPane();
                container.getChildren().add(notificationIndicator);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                vehiclesBtn.setGraphic(container);


            } else if (vehiclesBtn != null) {
                vehiclesBtn.setGraphic(null);
                System.out.println("Removed notification indicator: no unread notifications");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error checking for notifications: " + e.getMessage());
        }
    }

    @FXML
    private void loadSettings() {
        System.out.println("Loading Settings view...");
        loadView("gestionInfo.fxml");
        setActiveButton(settingsBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    void loadCandidates() {
        System.out.println("Loading Candidates view...");
        loadView("candidat.fxml");
        setActiveButton(candidatesBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene loginScene = new Scene(loginPage, 930, 560);
            stage.setScene(loginScene);
            stage.setTitle("Connexion Auto-École");
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Déconnexion réussie : page login.fxml chargée.");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de login.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void loadMoniteur() {
        System.out.println("Loading Moniteur view...");
        loadView("moniteur.fxml");
        setActiveButton(moniteurBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    void loadVehicles() {
        System.out.println("Loading Vehicles view...");
        loadView("vehicule.fxml");
        setActiveButton(vehiclesBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    private void loadSessions() {
        System.out.println("Loading Sessions view...");
        loadView("seance.fxml");
        setActiveButton(sessionsBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    public void loadExams() {
        System.out.println("Loading Exams view...");
        loadView("PasserExamen.fxml");
        setActiveButton(examsBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    void loadPayments() {
        System.out.println("Loading Payments view...");
        loadView("paiement.fxml");
        setActiveButton(paymentsBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    @FXML
    private void loadDashboard() {
        System.out.println("Loading Dashboard view...");
        loadView("dashboard.fxml");
        setActiveButton(dashboardBtn);
        VehiculeController vehiculeController = new VehiculeController();
        vehiculeController.insertNotificationsForDueDates();
        checkForNotifications();
    }

    void loadView(String fxmlFile) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof GestionInfoController) {
                GestionInfoController primaryController = (GestionInfoController) controller;
                primaryController.setMenuController(this);
            } else if (controller instanceof MoniteurController) {
                MoniteurController secondaryController = (MoniteurController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof AddMoniteurController) {
                AddMoniteurController secondaryController = (AddMoniteurController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof PasserExamenController) {
                PasserExamenController secondaryController = (PasserExamenController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof CandidatController) {
                CandidatController secondaryController = (CandidatController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof AddCandidatController) {
                AddCandidatController secondaryController = (AddCandidatController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof VehiculeController) {
                VehiculeController secondaryController = (VehiculeController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof AddVehiculeController) {
                AddVehiculeController secondaryController = (AddVehiculeController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof PaiementController) {
                PaiementController secondaryController = (PaiementController) controller;
                secondaryController.setMenuController(this);
            } else if (controller instanceof DashboardController) {
                DashboardController dashboardController = (DashboardController) controller;
                dashboardController.setMenuController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue : " + fxmlFile);
            System.err.println("Message d'erreur : " + e.getMessage());
        }
    }
}