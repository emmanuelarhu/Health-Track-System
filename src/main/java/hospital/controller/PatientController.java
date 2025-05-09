package main.java.hospital.controller;

import main.java.hospital.dao.PatientDAO;
import main.java.hospital.model.Patient;
import main.java.hospital.util.AlertUtils;
import main.java.hospital.util.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Patient view.
 * Handles all UI interactions related to patient management.
 */
public class PatientController implements Initializable {
    private static final Logger logger = LogManager.getLogger(PatientController.class);

    private final PatientDAO patientDAO = new PatientDAO();
    private ObservableList<Patient> patientList;
    private FilteredList<Patient> filteredPatients;

    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, Integer> colPatientId;
    @FXML private TableColumn<Patient, String> colFirstName;
    @FXML private TableColumn<Patient, String> colSurname;
    @FXML private TableColumn<Patient, String> colAddress;
    @FXML private TableColumn<Patient, String> colPhone;

    @FXML private TextField txtPatientId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSearch;
    @FXML private Label statusLabel;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    /**
     * Initializes the controller.
     *
     * @param location  The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing PatientController");

        // Initialize table columns
        colPatientId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Load patient data
        loadPatientData();

        // Set table selection listener
        tablePatients.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPatientDetails(newValue));

        // Disable patient ID field (auto-generated)
        txtPatientId.setEditable(false);

        // Initialize button states
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Set up search functionality
        setupSearch();

        setStatus("Patient management module loaded");
    }

    /**
     * Set status message
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Sets up the search functionality
     */
    private void setupSearch() {
        filteredPatients = new FilteredList<>(patientList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPatients.setPredicate(patient -> {
                // If search text is empty, show all patients
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (patient.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name
                } else if (patient.getSurname().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches surname
                } else if (patient.getPatientId().toString().contains(lowerCaseFilter)) {
                    return true; // Filter matches ID
                }
                return false; // No match
            });

            tablePatients.setItems(filteredPatients);
        });
    }

    /**
     * Handles the search action
     */
    @FXML
    private void handleSearchAction(ActionEvent event) {
        // Search is handled by the listener in setupSearch
        setStatus("Searching for: " + txtSearch.getText());
    }

    /**
     * Handles the clear search action
     */
    @FXML
    private void handleClearSearchAction(ActionEvent event) {
        txtSearch.clear();
        tablePatients.setItems(patientList);
        setStatus("Search cleared");
    }

    /**
     * Loads all patient data from the database and displays it in the table.
     */
    private void loadPatientData() {
        try {
            List<Patient> patients = patientDAO.findAll();
            patientList = FXCollections.observableArrayList(patients);
            tablePatients.setItems(patientList);
            logger.info("Loaded {} patients from database", patients.size());
        } catch (Exception e) {
            logger.error("Error loading patient data", e);
            AlertUtils.showError("Database Error", "Failed to load patient data", e.getMessage());
        }
    }

    /**
     * Displays the details of the selected patient in the form fields.
     *
     * @param patient The selected patient
     */
    private void showPatientDetails(Patient patient) {
        if (patient != null) {
            txtPatientId.setText(patient.getPatientId().toString());
            txtFirstName.setText(patient.getFirstName());
            txtSurname.setText(patient.getSurname());
            txtAddress.setText(patient.getAddress());
            txtPhone.setText(patient.getPhone());

            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);
        } else {
            clearFields();
        }
    }

    /**
     * Clears all form fields and resets button states.
     */
    @FXML
    private void handleClearAction(ActionEvent event) {
        clearFields();
    }

    /**
     * Helper method to clear form fields and reset button states.
     */
    private void clearFields() {
        txtPatientId.clear();
        txtFirstName.clear();
        txtSurname.clear();
        txtAddress.clear();
        txtPhone.clear();

        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tablePatients.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new patient to the database.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            Patient patient = new Patient(
                    txtFirstName.getText(),
                    txtSurname.getText(),
                    txtAddress.getText(),
                    txtPhone.getText()
            );

            try {
                Optional<Integer> patientId = patientDAO.insert(patient);

                if (patientId.isPresent()) {
                    AlertUtils.showInformation("Success", "Patient Saved",
                            "Patient was successfully saved with ID: " + patientId.get());
                    loadPatientData();
                    clearFields();
                    setStatus("Patient saved successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Save Patient",
                            "An error occurred while saving the patient.");
                    setStatus("Failed to save patient");
                }
            } catch (Exception e) {
                logger.error("Error saving patient", e);
                AlertUtils.showError("Database Error", "Failed to save patient", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing patient in the database.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (validateInput()) {
            try {
                int patientId = Integer.parseInt(txtPatientId.getText());

                Patient patient = new Patient(
                        patientId,
                        txtFirstName.getText(),
                        txtSurname.getText(),
                        txtAddress.getText(),
                        txtPhone.getText(),
                        null, null
                );

                boolean success = patientDAO.update(patient);

                if (success) {
                    AlertUtils.showInformation("Success", "Patient Updated",
                            "Patient information was successfully updated.");

                    // Update the item in the list directly
                    for (int i = 0; i < patientList.size(); i++) {
                        if (patientList.get(i).getPatientId().equals(patient.getPatientId())) {
                            patientList.set(i, patient);
                            break;
                        }
                    }

                    // Refresh the table
                    tablePatients.refresh();

                    clearFields();
                    setStatus("Patient updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Patient",
                            "Patient with ID " + patientId + " was not found.");
                    setStatus("Failed to update patient");
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid patient ID format", e);
                AlertUtils.showError("Validation Error", "Invalid Patient ID",
                        "Please select a valid patient to update.");
                setStatus("Invalid patient ID");
            } catch (Exception e) {
                logger.error("Error updating patient", e);
                AlertUtils.showError("Database Error", "Failed to update patient", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the patient from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            int patientId = Integer.parseInt(txtPatientId.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Patient", "Are you sure you want to delete this patient?");

            if (confirm) {
                boolean success = patientDAO.delete(patientId);

                if (success) {
                    AlertUtils.showInformation("Success", "Patient Deleted",
                            "Patient was successfully deleted.");
                    loadPatientData();
                    clearFields();
                    setStatus("Patient deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Patient",
                            "Patient with ID " + patientId + " was not found.");
                    setStatus("Failed to delete patient");
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid patient ID format", e);
            AlertUtils.showError("Validation Error", "Invalid Patient ID",
                    "Please select a valid patient to delete.");
            setStatus("Invalid patient ID");
        } catch (Exception e) {
            logger.error("Error deleting patient", e);
            AlertUtils.showError("Database Error", "Failed to delete patient", e.getMessage());
            setStatus("Database error: " + e.getMessage());
        }
    }

    /**
     * Validates the input fields.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtFirstName.getText().trim().isEmpty()) {
            errorMessage.append("First name cannot be empty.\n");
        } else if (!ValidationUtils.isValidName(txtFirstName.getText())) {
            errorMessage.append("First name contains invalid characters.\n");
        }

        if (txtSurname.getText().trim().isEmpty()) {
            errorMessage.append("Surname cannot be empty.\n");
        } else if (!ValidationUtils.isValidName(txtSurname.getText())) {
            errorMessage.append("Surname contains invalid characters.\n");
        }

        if (txtAddress.getText().trim().isEmpty()) {
            errorMessage.append("Address cannot be empty.\n");
        }

        if (txtPhone.getText().trim().isEmpty()) {
            errorMessage.append("Phone number cannot be empty.\n");
        } else if (!ValidationUtils.isValidPhoneNumber(txtPhone.getText())) {
            errorMessage.append("Phone number is invalid. Use format: XXX-XXX-XXXX or similar.\n");
        }

        if (errorMessage.length() > 0) {
            AlertUtils.showError("Validation Error", "Please correct the following errors:",
                    errorMessage.toString());
            setStatus("Validation error");
            return false;
        }

        return true;
    }
}