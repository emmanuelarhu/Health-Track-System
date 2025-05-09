package main.java.hospital.controller;

import javafx.beans.value.ObservableValue;
import main.java.hospital.dao.*;
import main.java.hospital.model.*;
import main.java.hospital.util.AlertUtils;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Patient Reports view.
 * Handles generation of various patient-related reports.
 */
public class PatientReportController implements Initializable {
    private static final Logger logger = LogManager.getLogger(PatientReportController.class);

    private final PatientDAO patientDAO = new PatientDAO();
    private final HospitalizationDAO hospitalizationDAO = new HospitalizationDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private ObservableList<Patient> patientList;
    private ObservableList<Department> departmentList;
    private Map<Integer, String> patientNames = new HashMap<>();
    private Map<String, String> departmentNames = new HashMap<>();
    private Map<Integer, String> doctorNames = new HashMap<>();

    private ObservableList<Map<String, Object>> reportData = FXCollections.observableArrayList();
    private String currentReportType = "";

    @FXML private ComboBox<Patient> cmbPatient;
    @FXML private ComboBox<Department> cmbDepartment;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtDiagnosis;
    @FXML private Label lblReportTitle;
    @FXML private Label lblRecordCount;
    @FXML private TableView<Map<String, Object>> tableReport;
    @FXML private TextArea txtReportSummary;
    @FXML private Label statusLabel;

    /**
     * Initializes the controller.
     *
     * @param location  The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing PatientReportController");

        // Load reference data
        loadPatientData();
        loadDepartmentData();
        loadDoctorData();

        // Set up comboboxes with custom string converters
        setupComboBoxes();

        // Initialize date pickers
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        setStatus("Patient report module loaded");
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
     * Loads doctor data for name mapping.
     */
    private void loadDoctorData() {
        try {
            List<Doctor> doctors = doctorDAO.findAll();

            // Create a map of doctor IDs to names for display in the table
            doctorNames.clear();
            for (Doctor doctor : doctors) {
                doctorNames.put(doctor.getEmployeeId(), doctor.getFullName() + " (" + doctor.getSpeciality() + ")");
            }

            logger.info("Loaded {} doctors for name mapping", doctors.size());
        } catch (Exception e) {
            logger.error("Error loading doctor data", e);
            AlertUtils.showError("Database Error", "Failed to load doctor data", e.getMessage());
        }
    }

    /**
     * Sets up the report table with the appropriate columns for the current report type.
     */
    private void setupReportTable() {
        // Clear existing columns
        tableReport.getColumns().clear();

        // Add columns based on report type
        switch (currentReportType) {
            case "PATIENT_HISTORY":
                addTableColumn("Hospitalization ID", "hospitalizationId");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Discharge Date", "dischargeDate");
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Bed", "bedNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Doctor", "doctorName");
                break;

            case "DEPARTMENT_PATIENTS":
                addTableColumn("Patient ID", "patientId");
                addTableColumn("Patient Name", "patientName");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Discharge Date", "dischargeDate");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Bed", "bedNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Doctor", "doctorName");
                break;

            case "DATE_RANGE":
                addTableColumn("Patient ID", "patientId");
                addTableColumn("Patient Name", "patientName");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Discharge Date", "dischargeDate");
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Doctor", "doctorName");
                break;

            case "DIAGNOSIS":
                addTableColumn("Patient ID", "patientId");
                addTableColumn("Patient Name", "patientName");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Discharge Date", "dischargeDate");
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Doctor", "doctorName");
                break;

            case "CURRENT_PATIENTS":
                addTableColumn("Patient ID", "patientId");
                addTableColumn("Patient Name", "patientName");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Bed", "bedNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Doctor", "doctorName");
                break;

            case "STATISTICS":
                addTableColumn("Statistic", "statistic");
                addTableColumn("Value", "value");
                break;
        }

        // Set the data
        tableReport.setItems(reportData);
    }

    /**
     * Adds a column to the report table.
     *
     * @param title The column title
     * @param property The property name to bind to
     */
    @SuppressWarnings("unchecked")
    private void addTableColumn(String title, String property) {
        TableColumn<Map<String, Object>, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> {
            Map<String, Object> rowData = data.getValue();
            Object value = rowData.get(property);

            // Cast to ObservableValue<Object> to ensure consistent return type
            if (value instanceof String) {
                return (ObservableValue<Object>) (ObservableValue<?>) new SimpleStringProperty((String) value);
            } else {
                return new SimpleObjectProperty<>(value);
            }
        });

        // Set cell factory for numeric values
        if (property.equals("salary")) {
            column.setCellFactory(col -> new TableCell<Map<String, Object>, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else if (item instanceof BigDecimal) {
                        setText(String.format("$%.2f", ((BigDecimal) item).doubleValue()));
                    } else {
                        setText(item.toString());
                    }
                }
            });
        }

        // Set cell factory for date values
        if (property.contains("Date")) {
            column.setCellFactory(col -> new TableCell<Map<String, Object>, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else if (item instanceof LocalDate) {
                        setText(((LocalDate) item).toString());
                    } else {
                        setText(item.toString());
                    }
                }
            });
        }

        tableReport.getColumns().add(column);
    }

    /**
     * Handles the action to generate a patient history report.
     */
    @FXML
    private void handlePatientReportAction(ActionEvent event) {
        Patient selectedPatient = cmbPatient.getValue();

        if (selectedPatient == null) {
            AlertUtils.showWarning("Input Required", "Patient Selection Required",
                    "Please select a patient to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "PATIENT_HISTORY";

            // Clear the report data
            reportData.clear();

            // Get hospitalizations for the patient
            List<Hospitalization> hospitalizations = hospitalizationDAO.findByPatient(selectedPatient.getPatientId());

            // Process each hospitalization
            for (Hospitalization h : hospitalizations) {
                Map<String, Object> row = new HashMap<>();
                row.put("hospitalizationId", h.getHospitalizationId());
                row.put("admissionDate", h.getAdmissionDate());
                row.put("dischargeDate", h.getDischargeDate());
                row.put("departmentName", departmentNames.getOrDefault(h.getDepartmentCode(), h.getDepartmentCode()));
                row.put("wardNumber", h.getWardNumber());
                row.put("bedNumber", h.getBedNumber());
                row.put("diagnosis", h.getDiagnosis());
                row.put("doctorName", doctorNames.getOrDefault(h.getDoctorId(), "Doctor ID: " + h.getDoctorId()));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Patient History for " + selectedPatient.getFullName());
            lblRecordCount.setText(hospitalizations.size() + " records found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Patient: ").append(selectedPatient.getFullName()).append("\n");
            summary.append("Total Hospitalizations: ").append(hospitalizations.size()).append("\n");

            // Count current hospitalizations
            long currentCount = hospitalizations.stream()
                    .filter(h -> h.getDischargeDate() == null)
                    .count();

            if (currentCount > 0) {
                summary.append("Currently Hospitalized: Yes\n");
            } else {
                LocalDate lastDischarge = hospitalizations.stream()
                        .filter(h -> h.getDischargeDate() != null)
                        .map(Hospitalization::getDischargeDate)
                        .max(LocalDate::compareTo)
                        .orElse(null);

                if (lastDischarge != null) {
                    summary.append("Last Discharge Date: ").append(lastDischarge).append("\n");
                }
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated patient history report for " + selectedPatient.getFullName());
        } catch (Exception e) {
            logger.error("Error generating patient report", e);
            AlertUtils.showError("Report Error", "Failed to generate patient report", e.getMessage());
            setStatus("Error generating patient report");
        }
    }

    /**
     * Handles the action to generate a department patients report.
     */
    @FXML
    private void handleDepartmentReportAction(ActionEvent event) {
        Department selectedDepartment = cmbDepartment.getValue();

        if (selectedDepartment == null) {
            AlertUtils.showWarning("Input Required", "Department Selection Required",
                    "Please select a department to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "DEPARTMENT_PATIENTS";

            // Clear the report data
            reportData.clear();

            // SQL to get hospitalizations by department
            String sql = "SELECT h.*, p.first_name, p.surname FROM Hospitalization h " +
                    "JOIN Patient p ON h.patient_id = p.patient_id " +
                    "WHERE h.department_code = ? AND h.discharge_date IS NULL " +
                    "ORDER BY h.ward_number, h.bed_number";

            // Custom query to get current patients in the department
            List<Map<String, Object>> results = getCustomQueryResults(sql, selectedDepartment.getDepartmentCode());

            // Process each result
            for (Map<String, Object> result : results) {
                Map<String, Object> row = new HashMap<>();

                int patientId = (Integer) result.get("patient_id");
                String firstName = (String) result.get("first_name");
                String surname = (String) result.get("surname");
                String patientName = firstName + " " + surname;

                row.put("patientId", patientId);
                row.put("patientName", patientName);
                row.put("admissionDate", result.get("admission_date"));
                row.put("dischargeDate", result.get("discharge_date"));
                row.put("wardNumber", result.get("ward_number"));
                row.put("bedNumber", result.get("bed_number"));
                row.put("diagnosis", result.get("diagnosis"));

                int doctorId = (Integer) result.get("doctor_id");
                row.put("doctorName", doctorNames.getOrDefault(doctorId, "Doctor ID: " + doctorId));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Current Patients in " + selectedDepartment.getName() + " Department");
            lblRecordCount.setText(reportData.size() + " patients found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Department: ").append(selectedDepartment.getName())
                    .append(" (").append(selectedDepartment.getDepartmentCode()).append(")\n");
            summary.append("Current Patients: ").append(reportData.size()).append("\n");

            // Count patients by ward
            Map<Integer, Integer> patientsByWard = new HashMap<>();
            for (Map<String, Object> row : reportData) {
                int wardNumber = (Integer) row.get("wardNumber");
                patientsByWard.put(wardNumber, patientsByWard.getOrDefault(wardNumber, 0) + 1);
            }

            if (!patientsByWard.isEmpty()) {
                summary.append("Patients by Ward:\n");
                patientsByWard.forEach((ward, count) ->
                        summary.append("  Ward ").append(ward).append(": ").append(count).append(" patients\n"));
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated department patients report for " + selectedDepartment.getName());
        } catch (Exception e) {
            logger.error("Error generating department report", e);
            AlertUtils.showError("Report Error", "Failed to generate department report", e.getMessage());
            setStatus("Error generating department report");
        }
    }

    /**
     * Handles the action to generate a date range report.
     */
    @FXML
    private void handleDateRangeReportAction(ActionEvent event) {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        if (startDate == null || endDate == null) {
            AlertUtils.showWarning("Input Required", "Date Range Required",
                    "Please select both start and end dates to generate the report.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            AlertUtils.showWarning("Invalid Input", "Invalid Date Range",
                    "Start date cannot be after end date.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "DATE_RANGE";

            // Clear the report data
            reportData.clear();

            // SQL to get hospitalizations in date range
            String sql = "SELECT h.*, p.first_name, p.surname FROM Hospitalization h " +
                    "JOIN Patient p ON h.patient_id = p.patient_id " +
                    "WHERE (h.admission_date BETWEEN ? AND ?) OR " +
                    "(h.discharge_date BETWEEN ? AND ?) OR " +
                    "(h.admission_date <= ? AND (h.discharge_date IS NULL OR h.discharge_date >= ?)) " +
                    "ORDER BY h.admission_date DESC";

            // Custom query to get hospitalizations in date range
            List<Map<String, Object>> results = getCustomQueryResults(sql,
                    startDate, endDate, startDate, endDate, startDate, endDate);

            // Process each result
            for (Map<String, Object> result : results) {
                Map<String, Object> row = new HashMap<>();

                int patientId = (Integer) result.get("patient_id");
                String firstName = (String) result.get("first_name");
                String surname = (String) result.get("surname");
                String patientName = firstName + " " + surname;

                row.put("patientId", patientId);
                row.put("patientName", patientName);
                row.put("admissionDate", result.get("admission_date"));
                row.put("dischargeDate", result.get("discharge_date"));

                String departmentCode = (String) result.get("department_code");
                row.put("departmentName", departmentNames.getOrDefault(departmentCode, departmentCode));

                row.put("wardNumber", result.get("ward_number"));
                row.put("diagnosis", result.get("diagnosis"));

                int doctorId = (Integer) result.get("doctor_id");
                row.put("doctorName", doctorNames.getOrDefault(doctorId, "Doctor ID: " + doctorId));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Hospitalizations from " + startDate + " to " + endDate);
            lblRecordCount.setText(reportData.size() + " records found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
            summary.append("Total Hospitalizations: ").append(reportData.size()).append("\n");

            // Count admissions and discharges in range
            long admissions = results.stream()
                    .filter(r -> {
                        LocalDate admDate = (LocalDate) r.get("admission_date");
                        return admDate != null &&
                                !admDate.isBefore(startDate) &&
                                !admDate.isAfter(endDate);
                    })
                    .count();

            long discharges = results.stream()
                    .filter(r -> {
                        LocalDate dischDate = (LocalDate) r.get("discharge_date");
                        return dischDate != null &&
                                !dischDate.isBefore(startDate) &&
                                !dischDate.isAfter(endDate);
                    })
                    .count();

            summary.append("Admissions in Period: ").append(admissions).append("\n");
            summary.append("Discharges in Period: ").append(discharges).append("\n");

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated date range report from " + startDate + " to " + endDate);
        } catch (Exception e) {
            logger.error("Error generating date range report", e);
            AlertUtils.showError("Report Error", "Failed to generate date range report", e.getMessage());
            setStatus("Error generating date range report");
        }
    }

    /**
     * Handles the action to generate a diagnosis report.
     */
    @FXML
    private void handleDiagnosisReportAction(ActionEvent event) {
        String diagnosisKeyword = txtDiagnosis.getText().trim();

        if (diagnosisKeyword.isEmpty()) {
            AlertUtils.showWarning("Input Required", "Diagnosis Keyword Required",
                    "Please enter a diagnosis keyword to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "DIAGNOSIS";

            // Clear the report data
            reportData.clear();

            // SQL to get hospitalizations by diagnosis
            String sql = "SELECT h.*, p.first_name, p.surname FROM Hospitalization h " +
                    "JOIN Patient p ON h.patient_id = p.patient_id " +
                    "WHERE h.diagnosis LIKE ? " +
                    "ORDER BY h.admission_date DESC";

            // Custom query to get hospitalizations by diagnosis
            List<Map<String, Object>> results = getCustomQueryResults(sql, "%" + diagnosisKeyword + "%");

            // Process each result
            for (Map<String, Object> result : results) {
                Map<String, Object> row = new HashMap<>();

                int patientId = (Integer) result.get("patient_id");
                String firstName = (String) result.get("first_name");
                String surname = (String) result.get("surname");
                String patientName = firstName + " " + surname;

                row.put("patientId", patientId);
                row.put("patientName", patientName);
                row.put("admissionDate", result.get("admission_date"));
                row.put("dischargeDate", result.get("discharge_date"));

                String departmentCode = (String) result.get("department_code");
                row.put("departmentName", departmentNames.getOrDefault(departmentCode, departmentCode));

                row.put("wardNumber", result.get("ward_number"));
                row.put("diagnosis", result.get("diagnosis"));

                int doctorId = (Integer) result.get("doctor_id");
                row.put("doctorName", doctorNames.getOrDefault(doctorId, "Doctor ID: " + doctorId));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Hospitalizations with Diagnosis: " + diagnosisKeyword);
            lblRecordCount.setText(reportData.size() + " records found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Diagnosis Keyword: ").append(diagnosisKeyword).append("\n");
            summary.append("Total Matching Records: ").append(reportData.size()).append("\n");

            // Count current vs past hospitalizations
            long currentCount = results.stream()
                    .filter(r -> r.get("discharge_date") == null)
                    .count();

            summary.append("Current Hospitalizations: ").append(currentCount).append("\n");
            summary.append("Past Hospitalizations: ").append(reportData.size() - currentCount).append("\n");

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated diagnosis report for keyword: " + diagnosisKeyword);
        } catch (Exception e) {
            logger.error("Error generating diagnosis report", e);
            AlertUtils.showError("Report Error", "Failed to generate diagnosis report", e.getMessage());
            setStatus("Error generating diagnosis report");
        }
    }

    /**
     * Handles the action to generate a report of all current patients.
     */
    @FXML
    private void handleAllCurrentPatientsAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "CURRENT_PATIENTS";

            // Clear the report data
            reportData.clear();

            // SQL to get all current hospitalizations
            String sql = "SELECT h.*, p.first_name, p.surname FROM Hospitalization h " +
                    "JOIN Patient p ON h.patient_id = p.patient_id " +
                    "WHERE h.discharge_date IS NULL " +
                    "ORDER BY h.department_code, h.ward_number, h.bed_number";

            // Custom query to get all current hospitalizations
            List<Map<String, Object>> results = getCustomQueryResults(sql);

            // Process each result
            for (Map<String, Object> result : results) {
                Map<String, Object> row = new HashMap<>();

                int patientId = (Integer) result.get("patient_id");
                String firstName = (String) result.get("first_name");
                String surname = (String) result.get("surname");
                String patientName = firstName + " " + surname;

                row.put("patientId", patientId);
                row.put("patientName", patientName);
                row.put("admissionDate", result.get("admission_date"));

                String departmentCode = (String) result.get("department_code");
                row.put("departmentName", departmentNames.getOrDefault(departmentCode, departmentCode));

                row.put("wardNumber", result.get("ward_number"));
                row.put("bedNumber", result.get("bed_number"));
                row.put("diagnosis", result.get("diagnosis"));

                int doctorId = (Integer) result.get("doctor_id");
                row.put("doctorName", doctorNames.getOrDefault(doctorId, "Doctor ID: " + doctorId));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("All Current Patients");
            lblRecordCount.setText(reportData.size() + " patients found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Total Current Patients: ").append(reportData.size()).append("\n");

            // Count patients by department
            Map<String, Integer> patientsByDepartment = new HashMap<>();
            for (Map<String, Object> result : results) {
                String departmentCode = (String) result.get("department_code");
                patientsByDepartment.put(departmentCode,
                        patientsByDepartment.getOrDefault(departmentCode, 0) + 1);
            }

            if (!patientsByDepartment.isEmpty()) {
                summary.append("Patients by Department:\n");
                patientsByDepartment.forEach((deptCode, count) -> {
                    String deptName = departmentNames.getOrDefault(deptCode, deptCode);
                    summary.append("  ").append(deptName)
                            .append(" (").append(deptCode).append("): ")
                            .append(count).append(" patients\n");
                });
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated report of all current patients");
        } catch (Exception e) {
            logger.error("Error generating current patients report", e);
            AlertUtils.showError("Report Error", "Failed to generate current patients report", e.getMessage());
            setStatus("Error generating current patients report");
        }
    }

    /**
     * Handles the action to generate patient statistics.
     */
    @FXML
    private void handlePatientStatisticsAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "STATISTICS";

            // Clear the report data
            reportData.clear();

            // Get total patients
            int totalPatients = patientDAO.findAll().size();

            // Get current hospitalized patients
            String sqlCurrent = "SELECT COUNT(DISTINCT patient_id) AS count FROM Hospitalization " +
                    "WHERE discharge_date IS NULL";
            List<Map<String, Object>> currentResults = getCustomQueryResults(sqlCurrent);
            int currentPatients = ((Number) currentResults.get(0).get("count")).intValue();

            // Get total hospitalizations
            String sqlTotal = "SELECT COUNT(*) AS count FROM Hospitalization";
            List<Map<String, Object>> totalResults = getCustomQueryResults(sqlTotal);
            int totalHospitalizations = ((Number) totalResults.get(0).get("count")).intValue();

            // Get average length of stay (for completed hospitalizations)
            String sqlAvgStay = "SELECT AVG(DATEDIFF(discharge_date, admission_date)) AS avg_stay " +
                    "FROM Hospitalization WHERE discharge_date IS NOT NULL";
            List<Map<String, Object>> avgStayResults = getCustomQueryResults(sqlAvgStay);
            Double avgStay = null;
            if (avgStayResults.get(0).get("avg_stay") != null) {
                avgStay = ((Number) avgStayResults.get(0).get("avg_stay")).doubleValue();
            }

            // Get department with most patients
            String sqlTopDept = "SELECT department_code, COUNT(*) AS count FROM Hospitalization " +
                    "WHERE discharge_date IS NULL GROUP BY department_code " +
                    "ORDER BY count DESC LIMIT 1";
            List<Map<String, Object>> topDeptResults = getCustomQueryResults(sqlTopDept);
            String topDeptCode = "";
            int topDeptCount = 0;

            if (!topDeptResults.isEmpty()) {
                topDeptCode = (String) topDeptResults.get(0).get("department_code");
                topDeptCount = ((Number) topDeptResults.get(0).get("count")).intValue();
            }

            // Get most common diagnosis
            String sqlTopDiag = "SELECT diagnosis, COUNT(*) AS count FROM Hospitalization " +
                    "GROUP BY diagnosis ORDER BY count DESC LIMIT 1";
            List<Map<String, Object>> topDiagResults = getCustomQueryResults(sqlTopDiag);
            String topDiagnosis = "";
            int topDiagCount = 0;

            if (!topDiagResults.isEmpty()) {
                topDiagnosis = (String) topDiagResults.get(0).get("diagnosis");
                topDiagCount = ((Number) topDiagResults.get(0).get("count")).intValue();
            }

            // Add statistics to the report data
            addStatistic("Total Patients", Integer.toString(totalPatients));
            addStatistic("Currently Hospitalized Patients", Integer.toString(currentPatients));
            addStatistic("Total Hospitalizations", Integer.toString(totalHospitalizations));

            if (avgStay != null) {
                addStatistic("Average Length of Stay", String.format("%.1f days", avgStay));
            }

            if (!topDeptCode.isEmpty()) {
                String deptName = departmentNames.getOrDefault(topDeptCode, topDeptCode);
                addStatistic("Department with Most Patients",
                        deptName + " (" + topDeptCode + "): " + topDeptCount + " patients");
            }

            if (!topDiagnosis.isEmpty()) {
                addStatistic("Most Common Diagnosis",
                        topDiagnosis + " (" + topDiagCount + " cases)");
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Patient Statistics");
            lblRecordCount.setText(reportData.size() + " statistics");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Patient Statistics Summary\n");
            summary.append("Total Patients: ").append(totalPatients)
                    .append(" | Current Patients: ").append(currentPatients).append("\n");

            if (avgStay != null) {
                summary.append("Average Stay Duration: ").append(String.format("%.1f days", avgStay)).append("\n");
            }

            if (!topDeptCode.isEmpty()) {
                String deptName = departmentNames.getOrDefault(topDeptCode, topDeptCode);
                summary.append("Busiest Department: ").append(deptName)
                        .append(" (").append(topDeptCode).append(")")
                        .append(" with ").append(topDeptCount).append(" patients");
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated patient statistics report");
        } catch (Exception e) {
            logger.error("Error generating patient statistics", e);
            AlertUtils.showError("Report Error", "Failed to generate patient statistics", e.getMessage());
            setStatus("Error generating patient statistics");
        }
    }

    /**
     * Adds a statistic to the report data.
     *
     * @param statistic The name of the statistic
     * @param value The value of the statistic
     */
    private void addStatistic(String statistic, String value) {
        Map<String, Object> row = new HashMap<>();
        row.put("statistic", statistic);
        row.put("value", value);
        reportData.add(row);
    }

    /**
     * Executes a custom SQL query and returns the results as a list of maps.
     *
     * @param sql The SQL query to execute
     * @param params The query parameters
     * @return A list of maps containing the query results
     * @throws SQLException If a database error occurs
     */
    private List<Map<String, Object>> getCustomQueryResults(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = hospitalizationDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String) {
                    stmt.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) params[i]);
                } else if (params[i] instanceof LocalDate) {
                    stmt.setDate(i + 1, java.sql.Date.valueOf((LocalDate) params[i]));
                } else {
                    stmt.setObject(i + 1, params[i]);
                }
            }

            // Execute query
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Process results
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);

                        // Convert SQL date to LocalDate
                        if (value instanceof java.sql.Date) {
                            value = ((java.sql.Date) value).toLocalDate();
                        }

                        row.put(columnName, value);
                    }

                    results.add(row);
                }
            }
        }

        return results;
    }

    /**
     * Handles the action to export the current report to a CSV file.
     */
    @FXML
    private void handleExportAction(ActionEvent event) {
        if (reportData.isEmpty()) {
            AlertUtils.showWarning("No Data", "No Report Data to Export",
                    "Please generate a report before exporting.");
            return;
        }

        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Set initial filename based on report type
        String filename = "HealthTrack_Report_" +
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        fileChooser.setInitialFileName(filename);

        // Show save dialog
        File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                List<TableColumn<Map<String, Object>, ?>> columns = tableReport.getColumns();
                StringBuilder header = new StringBuilder();

                for (int i = 0; i < columns.size(); i++) {
                    header.append(columns.get(i).getText());
                    if (i < columns.size() - 1) {
                        header.append(",");
                    }
                }

                writer.println(header.toString());

                // Write data
                for (Map<String, Object> row : reportData) {
                    StringBuilder line = new StringBuilder();

                    for (int i = 0; i < columns.size(); i++) {
                        TableColumn<Map<String, Object>, ?> column = columns.get(i);
                        String propertyName = getPropertyNameForColumn(column.getText());
                        Object value = row.get(propertyName);

                        if (value != null) {
                            // Handle commas and quotes in CSV
                            String valueStr = value.toString();
                            if (valueStr.contains(",") || valueStr.contains("\"")) {
                                valueStr = "\"" + valueStr.replace("\"", "\"\"") + "\"";
                            }
                            line.append(valueStr);
                        }

                        if (i < columns.size() - 1) {
                            line.append(",");
                        }
                    }

                    writer.println(line.toString());
                }

                AlertUtils.showInformation("Export Successful", "Report Exported",
                        "Report has been exported to " + file.getAbsolutePath());

                setStatus("Report exported to " + file.getName());
            } catch (Exception e) {
                logger.error("Error exporting report", e);
                AlertUtils.showError("Export Error", "Failed to export report", e.getMessage());
                setStatus("Error exporting report");
            }
        }
    }

    /**
     * Gets the property name for a column based on its title.
     *
     * @param columnTitle The title of the column
     * @return The property name for the column
     */
    private String getPropertyNameForColumn(String columnTitle) {
        switch (columnTitle) {
            case "Hospitalization ID": return "hospitalizationId";
            case "Patient ID": return "patientId";
            case "Patient Name": return "patientName";
            case "Admission Date": return "admissionDate";
            case "Discharge Date": return "dischargeDate";
            case "Department": return "departmentName";
            case "Ward": return "wardNumber";
            case "Bed": return "bedNumber";
            case "Diagnosis": return "diagnosis";
            case "Doctor": return "doctorName";
            case "Statistic": return "statistic";
            case "Value": return "value";
            default: return columnTitle.toLowerCase().replace(" ", "");
        }
    }
}