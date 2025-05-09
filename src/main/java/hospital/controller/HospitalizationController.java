package main.java.hospital.controller;

import main.java.hospital.dao.HospitalizationDAO;
import main.java.hospital.dao.PatientDAO;
import main.java.hospital.dao.DepartmentDAO;
import main.java.hospital.dao.WardDAO;
import main.java.hospital.dao.DoctorDAO;
import main.java.hospital.model.Hospitalization;
import main.java.hospital.model.Patient;
import main.java.hospital.model.Department;
import main.java.hospital.model.Ward;
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
import javafx.util.StringConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Hospitalization view.
 * Handles all UI interactions related to hospitalization management.
 */
public class HospitalizationController implements Initializable {
    private static final Logger logger = LogManager.getLogger(HospitalizationController.class);

    private final HospitalizationDAO hospitalizationDAO = new HospitalizationDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final WardDAO wardDAO = new WardDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private ObservableList<Hospitalization> hospitalizationList;
    private FilteredList<Hospitalization> filteredHospitalizations;
    private ObservableList<Patient> patientList;
    private ObservableList<Department> departmentList;
    private ObservableList<Ward> wardList;
    private ObservableList<Doctor> doctorList;

    private Map<Integer, String> patientNames = new HashMap<>();
    private Map<String, String> departmentNames = new HashMap<>();
    private Map<Integer, String> doctorNames = new HashMap<>();
    private Map<String, ObservableList<Ward>> departmentWards = new HashMap<>();

    @FXML private TableView<Hospitalization> tableHospitalizations;
    @FXML private TableColumn<Hospitalization, Integer> colHospitalizationId;
    @FXML private TableColumn<Hospitalization, Integer> colPatient;
    @FXML private TableColumn<Hospitalization, String> colDepartmentWard;
    @FXML private TableColumn<Hospitalization, Integer> colBedNumber;
    @FXML private TableColumn<Hospitalization, String> colDiagnosis;
    @FXML private TableColumn<Hospitalization, Integer> colDoctor;
    @FXML private TableColumn<Hospitalization, LocalDate> colAdmissionDate;
    @FXML private TableColumn<Hospitalization, LocalDate> colDischargeDate;

    @FXML private TextField txtHospitalizationId;
    @FXML private ComboBox<Patient> cmbPatient;
    @FXML private ComboBox<Department> cmbDepartment;
    @FXML private ComboBox<Ward> cmbWard;
    @FXML private TextField txtBedNumber;
    @FXML private TextField txtDiagnosis;
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private DatePicker dpAdmissionDate;
    @FXML private DatePicker dpDischargeDate;
    @FXML private TextField txtSearch;
    @FXML private CheckBox chkShowCurrent;
    @FXML private Label statusLabel;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDischarge;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private boolean isEditMode = false;

    /**
     * Initializes the controller.
     *
     * @param location  The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing HospitalizationController");

        // Initialize table columns
        colHospitalizationId.setCellValueFactory(new PropertyValueFactory<>("hospitalizationId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colDepartmentWard.setCellValueFactory(param -> {
            String departmentCode = param.getValue().getDepartmentCode();
            Integer wardNumber = param.getValue().getWardNumber();
            String departmentWard = departmentNames.getOrDefault(departmentCode, departmentCode)
                    + " / Ward " + wardNumber;
            return javafx.beans.binding.Bindings.createStringBinding(() -> departmentWard);
        });
        colBedNumber.setCellValueFactory(new PropertyValueFactory<>("bedNumber"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        colAdmissionDate.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        colDischargeDate.setCellValueFactory(new PropertyValueFactory<>("dischargeDate"));

        // Set up custom cell factories
        colPatient.setCellFactory(column -> new TableCell<Hospitalization, Integer>() {
            @Override
            protected void updateItem(Integer patientId, boolean empty) {
                super.updateItem(patientId, empty);
                if (empty || patientId == null) {
                    setText(null);
                } else {
                    // Display patient name if available, otherwise just the ID
                    setText(patientNames.getOrDefault(patientId, "Unknown"));
                }
            }
        });

        colDoctor.setCellFactory(column -> new TableCell<Hospitalization, Integer>() {
            @Override
            protected void updateItem(Integer doctorId, boolean empty) {
                super.updateItem(doctorId, empty);
                if (empty || doctorId == null) {
                    setText(null);
                } else {
                    // Display doctor name if available, otherwise just the ID
                    setText(doctorNames.getOrDefault(doctorId, "Unknown"));
                }
            }
        });

        // Date cell factories
        colAdmissionDate.setCellFactory(column -> new TableCell<Hospitalization, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toString());
                }
            }
        });

        colDischargeDate.setCellFactory(column -> new TableCell<Hospitalization, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("Still Admitted");
                } else {
                    setText(date.toString());
                }
            }
        });

        // Load reference data
        loadPatientData();
        loadDepartmentData();
        loadDoctorData();
        loadWardData();

        // Load hospitalization data
        loadHospitalizationData();

        // Set up comboboxes with custom string converters
        setupComboBoxes();

        // Set table selection listener
        tableHospitalizations.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showHospitalizationDetails(newValue));

        // Initialize button states
        btnUpdate.setDisable(true);
        btnDischarge.setDisable(true);
        btnDelete.setDisable(true);

        // Set up department-ward dependency
        cmbDepartment.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Set wards for the selected department
                String departmentCode = newValue.getDepartmentCode();
                cmbWard.setItems(departmentWards.getOrDefault(departmentCode, FXCollections.observableArrayList()));
            } else {
                cmbWard.getItems().clear();
            }
            cmbWard.setValue(null);
        });

        // Set the admission date to today by default
        dpAdmissionDate.setValue(LocalDate.now());

        // Set up search functionality
        setupSearch();

        setStatus("Hospitalization management module loaded");
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
     * Sets up all comboboxes with appropriate string converters.
     */
    private void setupComboBoxes() {
        // Patient combobox
        cmbPatient.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getFullName() + " (ID: " + patient.getPatientId() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Department combobox
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

        // Ward combobox
        cmbWard.setConverter(new StringConverter<Ward>() {
            @Override
            public String toString(Ward ward) {
                return ward == null ? "" : "Ward " + ward.getWardNumber() + " (" + ward.getBedCount() + " beds)";
            }

            @Override
            public Ward fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Doctor combobox
        cmbDoctor.setConverter(new StringConverter<Doctor>() {
            @Override
            public String toString(Doctor doctor) {
                return doctor == null ? "" : doctor.getFullName() + " (" + doctor.getSpeciality() + ")";
            }

            @Override
            public Doctor fromString(String string) {
                return null; // Not needed for this use case
            }
        });
    }

    /**
     * Loads patient data for the patient combobox and name mapping.
     */
    private void loadPatientData() {
        try {
            List<Patient> patients = patientDAO.findAll();
            patientList = FXCollections.observableArrayList(patients);
            cmbPatient.setItems(patientList);

            // Create a map of patient IDs to names for display in the table
            patientNames.clear();
            for (Patient patient : patients) {
                patientNames.put(patient.getPatientId(), patient.getFullName());
            }

            logger.info("Loaded {} patients for combobox", patients.size());
        } catch (Exception e) {
            logger.error("Error loading patient data", e);
            AlertUtils.showError("Database Error", "Failed to load patient data", e.getMessage());
        }
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
                departmentNames.put(department.getDepartmentCode(), department.getName());
            }

            logger.info("Loaded {} departments for combobox", departments.size());
        } catch (Exception e) {
            logger.error("Error loading department data", e);
            AlertUtils.showError("Database Error", "Failed to load department data", e.getMessage());
        }
    }

    /**
     * Loads ward data for the ward combobox and department-ward mapping.
     */
    private void loadWardData() {
        try {
            List<Ward> wards = wardDAO.findAll();
            wardList = FXCollections.observableArrayList(wards);

            // Create a map of department codes to ward lists
            departmentWards.clear();
            for (Ward ward : wards) {
                String departmentCode = ward.getDepartmentCode();
                if (!departmentWards.containsKey(departmentCode)) {
                    departmentWards.put(departmentCode, FXCollections.observableArrayList());
                }
                departmentWards.get(departmentCode).add(ward);
            }

            logger.info("Loaded {} wards for combobox", wards.size());
        } catch (Exception e) {
            logger.error("Error loading ward data", e);
            AlertUtils.showError("Database Error", "Failed to load ward data", e.getMessage());
        }
    }

    /**
     * Loads doctor data for the doctor combobox and name mapping.
     */
    private void loadDoctorData() {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            doctorList = FXCollections.observableArrayList(doctors);
            cmbDoctor.setItems(doctorList);

            // Create a map of doctor IDs to names for display in the table
            doctorNames.clear();
            for (Doctor doctor : doctors) {
                doctorNames.put(doctor.getEmployeeId(), doctor.getFullName() + " (" + doctor.getSpeciality() + ")");
            }

            logger.info("Loaded {} doctors for combobox", doctors.size());
        } catch (Exception e) {
            logger.error("Error loading doctor data", e);
            AlertUtils.showError("Database Error", "Failed to load doctor data", e.getMessage());
        }
    }

    /**
     * Loads all hospitalization data from the database and displays it in the table.
     */
    private void loadHospitalizationData() {
        try {
            List<Hospitalization> hospitalizations;

            if (chkShowCurrent.isSelected()) {
                // Show only current hospitalizations (no discharge date)
                hospitalizations = hospitalizationDAO.findCurrentHospitalizations();
            } else {
                // Show all hospitalizations
                hospitalizations = hospitalizationDAO.findAll();
            }

            hospitalizationList = FXCollections.observableArrayList(hospitalizations);

            setupSearch(); // Re-apply search filter

            logger.info("Loaded {} hospitalizations from database", hospitalizations.size());
        } catch (Exception e) {
            logger.error("Error loading hospitalization data", e);
            AlertUtils.showError("Database Error", "Failed to load hospitalization data", e.getMessage());
        }
    }

    /**
     * Sets up the search functionality.
     */
    private void setupSearch() {
        filteredHospitalizations = new FilteredList<>(hospitalizationList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredHospitalizations.setPredicate(hospitalization -> {
                // If search text is empty, show all hospitalizations
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Check patient name
                String patientName = patientNames.get(hospitalization.getPatientId());
                if (patientName != null && patientName.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Check diagnosis
                if (hospitalization.getDiagnosis().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Check doctor name
                String doctorName = doctorNames.get(hospitalization.getDoctorId());
                if (doctorName != null && doctorName.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Check department name
                String departmentName = departmentNames.get(hospitalization.getDepartmentCode());
                if (departmentName != null && departmentName.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // No match
            });

            tableHospitalizations.setItems(filteredHospitalizations);
        });

        // Apply current filter
        tableHospitalizations.setItems(filteredHospitalizations);
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
        setupSearch(); // Reset the filtered list
        setStatus("Search cleared");
    }

    /**
     * Handles the show current only checkbox action.
     */
    @FXML
    private void handleShowCurrentAction(ActionEvent event) {
        loadHospitalizationData();
        if (chkShowCurrent.isSelected()) {
            setStatus("Showing current hospitalizations only");
        } else {
            setStatus("Showing all hospitalizations");
        }
    }

    /**
     * Displays the details of the selected hospitalization in the form fields.
     *
     * @param hospitalization The selected hospitalization
     */
    private void showHospitalizationDetails(Hospitalization hospitalization) {
        if (hospitalization != null) {
            txtHospitalizationId.setText(hospitalization.getHospitalizationId().toString());

            // Set patient in combobox
            for (Patient patient : patientList) {
                if (patient.getPatientId().equals(hospitalization.getPatientId())) {
                    cmbPatient.setValue(patient);
                    break;
                }
            }

            // Set department in combobox
            for (Department department : departmentList) {
                if (department.getDepartmentCode().equals(hospitalization.getDepartmentCode())) {
                    cmbDepartment.setValue(department);
                    break;
                }
            }

            // Set ward in combobox
            for (Ward ward : wardList) {
                if (ward.getDepartmentCode().equals(hospitalization.getDepartmentCode()) &&
                        ward.getWardNumber().equals(hospitalization.getWardNumber())) {
                    cmbWard.setValue(ward);
                    break;
                }
            }

            txtBedNumber.setText(Integer.toString(hospitalization.getBedNumber()));
            txtDiagnosis.setText(hospitalization.getDiagnosis());

            // Set doctor in combobox
            for (Doctor doctor : doctorList) {
                if (doctor.getEmployeeId().equals(hospitalization.getDoctorId())) {
                    cmbDoctor.setValue(doctor);
                    break;
                }
            }

            dpAdmissionDate.setValue(hospitalization.getAdmissionDate());
            dpDischargeDate.setValue(hospitalization.getDischargeDate());

            // Enable edit mode
            isEditMode = true;
            cmbPatient.setDisable(true); // Don't allow changing the patient
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);

            // Enable discharge button only for current hospitalizations
            btnDischarge.setDisable(hospitalization.getDischargeDate() != null);
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
        txtHospitalizationId.clear();
        cmbPatient.setValue(null);
        cmbDepartment.setValue(null);
        cmbWard.setValue(null);
        txtBedNumber.clear();
        txtDiagnosis.clear();
        cmbDoctor.setValue(null);
        dpAdmissionDate.setValue(LocalDate.now());
        dpDischargeDate.setValue(null);

        // Disable edit mode
        isEditMode = false;
        cmbPatient.setDisable(false);
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDischarge.setDisable(true);
        btnDelete.setDisable(true);

        tableHospitalizations.getSelectionModel().clearSelection();

        setStatus("Form cleared");
    }

    /**
     * Handles the save button action.
     * Validates input and saves a new hospitalization to the database.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            try {
                Hospitalization hospitalization = new Hospitalization();
                hospitalization.setPatientId(cmbPatient.getValue().getPatientId());
                hospitalization.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());
                hospitalization.setWardNumber(cmbWard.getValue().getWardNumber());
                hospitalization.setBedNumber(Integer.parseInt(txtBedNumber.getText()));
                hospitalization.setDiagnosis(txtDiagnosis.getText());
                hospitalization.setDoctorId(cmbDoctor.getValue().getEmployeeId());
                hospitalization.setAdmissionDate(dpAdmissionDate.getValue());
                hospitalization.setDischargeDate(dpDischargeDate.getValue());

                Optional<Integer> hospitalizationId = hospitalizationDAO.insert(hospitalization);

                if (hospitalizationId.isPresent()) {
                    AlertUtils.showInformation("Success", "Hospitalization Saved",
                            "Hospitalization record was successfully saved with ID: " + hospitalizationId.get());
                    loadHospitalizationData();
                    clearFields();
                    setStatus("Hospitalization saved successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Save Hospitalization",
                            "An error occurred while saving the hospitalization record.");
                    setStatus("Failed to save hospitalization");
                }
            } catch (Exception e) {
                logger.error("Error saving hospitalization", e);
                AlertUtils.showError("Database Error", "Failed to save hospitalization", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the update button action.
     * Validates input and updates an existing hospitalization in the database.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (validateInput()) {
            try {
                int hospitalizationId = Integer.parseInt(txtHospitalizationId.getText());

                Hospitalization hospitalization = new Hospitalization();
                hospitalization.setHospitalizationId(hospitalizationId);
                hospitalization.setPatientId(cmbPatient.getValue().getPatientId());
                hospitalization.setDepartmentCode(cmbDepartment.getValue().getDepartmentCode());
                hospitalization.setWardNumber(cmbWard.getValue().getWardNumber());
                hospitalization.setBedNumber(Integer.parseInt(txtBedNumber.getText()));
                hospitalization.setDiagnosis(txtDiagnosis.getText());
                hospitalization.setDoctorId(cmbDoctor.getValue().getEmployeeId());
                hospitalization.setAdmissionDate(dpAdmissionDate.getValue());
                hospitalization.setDischargeDate(dpDischargeDate.getValue());

                boolean success = hospitalizationDAO.update(hospitalization);

                if (success) {
                    AlertUtils.showInformation("Success", "Hospitalization Updated",
                            "Hospitalization record was successfully updated.");
                    loadHospitalizationData();
                    clearFields();
                    setStatus("Hospitalization updated successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Update Hospitalization",
                            "Hospitalization with ID " + hospitalizationId + " was not found.");
                    setStatus("Failed to update hospitalization");
                }
            } catch (Exception e) {
                logger.error("Error updating hospitalization", e);
                AlertUtils.showError("Database Error", "Failed to update hospitalization", e.getMessage());
                setStatus("Database error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the discharge button action.
     * Sets the discharge date to today and updates the hospitalization record.
     */
    @FXML
    private void handleDischargeAction(ActionEvent event) {
        try {
            int hospitalizationId = Integer.parseInt(txtHospitalizationId.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Discharge",
                    "Discharge Patient", "Are you sure you want to discharge this patient today?");

            if (confirm) {
                // Set discharge date to today
                LocalDate dischargeDate = LocalDate.now();

                boolean success = hospitalizationDAO.dischargePatient(hospitalizationId, dischargeDate);

                if (success) {
                    AlertUtils.showInformation("Success", "Patient Discharged",
                            "Patient was successfully discharged.");
                    loadHospitalizationData();
                    clearFields();
                    setStatus("Patient discharged successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Discharge Patient",
                            "Hospitalization with ID " + hospitalizationId + " was not found.");
                    setStatus("Failed to discharge patient");
                }
            }
        } catch (Exception e) {
            logger.error("Error discharging patient", e);
            AlertUtils.showError("Database Error", "Failed to discharge patient", e.getMessage());
            setStatus("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles the delete button action.
     * Confirms deletion and removes the hospitalization from the database.
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        try {
            int hospitalizationId = Integer.parseInt(txtHospitalizationId.getText());

            boolean confirm = AlertUtils.showConfirmation("Confirm Delete",
                    "Delete Hospitalization", "Are you sure you want to delete this hospitalization record?");

            if (confirm) {
                boolean success = hospitalizationDAO.delete(hospitalizationId);

                if (success) {
                    AlertUtils.showInformation("Success", "Hospitalization Deleted",
                            "Hospitalization record was successfully deleted.");
                    loadHospitalizationData();
                    clearFields();
                    setStatus("Hospitalization deleted successfully");
                } else {
                    AlertUtils.showError("Error", "Failed to Delete Hospitalization",
                            "Hospitalization with ID " + hospitalizationId + " was not found.");
                    setStatus("Failed to delete hospitalization");
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting hospitalization", e);
            AlertUtils.showError("Database Error", "Failed to delete hospitalization", e.getMessage());
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

        if (cmbPatient.getValue() == null) {
            errorMessage.append("Please select a patient.\n");
        }

        if (cmbDepartment.getValue() == null) {
            errorMessage.append("Please select a department.\n");
        }

        if (cmbWard.getValue() == null) {
            errorMessage.append("Please select a ward.\n");
        }

        if (txtBedNumber.getText().trim().isEmpty()) {
            errorMessage.append("Bed number cannot be empty.\n");
        } else if (!ValidationUtils.isValidInteger(txtBedNumber.getText())) {
            errorMessage.append("Bed number must be a valid integer.\n");
        } else {
            int bedNumber = Integer.parseInt(txtBedNumber.getText());
            if (bedNumber <= 0) {
                errorMessage.append("Bed number must be greater than zero.\n");
            } else if (cmbWard.getValue() != null) {
                // Check if bed number exceeds ward capacity
                int bedCount = cmbWard.getValue().getBedCount();
                if (bedNumber > bedCount) {
                    errorMessage.append("Bed number exceeds ward capacity (" + bedCount + " beds).\n");
                }

                // Check if bed is already occupied by another patient
                if (!isEditMode) {
                    String departmentCode = cmbDepartment.getValue().getDepartmentCode();
                    int wardNumber = cmbWard.getValue().getWardNumber();
                    boolean bedOccupied = hospitalizationDAO.isBedOccupied(departmentCode, wardNumber, bedNumber);
                    if (bedOccupied) {
                        errorMessage.append("This bed is already occupied by another patient.\n");
                    }
                }
            }
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            errorMessage.append("Diagnosis cannot be empty.\n");
        }

        if (cmbDoctor.getValue() == null) {
            errorMessage.append("Please select a doctor.\n");
        }

        if (dpAdmissionDate.getValue() == null) {
            errorMessage.append("Admission date cannot be empty.\n");
        } else if (dpAdmissionDate.getValue().isAfter(LocalDate.now())) {
            errorMessage.append("Admission date cannot be in the future.\n");
        }

        if (dpDischargeDate.getValue() != null) {
            if (dpDischargeDate.getValue().isBefore(dpAdmissionDate.getValue())) {
                errorMessage.append("Discharge date cannot be before admission date.\n");
            } else if (dpDischargeDate.getValue().isAfter(LocalDate.now())) {
                errorMessage.append("Discharge date cannot be in the future.\n");
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