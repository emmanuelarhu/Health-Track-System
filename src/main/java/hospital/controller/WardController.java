package main.java.hospital.controller;

import main.java.hospital.dao.WardDAO;
import main.java.hospital.dao.DepartmentDAO;
import main.java.hospital.dao.NurseDAO;
import main.java.hospital.model.Ward;
import main.java.hospital.model.Department;
import main.java.hospital.model.Nurse;
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
import javafx.util.StringConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Ward view.
 * Handles all UI interactions related to ward management.
 */
public class WardController implements Initializable {
    private static final Logger logger = LogManager.getLogger(WardController.class);

    private final WardDAO wardDAO = new WardDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final NurseDAO nurseDAO = new NurseDAO();
    private ObservableList<Ward> wardList;
    private FilteredList<Ward> filteredWards;
    private ObservableList<Department> departmentList;
    private ObservableList<Nurse> nurseList;
    private Map<String, String> departmentNames = new HashMap<>();
    private Map<Integer, String> nurseNames = new HashMap<>();

    @FXML private TableView<Ward> tableWards;
    @FXML private TableColumn<Ward, String> colDepartmentCode;
    @FXML private TableColumn<Ward, Integer> colWardNumber;
    @FXML private TableColumn<Ward, Integer> colBedCount;
    @FXML private TableColumn<Ward, Integer> colSupervisor;

    @FXML private ComboBox<Department> cmbDepartment;
    @FXML private TextField txtWardNumber;
    @FXML private TextField txtBedCount;
    @FXML private ComboBox<Nurse> cmbSupervisor;
    @FXML private TextField txtSearch;
    @FXML private Label statusLabel;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private boolean isEditMode = false;
    private String originalDepartmentCode;
    private Integer originalWardNumber;

    /**
     * Initializes the controller.
     *
     * @param location  The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing WardController");

        // Initialize table columns
        colDepartmentCode.setCellValueFactory(new PropertyValueFactory<>("departmentCode"));
        colWardNumber.setCellValueFactory(new PropertyValueFactory<>("wardNumber"));
        colBedCount.setCellValueFactory(new PropertyValueFactory<>("bedCount"));
        colSupervisor.setCellValueFactory(new PropertyValueFactory<>("supervisorId"));

        // Set up custom cell factory for department column to display department name
        colDepartmentCode.setCellFactory(column -> new TableCell<Ward, String>() {
            @Override
            protected void updateItem(String departmentCode, boolean empty) {
                super.updateItem(departmentCode, empty);
                if (empty || departmentCode == null) {
                    setText(null);
                } else {
                    // Display department name if available, otherwise just the code
                    setText(departmentNames.getOrDefault(departmentCode, departmentCode));
                }
            }
        });

        // Set up custom cell factory for supervisor column to display nurse name
        colSupervisor.setCellFactory(column -> new TableCell<Ward, Integer>() {
            @Override
            protected void updateItem(Integer supervisorId, boolean empty) {
                super.updateItem(supervisorId, empty);
                if (empty || supervisorId == null) {
                    setText(null);
                } else {
                    // Display nurse name if available, otherwise unknown
                    setText(nurseNames.getOrDefault(supervisorId, "Unknown"));
                }
            }
        });

        // Load department data for combobox
        loadDepartmentData();

        // Load nurse data for combobox
        loadNurseData();

        // Load ward data
        loadWardData();

        // Set up department combobox with custom string converter
        cmbDepartment.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                return department == null ? "" : department.getName() + " (" + department.getDepartmentCode() + ")";
            }

            @Override
            public Department fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Set up supervisor combobox with custom string converter
        cmbSupervisor.setConverter(new StringConverter<Nurse>() {
            @Override
            public String toString(Nurse nurse) {
                return nurse == null ? "" : nurse.getFullName() + " (" + nurse.getDepartmentCode() + ")";
            }

            @Override
            public Nurse fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Filter nurses based on selected department
        cmbDepartment.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Filter nurses to show only those in the selected department
                List<Nurse> departmentNurses = nurseDAO.findByDepartment(newValue.getDepartmentCode());
                cmbSupervisor.setItems(FXCollections.observableArrayList(departmentNurses));

                // Clear current selection if the nurse doesn't belong to the new department
                if (cmbSupervisor.getValue() != null &&
                        !cmbSupervisor.getValue().getDepartmentCode().equals(newValue.getDepartmentCode())) {
                    cmbSupervisor.setValue(null);
                }
            } else {
                // If no department selected, show all nurses
                cmbSupervisor.setItems(nurseList);
            }
        });

        // Set table selection listener
        tableWards.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showWardDetails(newValue));

        // Initialize button states
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Set up search functionality
        setupSearch();

        setStatus("Ward management module loaded");
    }

    /**
     * Sets the status message.
     *
     * @param message The status message to display
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Loads department data for the department combobox and name mapping.
     */
    private void loadDepartmentData() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentList = FXCollections.observableArrayList(departments);
            cmbDepartment.setItems(departmentList);

            // Create a map of department codes to names for display in the table
            departmentNames.clear();
            for (Department department : departments) {
                departmentNames.put(department.getDepartmentCode(),
                        department.getName() + " (" + department.getDepartmentCode() + ")");
            }

            logger.info("Loaded {} departments for combobox", departments.size());
        } catch (Exception e) {
            logger.error("Error loading department data", e);
            AlertUtils.showError("Database Error", "Failed to load department data", e.getMessage());
        }
    }

    /**
     * Loads nurse data for the supervisor combobox and name mapping.
     */
    private void loadNurseData() {
        try {
            List<Nurse> nurses = nurseDAO.findAll();
            nurseList = FXCollections.observableArrayList(nurses);
            cmbSupervisor.setItems(nurseList);

            // Create a map of nurse IDs to names for display in the table
            nurseNames.clear();
            for (Nurse nurse : nurses) {
                nurseNames.put(nurse.getEmployeeId(),
                        nurse.getFullName() + " (" + nurse.getDepartmentCode() + ")");
            }

            logger.info("Loaded {} nurses for combobox", nurses.size());
        } catch (Exception e) {
            logger.error("Error loading nurse data", e);
            AlertUtils.showError("Database Error", "Failed to load nurse data", e.getMessage());
        }
    }

    /**
     * Sets up the search functionality.
     */
    private void setupSearch() {
        filteredWards = new FilteredList<>(wardList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredWards.setPredicate(ward -> {
                // If search text is empty, show all wards
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Check department code/name
                if (ward.getDepartmentCode().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                String departmentName = departmentNames.get(ward.getDepartmentCode());
                if (departmentName != null && departmentName.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Check ward number
                if (Integer.toString(ward.getWardNumber()).contains(lowerCaseFilter)) {
                    return true;
                }

                // Check supervisor
                String supervisorName = nurseNames.get(ward.getSupervisorId());
                if (supervisorName != null && supervisorName.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // No match
            });

            tableWards.setItems(filteredWards);
        });
    }

    /**
     * Handles the search action.
     */
    @FXML
    private void handleSearchAction(ActionEvent event) {
        // Search is handled by the listener in setupSearch
        setStatus("Searching for: " + txtSearch.getText());
    }

    /**
     * Handles the clear search action.
     */
    @FXML
    private void handleClearSearchAction(ActionEvent event) {
        txtSearch.clear();
        tableWards.setItems(wardList);
        setStatus("Search cleared");
    }

    /**
     * Loads all ward data from the database and displays it in the table.
     */
    private void loadWardData() {
        try {
            List<Ward> wards = wardDAO.findAll();
            wardList = FXCollections.observableArrayList(wards);
            tableWards.setItems(wardList);
            logger.info("Loaded {} wards from database", wards.size());
        } catch (Exception e) {
            logger.error("Error loading ward data", e);
            AlertUtils.showError("Database Error", "Failed to load ward data", e.getMessage());
        }
    }

    /**
     * Displays the details of the selected ward in the form fields.
     *
     * @param ward The selected ward
     */
    private void showWardDetails(Ward ward) {
        if (ward != null) {
            // Save original values for update operation
            originalDepartmentCode = ward.getDepartmentCode();
            originalWardNumber = ward.getWardNumber();

            // Set department in combobox
            for (Department department : departmentList) {
                if (department.getDepartmentCode().equals(ward.getDepartmentCode())) {
                    cmbDepartment.setValue(department);
                    break;
                }
            }

            txtWardNumber.setText(Integer.toString(ward.getWardNumber()));
            txtBedCount.setText(Integer.toString(ward.getBedCount()));

            // Set supervisor in combobox
            for (Nurse nurse : nurseList) {
                if (nurse.getEmployeeId().equals(ward.getSupervisorId())) {
                    cmbSupervisor.setValue(nurse);
                    break;
                }
            }

            // Enable edit mode
            isEditMode = true;
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
        cmbDepartment.setValue(null);
        txtWardNumber.clear();
        txtBedCount.clear();
        cmbSupervisor.setValue(null);

        // Reset original values
        originalDepartmentCode = null;
        originalWardNumber = null;

        // Disable edit mode
        isEditMode = false;
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tableWards.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new ward to the database.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            try {
                Ward ward = new Ward();
                ward.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());
                ward.setWardNumber(Integer.parseInt(txtWardNumber.getText()));
                ward.setBedCount(Integer.parseInt(txtBedCount.getText()));
                ward.setSupervisorId(cmbSupervisor.getValue().getEmployeeId());

                boolean success = wardDAO.insert(ward);

                if (success) {
                    AlertUtils.showInformation("Success", "Ward Saved",
                            "Ward was successfully saved.");
                    loadWardData();
                    clearFields();
                    setStatus("Ward saved successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Save Ward",
                            "An error occurred while saving the ward.");
                    setStatus("Failed to save ward");
                }
            } catch (Exception e) {
                logger.error("Error saving ward", e);
                AlertUtils.showError("Database Error", "Failed to save ward", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing ward in the database.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (validateInput()) {
            try {
                Ward ward = new Ward();
                ward.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());
                ward.setWardNumber(Integer.parseInt(txtWardNumber.getText()));
                ward.setBedCount(Integer.parseInt(txtBedCount.getText()));
                ward.setSupervisorId(cmbSupervisor.getValue().getEmployeeId());

                boolean success = wardDAO.update(originalDepartmentCode, originalWardNumber, ward);

                if (success) {
                    AlertUtils.showInformation("Success", "Ward Updated",
                            "Ward information was successfully updated.");

                    // Update the item in the list directly
                    for (int i = 0; i < wardList.size(); i++) {
                        if (wardList.get(i).getWardId().equals(ward.getWardId())) {
                            wardList.set(i, ward);
                            break;
                        }
                    }

                    // Refresh the table
                    tableWards.refresh();

                    clearFields();
                    setStatus("Ward updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Ward",
                            "Ward was not found or could not be updated.");
                    setStatus("Failed to update ward");
                }
            } catch (Exception e) {
                logger.error("Error updating ward", e);
                AlertUtils.showError("Database Error", "Failed to update ward", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the ward from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            String departmentCode = cmbDepartment.getValue().getDepartmentCode();
            int wardNumber = Integer.parseInt(txtWardNumber.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Ward", "Are you sure you want to delete this ward? This will also affect hospitalization records.");

            if (confirm) {
                boolean success = wardDAO.delete(departmentCode, wardNumber);

                if (success) {
                    AlertUtils.showInformation("Success", "Ward Deleted",
                            "Ward was successfully deleted.");
                    loadWardData();
                    clearFields();
                    setStatus("Ward deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Ward",
                            "Ward was not found or has associated records that prevent deletion.");
                    setStatus("Failed to delete ward");
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting ward", e);
            AlertUtils.showError("Database Error", "Failed to delete ward", e.getMessage());
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

        if (cmbDepartment.getValue() == null) {
            errorMessage.append("Please select a department.\n");
        }

        if (txtWardNumber.getText().trim().isEmpty()) {
            errorMessage.append("Ward number cannot be empty.\n");
        } else if (!ValidationUtils.isValidInteger(txtWardNumber.getText())) {
            errorMessage.append("Ward number must be a valid integer.\n");
        } else {
            int wardNumber = Integer.parseInt(txtWardNumber.getText());
            if (wardNumber <= 0) {
                errorMessage.append("Ward number must be greater than zero.\n");
            } else if (!isEditMode) {
                // Check if ward already exists (only for new wards)
                String departmentCode = cmbDepartment.getValue() != null ?
                        cmbDepartment.getValue().getDepartmentCode() : null;
                if (departmentCode != null) {
                    Optional<Ward> existingWard = wardDAO.findById(departmentCode, wardNumber);
                    if (existingWard.isPresent()) {
                        errorMessage.append("Ward number already exists for this department. Please choose a different number.\n");
                    }
                }
            }
        }

        if (txtBedCount.getText().trim().isEmpty()) {
            errorMessage.append("Bed count cannot be empty.\n");
        } else if (!ValidationUtils.isValidInteger(txtBedCount.getText())) {
            errorMessage.append("Bed count must be a valid integer.\n");
        } else {
            int bedCount = Integer.parseInt(txtBedCount.getText());
            if (bedCount <= 0) {
                errorMessage.append("Bed count must be greater than zero.\n");
            }
        }

        if (cmbSupervisor.getValue() == null) {
            errorMessage.append("Please select a supervisor.\n");
        } else if (cmbDepartment.getValue() != null) {
            // Ensure supervisor belongs to the selected department
            String departmentCode = cmbDepartment.getValue().getDepartmentCode();
            String supervisorDepartment = cmbSupervisor.getValue().getDepartmentCode();
            if (!departmentCode.equals(supervisorDepartment)) {
                errorMessage.append("Supervisor must belong to the selected department.\n");
            }
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