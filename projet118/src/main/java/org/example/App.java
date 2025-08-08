package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**

 JavaFX App*/
public class App extends Application {

    private static Scene scene;
    private static FXMLLoader fxmlLoader;

    @Override
    public void start(Stage stage) throws IOException {
        // Charger la page de connexion (login.fxml) au démarrage
        fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        Parent root = fxmlLoader.load();

        // Créer la scène avec les dimensions souhaitées
        scene = new Scene(root, 950, 560);

        // Configurer et afficher la fenêtre
        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.setResizable(false); // Prevent resizing for the login window
        stage.setMaximized(false); // Ensure the login window is not maximized
        stage.centerOnScreen(); // Center the login window on the screen
        stage.show();
    }

    public static <T> T getController() {
        return fxmlLoader.getController();
    }

    static void setRoot(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
    }

    private static Parent loadFXML(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
