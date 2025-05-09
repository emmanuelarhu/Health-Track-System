package main.java.hospital.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Utility class for displaying various alert dialogs in the application.
 */
public class AlertUtils {
    private static final Logger logger = LogManager.getLogger(AlertUtils.class);

    /**
     * Displays an information alert dialog.
     *
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showInformation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        logger.info("Showing information alert: {}", header);
        alert.showAndWait();
    }

    /**
     * Displays an error alert dialog.
     *
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        logger.error("Showing error alert: {}", header);
        alert.showAndWait();
    }

    /**
     * Displays a warning alert dialog.
     *
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        logger.warn("Showing warning alert: {}", header);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation alert dialog and returns the user's choice.
     *
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        logger.info("Showing confirmation alert: {}", header);
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }
}