package main.java.hospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Main application class for the HealthTrack System.
 */
public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main view
            // Changed path to match the package structure
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root, 1024, 768);
            // Changed path to match the package structure
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/main/resources/css/application.css")).toExternalForm());

            // Configure the stage
            primaryStage.setTitle("HealthTrack System");
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Clean up resources when the application is closing
        logger.info("Application stopping");
    }

    /**
     * Main method to launch the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}