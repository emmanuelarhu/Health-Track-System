package main.java.hospital.controller;

import main.java.hospital.dao.NurseDAO;
import main.java.hospital.dao.EmployeeDAO;
import main.java.hospital.dao.DepartmentDAO;
import main.java.hospital.model.Nurse;
import main.java.hospital.model.Employee;
import main.java.hospital.model.Department;
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

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Nurse view.
 * Handles all UI interactions related to nurse management.
 */
public class NurseController implements Initializable {
    private static final Logger logger = LogManager.getLogger(NurseController.class);

    private final NurseDAO nurseDAO = new NurseDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private ObservableList<Nurse> nurseList;
    private FilteredList<Nurse> filteredNurses;
    private ObservableList<Department> departmentList;

    @FXML private TableView<Nurse> tableNurses;
    @FXML private TableColumn<Nurse, Integer> colEmployeeId;
    @FXML private TableColumn<Nurse, String> colFirstName;
    @FXML private TableColumn<Nurse, String> colSurname;
    @FXML private TableColumn<Nurse, String> colRotation;
    @FXML private TableColumn<Nurse, BigDecimal> colSalary;
    @FXML private TableColumn<Nurse, String> colDepartment;

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbRotation;
    @FXML private TextField txtSalary;
    @FXML private ComboBox<Department> cmbDepartment;
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
        logger.info("Initializing NurseController");

        // Initialize table columns
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colRotation.setCellValueFactory(new PropertyValueFactory<>("rotation"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentCode"));

        // Load nurse data
        loadNurseData();

        // Load department data for combobox
        loadDepartmentData();

        // Initialize rotation combobox
        cmbRotation.setItems(FXCollections.observableArrayList("Morning", "Evening", "Night"));

        // Set table selection listener
        tableNurses.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showNurseDetails(newValue));

        // Disable employee ID field (auto-generated)
        txtEmployeeId.setEditable(false);

        // Initialize button states
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Set up search functionality
        setupSearch();

        setStatus("Nurse management module loaded");
    }

    /**
     * Set status message
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Loads department data for the combobox.
     */
    private void loadDepartmentData() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentList = FXCollections.observableArrayList(departments);
            cmbDepartment.setItems(departmentList);
            cmbDepartment.setCellFactory(param -> new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getDepartmentCode() + ")");
                    }
                }
            });
            cmbDepartment.setButtonCell(new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getDepartmentCode() + ")");
                    }
                }
            });

            logger.info("Loaded {} departments", departments.size());
        } catch (Exception e) {
            logger.error("Error loading department data", e);
            AlertUtils.showError("Database Error", "Failed to load department data", e.getMessage());
        }
    }

    /**
     * Sets up the search functionality
     */
    private void setupSearch() {
        filteredNurses = new FilteredList<>(nurseList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredNurses.setPredicate(nurse -> {
                // If search text is empty, show all nurses
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (nurse.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name
                } else if (nurse.getSurname().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches surname
                } else if (nurse.getRotation().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches rotation
                } else if (nurse.getDepartmentCode().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches department code
                } else if (nurse.getEmployeeId().toString().contains(lowerCaseFilter)) {
                    return true; // Filter matches ID
                }
                return false; // No match
            });

            tableNurses.setItems(filteredNurses);
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
        tableNurses.setItems(nurseList);
        setStatus("Search cleared");
    }

    /**
     * Loads all nurse data from the database and displays it in the table.
     */
    private void loadNurseData() {
        try {
            List<Nurse> nurses = nurseDAO.findAll();
            nurseList = FXCollections.observableArrayList(nurses);
            tableNurses.setItems(nurseList);
            logger.info("Loaded {} nurses from database", nurses.size());
        } catch (Exception e) {
            logger.error("Error loading nurse data", e);
            AlertUtils.showError("Database Error", "Failed to load nurse data", e.getMessage());
        }
    }

    /**
     * Displays the details of the selected nurse in the form fields.
     *
     * @param nurse The selected nurse
     */
    private void showNurseDetails(Nurse nurse) {
        if (nurse != null) {
            txtEmployeeId.setText(nurse.getEmployeeId().toString());
            txtFirstName.setText(nurse.getFirstName());
            txtSurname.setText(nurse.getSurname());
            txtAddress.setText(nurse.getAddress());
            txtPhone.setText(nurse.getPhone());
            cmbRotation.setValue(nurse.getRotation());
            txtSalary.setText(nurse.getSalary().toString());

            // Set the department in the combobox
            String departmentCode = nurse.getDepartmentCode();
            for (Department department : departmentList) {
                if (department.getDepartmentCode().equals(departmentCode)) {
                    cmbDepartment.setValue(department);
                    break;
                }
            }

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
        cmbRotation.getSelectionModel().clearSelection();
        txtSalary.clear();
        cmbDepartment.getSelectionModel().clearSelection();

        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tableNurses.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new nurse to the database.
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
                    // Create and save nurse with employee ID
                    Nurse nurse = new Nurse();
                    nurse.setEmployeeId(employeeId.get());
                    nurse.setRotation(cmbRotation.getValue());
                    nurse.setSalary(new BigDecimal(txtSalary.getText()));
                    nurse.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());

                    boolean success = nurseDAO.insert(nurse);

                    if (success) {
                        AlertUtils.showInformation("Success", "Nurse Saved",
                                "Nurse was successfully saved with ID: " + employeeId.get());
                        loadNurseData();
                        clearFields();
                        setStatus("Nurse saved successfully");
                    } else {
                        // If nurse insertion fails, delete the employee
                        employeeDAO.delete(employeeId.get());
                        AlertUtils.showError("Error", "Failed to Save Nurse",
                                "An error occurred while saving the nurse.");
                        setStatus("Failed to save nurse");
                    }
                } else {
                    AlertUtils.showError("Error", "Failed to Save Nurse",
                            "An error occurred while saving the employee information.");
                    setStatus("Failed to save employee information");
                }
            } catch (Exception e) {
                logger.error("Error saving nurse", e);
                AlertUtils.showError("Database Error", "Failed to save nurse", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing nurse in the database.
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
                    AlertUtils.showError("Error", "Failed to Update Nurse",
                            "Employee with ID " + employeeId + " was not found.");
                    setStatus("Failed to update employee information");
                    return;
                }

                // Update nurse information
                Nurse nurse = new Nurse();
                nurse.setEmployeeId(employeeId);
                nurse.setRotation(cmbRotation.getValue());
                nurse.setSalary(new BigDecimal(txtSalary.getText()));
                nurse.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());

                boolean nurseSuccess = nurseDAO.update(nurse);

                if (nurseSuccess) {
                    AlertUtils.showInformation("Success", "Nurse Updated",
                            "Nurse information was successfully updated.");
                    loadNurseData();
                    clearFields();
                    setStatus("Nurse updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Nurse",
                            "Nurse with ID " + employeeId + " was not found.");
                    setStatus("Failed to update nurse");
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid employee ID format", e);
                AlertUtils.showError("Validation Error", "Invalid Employee ID",
                        "Please select a valid nurse to update.");
                setStatus("Invalid employee ID");
            } catch (Exception e) {
                logger.error("Error updating nurse", e);
                AlertUtils.showError("Database Error", "Failed to update nurse", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the nurse from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            int employeeId = Integer.parseInt(txtEmployeeId.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Nurse", "Are you sure you want to delete this nurse?");

            if (confirm) {
                // Delete nurse first (will cascade to employee)
                boolean success = nurseDAO.delete(employeeId);

                if (success) {
                    // Also delete the employee
                    employeeDAO.delete(employeeId);

                    AlertUtils.showInformation("Success", "Nurse Deleted",
                            "Nurse was successfully deleted.");
                    loadNurseData();
                    clearFields();
                    setStatus("Nurse deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Nurse",
                            "Nurse with ID " + employeeId + " was not found.");
                    setStatus("Failed to delete nurse");
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid employee ID format", e);
            AlertUtils.showError("Validation Error", "Invalid Employee ID",
                    "Please select a valid nurse to delete.");
            setStatus("Invalid employee ID");
        } catch (Exception e) {
            logger.error("Error deleting nurse", e);
            AlertUtils.showError("Database Error", "Failed to delete nurse", e.getMessage());
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

        if (cmbRotation.getValue() == null) {
            errorMessage.append("Please select a rotation.\n");
        }

        if (txtSalary.getText().trim().isEmpty()) {
            errorMessage.append("Salary cannot be empty.\n");
        } else {
            try {
                BigDecimal salary = new BigDecimal(txtSalary.getText());
                if (salary.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Salary must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Salary must be a valid number.\n");
            }
        }

        if (cmbDepartment.getValue() == null) {
            errorMessage.append("Please select a department.\n");
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