package org.example;

import Persistance.models.NotificationVehicule;
import Persistance.models.Vehicule;
import Service.NotificationService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.InputStream;

import static Persistance.utils.Alert.*;


public class NotificationController {
    private Stage dialogStage;
    private ObservableList<NotificationVehicule> notifications;
    private MenuController menuController;

    @FXML private TableView<NotificationVehicule> notificationsTable;
    @FXML private TableColumn<NotificationVehicule, String> vehiculeColumn;
    @FXML private TableColumn<NotificationVehicule, String> typeColumn;
    @FXML private TableColumn<NotificationVehicule, String> dateEcheanceColumn;
    @FXML private TableColumn<NotificationVehicule, Boolean> statutColumn;
    @FXML private TableColumn<NotificationVehicule, NotificationVehicule> actionColumn;
    @FXML private Button closeButton;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML
    private void initialize() {
        if (notificationsTable == null || vehiculeColumn == null || typeColumn == null ||
                dateEcheanceColumn == null || statutColumn == null || actionColumn == null || closeButton == null) {
            showErrorAlert("Erreur", "Une ou plusieurs injections FXML sont null - Vérifiez notification.fxml");
            return;
        }

        configureTableColumns();
        loadNotifications();
    }

    private void configureTableColumns() {
        vehiculeColumn.setCellValueFactory(cellData -> {
            int idVehicule = cellData.getValue().getIdVehicule();
            Vehicule vehicule = NotificationService.getVehicule(idVehicule);
            return new SimpleStringProperty(vehicule != null ? vehicule.getImmatriculation() : "Inconnu");
        });

        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        dateEcheanceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        statutColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isEtat()));
        statutColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Lu" : "Non lu"));
            }
        });

        actionColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button markAsReadButton = createIconButton("/images/markAsRead.png");
            private final Button deleteButton = createIconButton("/images/delete.png");
            private final HBox actionBox;

            {
                markAsReadButton.setTooltip(new Tooltip("Marquer comme lu"));
                deleteButton.setTooltip(new Tooltip("Supprimer"));
                actionBox = new HBox(10, markAsReadButton, deleteButton);
                actionBox.setAlignment(javafx.geometry.Pos.CENTER);

                markAsReadButton.setOnAction(event -> {
                    NotificationVehicule notification = getTableView().getItems().get(getIndex());
                    handleMarkAsRead(notification);
                });

                deleteButton.setOnAction(event -> {
                    NotificationVehicule notification = getTableView().getItems().get(getIndex());
                    handleDelete(notification);
                });
            }

            @Override
            protected void updateItem(NotificationVehicule notification, boolean empty) {
                super.updateItem(notification, empty);
                if (empty || notification == null) {
                    setGraphic(null);
                } else {
                    markAsReadButton.setDisable(notification.isEtat());
                    setGraphic(actionBox);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
    }

    private Button createIconButton(String imagePath) {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            Button button = new Button(imagePath.contains("markAsRead") ? "Lu" : "X");
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4444;");
            return button;
        }

        ImageView icon = new ImageView(new Image(imageStream));
        icon.setFitHeight(28);
        icon.setFitWidth(28);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private void loadNotifications() {
        try {
            notifications = FXCollections.observableArrayList(NotificationService.getNotifications());
            notificationsTable.setItems(notifications);
            if (notifications.isEmpty()) {
                notificationsTable.setPlaceholder(new Label("Aucune notification disponible"));
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les notifications : " + e.getMessage());
            notifications = FXCollections.observableArrayList();
            notificationsTable.setItems(notifications);
        }
    }

    private void handleMarkAsRead(NotificationVehicule notification) {
        if (notification == null) {
            showInformationAlert("Information", "Aucune notification sélectionnée.");
            return;
        }
        if (notification.isEtat()) {
            showInformationAlert("Information", "Cette notification est déjà marquée comme lue.");
            return;
        }

        try {
            NotificationService.markNotificationAsRead(notification.getIdNotification());
            notification.setEtat(true);
            notificationsTable.refresh();
            showSuccessAlert("Succès", "Notification marquée comme lue avec succès !");
            if (menuController != null) {
                menuController.checkForNotifications();
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la mise à jour de la notification : " + e.getMessage());
        }
    }

    private void handleDelete(NotificationVehicule notification) {
        if (notification == null) {
            showInformationAlert("Information", "Aucune notification sélectionnée.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Supprimer la notification");
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette notification ?");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    NotificationService.deleteNotification(notification.getIdNotification());
                    notifications.remove(notification);
                    showSuccessAlert("Succès", "Notification supprimée avec succès !");
                    if (menuController != null) {
                        menuController.checkForNotifications();
                    }
                } catch (Exception e) {
                    showErrorAlert("Erreur", "Erreur lors de la suppression de la notification : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void close() {
        if (dialogStage != null) {
            dialogStage.close();
        }
        if (menuController != null) {
            menuController.checkForNotifications();
        }
    }
}