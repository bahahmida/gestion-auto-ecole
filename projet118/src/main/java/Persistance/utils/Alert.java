package Persistance.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Alert {

    public static void showSuccessAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("success-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #2E7D32;"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("error-dialog");
        alert.getDialogPane().setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-weight: bold;");
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle("-fx-background-color: #CC0000; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #AA0000;"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #CC0000; -fx-text-fill: white; -fx-font-weight: bold;"));
        });
        alert.showAndWait();
    }
    public static void showInformationAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("info-dialog");
        alert.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1e5b9c, #134673); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold;"
        );
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle(
                    "-fx-background-color: #1565C0; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 5px;"
            );
            button.setOnMouseEntered(event ->
                    button.setStyle(
                            "-fx-background-color: #0D47A1; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5px;"
                    )
            );
            button.setOnMouseExited(event ->
                    button.setStyle(
                            "-fx-background-color: #1565C0; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5px;"
                    )
            );
        });
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmationAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("confirmation-dialog");
        alert.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1e5b9c, #134673); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold;"
        );
        alert.getButtonTypes().forEach(type -> {
            Button button = (Button) alert.getDialogPane().lookupButton(type);
            button.setStyle(
                    "-fx-background-color: #1565C0; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 5px;"
            );
            button.setOnMouseEntered(event ->
                    button.setStyle(
                            "-fx-background-color: #0D47A1; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5px;"
                    )
            );
            button.setOnMouseExited(event ->
                    button.setStyle(
                            "-fx-background-color: #1565C0; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5px;"
                    )
            );
        });
        return alert.showAndWait();
    }
}
