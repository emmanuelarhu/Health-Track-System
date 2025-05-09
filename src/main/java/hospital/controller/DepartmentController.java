package main.java.hospital.controller;

import main.java.hospital.dao.DepartmentDAO;
import main.java.hospital.dao.DoctorDAO;
import main.java.hospital.model.Department;
import main.java.hospital.model.Doctor;
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
 * Controller for the Department view.
 * Handles all UI interactions related to department management.
 */
public class DepartmentController implements Initializable {
    private static final Logger logger = LogManager.getLogger(DepartmentController.class);

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Department> departmentList;
    private FilteredList<Department> filteredDepartments;
    private ObservableList<Doctor> doctorList;

    @FXML private TableView<Department> tableDepartments;
    @FXML private TableColumn<Department, String> colDepartmentCode;
    @FXML private TableColumn<Department, String> colName;
    @FXML private TableColumn<Department, String> colBuilding;
    @FXML private TableColumn<Department, Integer> colDirector;

    @FXML private TextField txtDepartmentCode;
    @FXML private TextField txtName;
    @FXML private TextField txtBuilding;
    @FXML private ComboBox<Doctor> cmbDirector;
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
        logger.info("Initializing DepartmentController");

        // Initialize table columns
        colDepartmentCode.setCellValueFactory(new PropertyValueFactory<>("departmentCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBuilding.setCellValueFactory(new PropertyValueFactory<>("building"));
        colDirector.setCellValueFactory(new PropertyValueFactory<>("directorId"));

        // Load department data
        loadDepartmentData();

        // Load doctor data for combobox
        loadDoctorData();

        // Set table selection listener
        tableDepartments.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDepartmentDetails(newValue));

        // Set up search functionality
        setupSearch();

        setStatus("Department management module loaded");
    }

    /**
     * Set status message
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Loads doctor data for the combobox.
     */
    private void loadDoctorData() {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            doctorList = FXCollections.observableArrayList(doctors);
            cmbDirector.setItems(doctorList);

            // Set the cell factory to display doctor name and ID
            cmbDirector.setCellFactory(param -> new ListCell<Doctor>() {
                @Override
                protected void updateItem(Doctor item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName() + " (ID: " + item.getEmployeeId() + ")");
                    }
                }
            });

            // Set the button cell to display doctor name and ID
            cmbDirector.setButtonCell(new ListCell<Doctor>() {
                @Override
                protected void updateItem(Doctor item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName() + " (ID: " + item.getEmployeeId() + ")");
                    }
                }
            });

            logger.info("Loaded {} doctors", doctors.size());
        } catch (Exception e) {
            logger.error("Error loading doctor data", e);
            AlertUtils.showError("Database Error", "Failed to load doctor data", e.getMessage());
        }
    }

    /**
     * Sets up the search functionality
     */
    private void setupSearch() {
        filteredDepartments = new FilteredList<>(departmentList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDepartments.setPredicate(department -> {
                // If search text is empty, show all departments
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (department.getDepartmentCode().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches department code
                } else if (department.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches name
                } else if (department.getBuilding().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches building
                }
                return false; // No match
            });

            tableDepartments.setItems(filteredDepartments);
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
        tableDepartments.setItems(departmentList);
        setStatus("Search cleared");
    }

    /**
     * Loads all department data from the database and displays it in the table.
     */
    private void loadDepartmentData() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentList = FXCollections.observableArrayList(departments);
            tableDepartments.setItems(departmentList);
            logger.info("Loaded {} departments from database", departments.size());
        } catch (Exception e) {
            logger.error("Error loading department data", e);
            AlertUtils.showError("Database Error", "Failed to load department data", e.getMessage());
        }
    }

    /**
     * Displays the details of the selected department in the form fields.
     *
     * @param department The selected department
     */
    private void showDepartmentDetails(Department department) {
        if (department != null) {
            txtDepartmentCode.setText(department.getDepartmentCode());
            txtName.setText(department.getName());
            txtBuilding.setText(department.getBuilding());

            // Set the director in the combobox
            if (department.getDirectorId() != null) {
                for (Doctor doctor : doctorList) {
                    if (doctor.getEmployeeId().equals(department.getDirectorId())) {
                        cmbDirector.setValue(doctor);
                        break;
                    }
                }
            } else {
                cmbDirector.setValue(null);
            }

            txtDepartmentCode.setEditable(false);
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
        txtDepartmentCode.clear();
        txtName.clear();
        txtBuilding.clear();
        cmbDirector.setValue(null);

        txtDepartmentCode.setEditable(true);
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tableDepartments.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new department to the database.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            Department department = new Department();
            department.setDepartmentCode(txtDepartmentCode.getText().toUpperCase());
            department.setName(txtName.getText());
            department.setBuilding(txtBuilding.getText());

            Doctor selectedDoctor = cmbDirector.getValue();
            if (selectedDoctor != null) {
                department.setDirectorId(selectedDoctor.getEmployeeId());
            }

            try {
                boolean success = departmentDAO.insert(department);

                if (success) {
                    AlertUtils.showInformation("Success", "Department Saved",
                            "Department was successfully saved with code: " + department.getDepartmentCode());
                    loadDepartmentData();
                    clearFields();
                    setStatus("Department saved successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Save Department",
                            "An error occurred while saving the department.");
                    setStatus("Failed to save department");
                }
            } catch (Exception e) {
                logger.error("Error saving department", e);
                AlertUtils.showError("Database Error", "Failed to save department", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing department in the database.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (validateInput()) {
            try {
                Department department = new Department();
                department.setDepartmentCode(txtDepartmentCode.getText().toUpperCase());
                department.setName(txtName.getText());
                department.setBuilding(txtBuilding.getText());

                Doctor selectedDoctor = cmbDirector.getValue();
                if (selectedDoctor != null) {
                    department.setDirectorId(selectedDoctor.getEmployeeId());
                }

                boolean success = departmentDAO.update(department);

                if (success) {
                    AlertUtils.showInformation("Success", "Department Updated",
                            "Department information was successfully updated.");
                    loadDepartmentData();
                    clearFields();
                    setStatus("Department updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Department",
                            "Department with code " + department.getDepartmentCode() + " was not found.");
                    setStatus("Failed to update department");
                }
            } catch (Exception e) {
                logger.error("Error updating department", e);
                AlertUtils.showError("Database Error", "Failed to update department", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the department from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            String departmentCode = txtDepartmentCode.getText().toUpperCase();

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Department", "Are you sure you want to delete this department? All associated wards and nurses will be affected.");

            if (confirm) {
                boolean success = departmentDAO.delete(departmentCode);

                if (success) {
                    AlertUtils.showInformation("Success", "Department Deleted",
                            "Department was successfully deleted.");
                    loadDepartmentData();
                    clearFields();
                    setStatus("Department deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Department",
                            "Department with code " + departmentCode + " was not found.");
                    setStatus("Failed to delete department");
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting department", e);
            AlertUtils.showError("Database Error", "Failed to delete department", e.getMessage());
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

        if (txtDepartmentCode.getText().trim().isEmpty()) {
            errorMessage.append("Department code cannot be empty.\n");
        } else if (txtDepartmentCode.getText().length() > 10) {
            errorMessage.append("Department code cannot exceed 10 characters.\n");
        }

        if (txtName.getText().trim().isEmpty()) {
            errorMessage.append("Department name cannot be empty.\n");
        }

        if (txtBuilding.getText().trim().isEmpty()) {
            errorMessage.append("Building cannot be empty.\n");
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