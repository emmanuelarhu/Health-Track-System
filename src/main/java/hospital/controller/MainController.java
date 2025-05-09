package main.java.hospital.controller;

import main.java.hospital.util.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the main view of the application.
 * Handles navigation between different modules.
 */
public class MainController implements Initializable {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    @FXML private TabPane tabPane;
    @FXML private Tab dashboardTab;
    @FXML private Label statusLabel;

    private final Map<String, Tab> openTabs = new HashMap<>();

    /**
     * Initializes the controller.
     *
     * @param location  The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing MainController");
        setStatus("HealthTrack System started successfully");
    }

    /**
     * Sets the status message in the status bar.
     *
     * @param message The status message to display
     */
    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Handles the exit action.
     * Closes the application after confirmation.
     */
    @FXML
    private void handleExitAction(ActionEvent event) {
        boolean confirm = AlertUtils.showConfirmation("Confirm Exit",
                "Exit Application", "Are you sure you want to exit the application?");

        if (confirm) {
            logger.info("Application exit requested by user");
            Platform.exit();
        }
    }

    /**
     * Handles the about action.
     * Displays information about the application.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        AlertUtils.showInformation("About", "HealthTrack System",
                "HealthTrack System\nVersion 1.0\n© 2025\n\n" +
                        "A comprehensive system for managing hospital data\n" +
                        "including patients, staff, departments, and hospitalizations.");
    }

    /**
     * Opens the patients module.
     */
    @FXML
    private void handlePatientsAction(ActionEvent event) {
        openTab("Patient Management", "view/PatientView.fxml");
    }

    /**
     * Opens the doctors module.
     */
    @FXML
    private void handleDoctorsAction(ActionEvent event) {
        openTab("Doctor Management", "view/DoctorView.fxml");
    }

    /**
     * Opens the nurses module.
     */
    @FXML
    private void handleNursesAction(ActionEvent event) {
        openTab("Nurse Management", "view/NurseView.fxml");
    }

    /**
     * Opens the departments module.
     */
    @FXML
    private void handleDepartmentsAction(ActionEvent event) {
        // For demonstration, show not implemented yet
        openTab("Department Management", "view/DepartmentView.fxml");
//        AlertUtils.showInformation("Under Development", "Departments Management",
//                "This module is currently under development and will be available soon.");
    }

    /**
     * Opens the wards module.
     */
    @FXML
    private void handleWardsAction(ActionEvent event) {
        // For demonstration, show not implemented yet
        openTab("Ward Management", "view/WardView.fxml");
//        AlertUtils.showInformation("Under Development", "Wards Management",
//                "This module is currently under development and will be available soon.");
    }

    /**
     * Opens the hospitalizations module.
     */
    @FXML
    private void handleHospitalizationsAction(ActionEvent event) {
        // For demonstration, show not implemented yet
        openTab("Hospitalization Management", "view/HospitalizationView.fxml");
//        AlertUtils.showInformation("Under Development", "Hospitalizations Management",
//                "This module is currently under development and will be available soon.");
    }

    /**
     * Opens the patient reports module.
     */
    @FXML
    private void handlePatientReportsAction(ActionEvent event) {
        // For demonstration, show not implemented yet
        openTab("Patient Reports", "view/PatientReportView.fxml");
//        AlertUtils.showInformation("Under Development", "Patient Reports",
//                "This module is currently under development and will be available soon.");
    }

    /**
     * Opens the staff reports module.
     */
    @FXML
    private void handleStaffReportsAction(ActionEvent event) {
        // For demonstration, show not implemented yet
        openTab("Staff Reports", "view/StaffReportView.fxml");
//        AlertUtils.showInformation("Under Development", "Staff Reports",
//                "This module is currently under development and will be available soon.");
    }

    /**
     * Opens a new tab with the specified title and content.
     * If a tab with the same title already exists, it is brought to the front.
     *
     * @param title       The title of the tab
     * @param contentPath The path to the FXML file for the tab content
     */
    private void openTab(String title, String contentPath) {
        try {
            // Check if tab already exists
            if (openTabs.containsKey(title)) {
                // Select existing tab
                tabPane.getSelectionModel().select(openTabs.get(title));
                logger.info("Selected existing tab: {}", title);
                return;
            }

            // Load the content with correct ClassLoader approach
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(contentPath));
            BorderPane content = loader.load();

            // Create a new tab
            Tab tab = new Tab(title);
            tab.setContent(content);
            tab.setClosable(true);

            // Add close handler
            tab.setOnClosed(e -> {
                openTabs.remove(title);
                logger.info("Closed tab: {}", title);
            });

            // Add to tab pane and map
            tabPane.getTabs().add(tab);
            openTabs.put(title, tab);

            // Select the new tab
            tabPane.getSelectionModel().select(tab);

            logger.info("Opened new tab: {}", title);
            setStatus("Opened " + title);
        } catch (IOException e) {
            logger.error("Error loading tab content: {}", contentPath, e);
            AlertUtils.showError("Error", "Failed to Load Module",
                    "An error occurred while loading the " + title + " module: " + e.getMessage());
        }
    }
}