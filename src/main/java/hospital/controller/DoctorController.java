package main.java.hospital.controller;

import main.java.hospital.dao.DoctorDAO;
import main.java.hospital.dao.EmployeeDAO;
import main.java.hospital.model.Doctor;
import main.java.hospital.model.Employee;
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
 * Controller for the Doctor view.
 * Handles all UI interactions related to doctor management.
 */
public class DoctorController implements Initializable {
    private static final Logger logger = LogManager.getLogger(DoctorController.class);

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private ObservableList<Doctor> doctorList;
    private FilteredList<Doctor> filteredDoctors;

    @FXML private TableView<Doctor> tableDoctors;
    @FXML private TableColumn<Doctor, Integer> colEmployeeId;
    @FXML private TableColumn<Doctor, String> colFirstName;
    @FXML private TableColumn<Doctor, String> colSurname;
    @FXML private TableColumn<Doctor, String> colSpeciality;
    @FXML private TableColumn<Doctor, String> colPhone;

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSpeciality;
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
        logger.info("Initializing DoctorController");

        // Initialize table columns
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colSpeciality.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Load doctor data
        loadDoctorData();

        // Set table selection listener
        tableDoctors.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDoctorDetails(newValue));

        // Disable employee ID field (auto-generated)
        txtEmployeeId.setEditable(false);

        // Initialize button states
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Set up search functionality
        setupSearch();

        setStatus("Doctor management module loaded");
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
        filteredDoctors = new FilteredList<>(doctorList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDoctors.setPredicate(doctor -> {
                // If search text is empty, show all doctors
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (doctor.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name
                } else if (doctor.getSurname().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches surname
                } else if (doctor.getSpeciality().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches speciality
                } else if (doctor.getEmployeeId().toString().contains(lowerCaseFilter)) {
                    return true; // Filter matches ID
                }
                return false; // No match
            });

            tableDoctors.setItems(filteredDoctors);
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
        tableDoctors.setItems(doctorList);
        setStatus("Search cleared");
    }

    /**
     * Loads all doctor data from the database and displays it in the table.
     */
    private void loadDoctorData() {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            doctorList = FXCollections.observableArrayList(doctors);
            tableDoctors.setItems(doctorList);
            logger.info("Loaded {} doctors from database", doctors.size());
        } catch (Exception e) {
            logger.error("Error loading doctor data", e);
            AlertUtils.showError("Database Error", "Failed to load doctor data", e.getMessage());
        }
    }

    /**
     * Displays the details of the selected doctor in the form fields.
     *
     * @param doctor The selected doctor
     */
    private void showDoctorDetails(Doctor doctor) {
        if (doctor != null) {
            txtEmployeeId.setText(doctor.getEmployeeId().toString());
            txtFirstName.setText(doctor.getFirstName());
            txtSurname.setText(doctor.getSurname());
            txtAddress.setText(doctor.getAddress());
            txtPhone.setText(doctor.getPhone());
            txtSpeciality.setText(doctor.getSpeciality());

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
        txtEmployeeId.clear();
        txtFirstName.clear();
        txtSurname.clear();
        txtAddress.clear();
        txtPhone.clear();
        txtSpeciality.clear();

        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tableDoctors.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new doctor to the database.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            try {
                // Create and save employee first
                Employee employee = new Employee(
                        txtFirstName.getText(),
                        txtSurname.getText(),
                        txtAddress.getText(),
                        txtPhone.getText()
                );

                Optional<Integer> employeeId = employeeDAO.insert(employee);

                if (employeeId.isPresent()) {
                    // Create and save doctor with employee ID
                    Doctor doctor = new Doctor();
                    doctor.setEmployeeId(employeeId.get());
                    doctor.setSpeciality(txtSpeciality.getText());

                    boolean success = doctorDAO.insert(doctor);

                    if (success) {
                        AlertUtils.showInformation("Success", "Doctor Saved",
                                "Doctor was successfully saved with ID: " + employeeId.get());
                        loadDoctorData();
                        clearFields();
                        setStatus("Doctor saved successfully");
                    } else {
                        // If doctor insertion fails, delete the employee
                        employeeDAO.delete(employeeId.get());
                        AlertUtils.showError("Error", "Failed to Save Doctor",
                                "An error occurred while saving the doctor.");
                        setStatus("Failed to save doctor");
                    }
                } else {
                    AlertUtils.showError("Error", "Failed to Save Doctor",
                            "An error occurred while saving the employee information.");
                    setStatus("Failed to save employee information");
                }
            } catch (Exception e) {
                logger.error("Error saving doctor", e);
                AlertUtils.showError("Database Error", "Failed to save doctor", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing doctor in the database.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (validateInput()) {
            try {
                int employeeId = Integer.parseInt(txtEmployeeId.getText());

                // Update employee information
                Employee employee = new Employee(
                        employeeId,
                        txtFirstName.getText(),
                        txtSurname.getText(),
                        txtAddress.getText(),
                        txtPhone.getText(),
                        null, null
                );

                boolean employeeSuccess = employeeDAO.update(employee);

                if (!employeeSuccess) {
                    AlertUtils.showError("Error", "Failed to Update Doctor",
                            "Employee with ID " + employeeId + " was not found.");
                    setStatus("Failed to update employee information");
                    return;
                }

                // Update doctor information
                Doctor doctor = new Doctor();
                doctor.setEmployeeId(employeeId);
                doctor.setSpeciality(txtSpeciality.getText());

                boolean doctorSuccess = doctorDAO.update(doctor);

                if (doctorSuccess) {
                    AlertUtils.showInformation("Success", "Doctor Updated",
                            "Doctor information was successfully updated.");
                    loadDoctorData();
                    clearFields();
                    setStatus("Doctor updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Doctor",
                            "Doctor with ID " + employeeId + " was not found.");
                    setStatus("Failed to update doctor");
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid employee ID format", e);
                AlertUtils.showError("Validation Error", "Invalid Employee ID",
                        "Please select a valid doctor to update.");
                setStatus("Invalid employee ID");
            } catch (Exception e) {
                logger.error("Error updating doctor", e);
                AlertUtils.showError("Database Error", "Failed to update doctor", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the doctor from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            int employeeId = Integer.parseInt(txtEmployeeId.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Doctor", "Are you sure you want to delete this doctor?");

            if (confirm) {
                // Delete doctor first (will cascade to employee)
                boolean success = doctorDAO.delete(employeeId);

                if (success) {
                    // Also delete the employee
                    employeeDAO.delete(employeeId);

                    AlertUtils.showInformation("Success", "Doctor Deleted",
                            "Doctor was successfully deleted.");
                    loadDoctorData();
                    clearFields();
                    setStatus("Doctor deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Doctor",
                            "Doctor with ID " + employeeId + " was not found.");
                    setStatus("Failed to delete doctor");
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid employee ID format", e);
            AlertUtils.showError("Validation Error", "Invalid Employee ID",
                    "Please select a valid doctor to delete.");
            setStatus("Invalid employee ID");
        } catch (Exception e) {
            logger.error("Error deleting doctor", e);
            AlertUtils.showError("Database Error", "Failed to delete doctor", e.getMessage());
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
            errorMessage.append("Phone number is invalid. Use format: XXX-XXXX or similar.\n");
        }

        if (txtSpeciality.getText().trim().isEmpty()) {
            errorMessage.append("Speciality cannot be empty.\n");
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