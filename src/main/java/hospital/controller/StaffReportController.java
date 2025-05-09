package main.java.hospital.controller;

import javafx.beans.value.ObservableValue;
import main.java.hospital.dao.*;
import main.java.hospital.model.*;
import main.java.hospital.util.AlertUtils;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for the Staff Reports view.
 * Handles generation of various staff-related reports.
 */
public class StaffReportController implements Initializable {
    private static final Logger logger = LogManager.getLogger(StaffReportController.class);

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NurseDAO nurseDAO = new NurseDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final WardDAO wardDAO = new WardDAO();
    private final HospitalizationDAO hospitalizationDAO = new HospitalizationDAO();

    private ObservableList<Doctor> doctorList;
    private ObservableList<Nurse> nurseList;
    private ObservableList<Department> departmentList;
    private Map<String, String> departmentNames = new HashMap<>();

    private ObservableList<Map<String, Object>> reportData = FXCollections.observableArrayList();
    private String currentReportType = "";

    @FXML private ComboBox<Department> cmbDepartment;
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private ComboBox<String> cmbRotation;
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
        logger.info("Initializing StaffReportController");

        // Load reference data
        loadDoctorData();
        loadNurseData();
        loadDepartmentData();

        // Initialize rotation combobox
        cmbRotation.setItems(FXCollections.observableArrayList("Morning", "Evening", "Night"));

        setStatus("Staff report module loaded");
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
     * Loads doctor data for the combobox.
     */
    private void loadDoctorData() {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            doctorList = FXCollections.observableArrayList(doctors);
            cmbDoctor.setItems(doctorList);

            // Set up the cell factory for doctor display
            cmbDoctor.setCellFactory(param -> new ListCell<Doctor>() {
                @Override
                protected void updateItem(Doctor item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName() + " (" + item.getSpeciality() + ")");
                    }
                }
            });

            logger.info("Loaded {} doctors for combobox", doctors.size());
        } catch (Exception e) {
            logger.error("Error loading doctor data", e);
            AlertUtils.showError("Database Error", "Failed to load doctor data", e.getMessage());
        }
    }

    /**
     * Loads nurse data for the combobox.
     */
    private void loadNurseData() {
        try {
            List<Nurse> nurses = nurseDAO.findAll();
            nurseList = FXCollections.observableArrayList(nurses);

            logger.info("Loaded {} nurses", nurses.size());
        } catch (Exception e) {
            logger.error("Error loading nurse data", e);
            AlertUtils.showError("Database Error", "Failed to load nurse data", e.getMessage());
        }
    }

    /**
     * Loads department data for the combobox and name mapping.
     */
    private void loadDepartmentData() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentList = FXCollections.observableArrayList(departments);
            cmbDepartment.setItems(departmentList);

            // Set up the cell factory for department display
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
     * Sets up the report table with appropriate columns for the current report type.
     */
    private void setupReportTable() {
        // Clear existing columns
        tableReport.getColumns().clear();

        // Add columns based on report type
        switch (currentReportType) {
            case "DOCTOR_LIST":
                addTableColumn("ID", "employeeId");
                addTableColumn("First Name", "firstName");
                addTableColumn("Surname", "surname");
                addTableColumn("Speciality", "speciality");
                addTableColumn("Phone", "phone");
                break;

            case "NURSE_LIST":
                addTableColumn("ID", "employeeId");
                addTableColumn("First Name", "firstName");
                addTableColumn("Surname", "surname");
                addTableColumn("Rotation", "rotation");
                addTableColumn("Department", "departmentName");
                addTableColumn("Salary", "salary");
                break;

            case "DEPARTMENT_STAFF":
                addTableColumn("ID", "employeeId");
                addTableColumn("Name", "fullName");
                addTableColumn("Role", "role");
                addTableColumn("Speciality/Rotation", "specialityRotation");
                addTableColumn("Phone", "phone");
                break;

            case "DOCTOR_PATIENTS":
                addTableColumn("Patient ID", "patientId");
                addTableColumn("Patient Name", "patientName");
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward", "wardNumber");
                addTableColumn("Diagnosis", "diagnosis");
                addTableColumn("Admission Date", "admissionDate");
                addTableColumn("Discharge Date", "dischargeDate");
                break;

            case "WARD_SUPERVISORS":
                addTableColumn("Department", "departmentName");
                addTableColumn("Ward Number", "wardNumber");
                addTableColumn("Bed Count", "bedCount");
                addTableColumn("Supervisor ID", "supervisorId");
                addTableColumn("Supervisor Name", "supervisorName");
                break;

            case "STAFF_BY_ROTATION":
                addTableColumn("ID", "employeeId");
                addTableColumn("Name", "fullName");
                addTableColumn("Department", "departmentName");
                addTableColumn("Salary", "salary");
                addTableColumn("Phone", "phone");
                break;

            case "STAFF_STATISTICS":
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

            // We need to ensure both branches return the same type
            if (value instanceof String) {
                // Use double casting to ensure type compatibility
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
     * Handles the action to generate a list of all doctors.
     */
    @FXML
    private void handleDoctorListAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "DOCTOR_LIST";

            // Clear the report data
            reportData.clear();

            // Process each doctor
            for (Doctor doctor : doctorList) {
                Map<String, Object> row = new HashMap<>();
                row.put("employeeId", doctor.getEmployeeId());
                row.put("firstName", doctor.getFirstName());
                row.put("surname", doctor.getSurname());
                row.put("speciality", doctor.getSpeciality());
                row.put("phone", doctor.getPhone());

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("All Doctors");
            lblRecordCount.setText(reportData.size() + " doctors found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Total Doctors: ").append(reportData.size()).append("\n");

            // Count doctors by speciality
            Map<String, Integer> doctorsBySpeciality = new HashMap<>();
            for (Doctor doctor : doctorList) {
                String speciality = doctor.getSpeciality();
                doctorsBySpeciality.put(speciality, doctorsBySpeciality.getOrDefault(speciality, 0) + 1);
            }

            if (!doctorsBySpeciality.isEmpty()) {
                summary.append("Doctors by Speciality:\n");
                doctorsBySpeciality.forEach((speciality, count) ->
                        summary.append("  ").append(speciality).append(": ").append(count).append("\n"));
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated doctor list report");
        } catch (Exception e) {
            logger.error("Error generating doctor list report", e);
            AlertUtils.showError("Report Error", "Failed to generate doctor list report", e.getMessage());
            setStatus("Error generating doctor list report");
        }
    }

    /**
     * Handles the action to generate a list of all nurses.
     */
    @FXML
    private void handleNurseListAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "NURSE_LIST";

            // Clear the report data
            reportData.clear();

            // Process each nurse
            for (Nurse nurse : nurseList) {
                Map<String, Object> row = new HashMap<>();
                row.put("employeeId", nurse.getEmployeeId());
                row.put("firstName", nurse.getFirstName());
                row.put("surname", nurse.getSurname());
                row.put("rotation", nurse.getRotation());
                row.put("departmentName", departmentNames.getOrDefault(nurse.getDepartmentCode(),
                        nurse.getDepartmentCode()));
                row.put("salary", nurse.getSalary());

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("All Nurses");
            lblRecordCount.setText(reportData.size() + " nurses found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Total Nurses: ").append(reportData.size()).append("\n");

            // Count nurses by department
            Map<String, Integer> nursesByDepartment = new HashMap<>();
            for (Nurse nurse : nurseList) {
                String departmentCode = nurse.getDepartmentCode();
                String departmentName = departmentNames.getOrDefault(departmentCode, departmentCode);
                nursesByDepartment.put(departmentName, nursesByDepartment.getOrDefault(departmentName, 0) + 1);
            }

            if (!nursesByDepartment.isEmpty()) {
                summary.append("Nurses by Department:\n");
                nursesByDepartment.forEach((department, count) ->
                        summary.append("  ").append(department).append(": ").append(count).append("\n"));
            }

            // Count nurses by rotation
            Map<String, Integer> nursesByRotation = new HashMap<>();
            for (Nurse nurse : nurseList) {
                String rotation = nurse.getRotation();
                nursesByRotation.put(rotation, nursesByRotation.getOrDefault(rotation, 0) + 1);
            }

            if (!nursesByRotation.isEmpty()) {
                summary.append("Nurses by Rotation:\n");
                nursesByRotation.forEach((rotation, count) ->
                        summary.append("  ").append(rotation).append(": ").append(count).append("\n"));
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated nurse list report");
        } catch (Exception e) {
            logger.error("Error generating nurse list report", e);
            AlertUtils.showError("Report Error", "Failed to generate nurse list report", e.getMessage());
            setStatus("Error generating nurse list report");
        }
    }

    /**
     * Handles the action to generate a report of staff in a department.
     */
    @FXML
    private void handleDepartmentStaffAction(ActionEvent event) {
        Department selectedDepartment = cmbDepartment.getValue();

        if (selectedDepartment == null) {
            AlertUtils.showWarning("Input Required", "Department Selection Required",
                    "Please select a department to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "DEPARTMENT_STAFF";

            // Clear the report data
            reportData.clear();

            String departmentCode = selectedDepartment.getDepartmentCode();

            // Get doctors who are directors of this department
            List<Doctor> allDoctors = doctorDAO.findAll();
            for (Doctor doctor : allDoctors) {
                // Include only the department director
                if (selectedDepartment.getDirectorId() != null &&
                        selectedDepartment.getDirectorId().equals(doctor.getEmployeeId())) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employeeId", doctor.getEmployeeId());
                    row.put("fullName", doctor.getFullName());
                    row.put("role", "Doctor (Director)");
                    row.put("specialityRotation", doctor.getSpeciality());
                    row.put("phone", doctor.getPhone());
                    reportData.add(row);
                }
            }

            // Get nurses in this department
            List<Nurse> departmentNurses = nurseDAO.findByDepartment(departmentCode);
            for (Nurse nurse : departmentNurses) {
                Map<String, Object> row = new HashMap<>();
                row.put("employeeId", nurse.getEmployeeId());
                row.put("fullName", nurse.getFullName());

                // Check if this nurse is a ward supervisor
                List<Ward> supervisedWards = wardDAO.findBySupervisor(nurse.getEmployeeId());
                if (!supervisedWards.isEmpty()) {
                    row.put("role", "Nurse (Ward Supervisor)");
                } else {
                    row.put("role", "Nurse");
                }

                row.put("specialityRotation", nurse.getRotation());
                row.put("phone", nurse.getPhone());
                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Staff in " + selectedDepartment.getName() + " Department");
            lblRecordCount.setText(reportData.size() + " staff members found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Department: ").append(selectedDepartment.getName())
                    .append(" (").append(selectedDepartment.getDepartmentCode()).append(")\n");
            summary.append("Building: ").append(selectedDepartment.getBuilding()).append("\n");

            long directorCount = reportData.stream()
                    .filter(row -> row.get("role").equals("Doctor (Director)"))
                    .count();

            long supervisorCount = reportData.stream()
                    .filter(row -> row.get("role").equals("Nurse (Ward Supervisor)"))
                    .count();

            long nurseCount = reportData.stream()
                    .filter(row -> row.get("role").equals("Nurse"))
                    .count();

            summary.append("Department Director: ").append(directorCount > 0 ? "Yes" : "No").append("\n");
            summary.append("Ward Supervisors: ").append(supervisorCount).append("\n");
            summary.append("Staff Nurses: ").append(nurseCount).append("\n");
            summary.append("Total Staff: ").append(reportData.size()).append("\n");

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated department staff report");
        } catch (Exception e) {
            logger.error("Error generating department staff report", e);
            AlertUtils.showError("Report Error", "Failed to generate department staff report", e.getMessage());
            setStatus("Error generating department staff report");
        }
    }

    /**
     * Handles the action to generate a report of patients under a doctor.
     */
    @FXML
    private void handleDoctorPatientsAction(ActionEvent event) {
        Doctor selectedDoctor = cmbDoctor.getValue();

        if (selectedDoctor == null) {
            AlertUtils.showWarning("Input Required", "Doctor Selection Required",
                    "Please select a doctor to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "DOCTOR_PATIENTS";

            // Clear the report data
            reportData.clear();

            // Get hospitalizations under this doctor
            List<Hospitalization> hospitalizations = hospitalizationDAO.findByDoctor(selectedDoctor.getEmployeeId());

            // Create a PatientDAO to get patient names
            PatientDAO patientDAO = new PatientDAO();
            Map<Integer, String> patientNames = new HashMap<>();

            // Process each hospitalization
            for (Hospitalization h : hospitalizations) {
                Map<String, Object> row = new HashMap<>();

                // Get patient name if not already cached
                if (!patientNames.containsKey(h.getPatientId())) {
                    Optional<Patient> patient = patientDAO.findById(h.getPatientId());
                    patient.ifPresent(p -> patientNames.put(p.getPatientId(), p.getFullName()));
                }

                row.put("patientId", h.getPatientId());
                row.put("patientName", patientNames.getOrDefault(h.getPatientId(), "Unknown"));
                row.put("departmentName", departmentNames.getOrDefault(h.getDepartmentCode(), h.getDepartmentCode()));
                row.put("wardNumber", h.getWardNumber());
                row.put("diagnosis", h.getDiagnosis());
                row.put("admissionDate", h.getAdmissionDate());
                row.put("dischargeDate", h.getDischargeDate());

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Patients under Dr. " + selectedDoctor.getSurname());
            lblRecordCount.setText(reportData.size() + " patients found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Doctor: ").append(selectedDoctor.getFullName()).append("\n");
            summary.append("Speciality: ").append(selectedDoctor.getSpeciality()).append("\n");
            summary.append("Total Patients Treated: ").append(reportData.size()).append("\n");

            long currentPatients = hospitalizations.stream()
                    .filter(h -> h.getDischargeDate() == null)
                    .count();

            summary.append("Current Patients: ").append(currentPatients).append("\n");
            summary.append("Discharged Patients: ").append(reportData.size() - currentPatients).append("\n");

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated doctor patients report");
        } catch (Exception e) {
            logger.error("Error generating doctor patients report", e);
            AlertUtils.showError("Report Error", "Failed to generate doctor patients report", e.getMessage());
            setStatus("Error generating doctor patients report");
        }
    }

    /**
     * Handles the action to generate a report of ward supervisors.
     */
    @FXML
    private void handleWardSupervisorsAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "WARD_SUPERVISORS";

            // Clear the report data
            reportData.clear();

            // Get all wards
            List<Ward> wards = wardDAO.findAll();

            // Get all nurses for supervisor lookup
            Map<Integer, String> supervisorNames = new HashMap<>();
            for (Nurse nurse : nurseList) {
                supervisorNames.put(nurse.getEmployeeId(), nurse.getFullName());
            }

            // Process each ward
            for (Ward ward : wards) {
                Map<String, Object> row = new HashMap<>();
                row.put("departmentName", departmentNames.getOrDefault(
                        ward.getDepartmentCode(), ward.getDepartmentCode()));
                row.put("wardNumber", ward.getWardNumber());
                row.put("bedCount", ward.getBedCount());
                row.put("supervisorId", ward.getSupervisorId());
                row.put("supervisorName", supervisorNames.getOrDefault(
                        ward.getSupervisorId(), "Unknown"));

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Ward Supervisors");
            lblRecordCount.setText(reportData.size() + " wards found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Total Wards: ").append(reportData.size()).append("\n");

            // Count wards by department
            Map<String, Integer> wardsByDepartment = new HashMap<>();
            for (Ward ward : wards) {
                String departmentName = departmentNames.getOrDefault(
                        ward.getDepartmentCode(), ward.getDepartmentCode());
                wardsByDepartment.put(departmentName,
                        wardsByDepartment.getOrDefault(departmentName, 0) + 1);
            }

            if (!wardsByDepartment.isEmpty()) {
                summary.append("Wards by Department:\n");
                wardsByDepartment.forEach((department, count) ->
                        summary.append("  ").append(department).append(": ").append(count).append("\n"));
            }

            // Calculate total bed capacity
            int totalBeds = wards.stream().mapToInt(Ward::getBedCount).sum();
            summary.append("Total Bed Capacity: ").append(totalBeds).append("\n");

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated ward supervisors report");
        } catch (Exception e) {
            logger.error("Error generating ward supervisors report", e);
            AlertUtils.showError("Report Error", "Failed to generate ward supervisors report", e.getMessage());
            setStatus("Error generating ward supervisors report");
        }
    }

    /**
     * Handles the action to generate a report of nurses by rotation.
     */
    @FXML
    private void handleStaffByRotationAction(ActionEvent event) {
        String selectedRotation = cmbRotation.getValue();

        if (selectedRotation == null) {
            AlertUtils.showWarning("Input Required", "Rotation Selection Required",
                    "Please select a rotation to generate the report.");
            return;
        }

        try {
            // Set the current report type
            currentReportType = "STAFF_BY_ROTATION";

            // Clear the report data
            reportData.clear();

            // Get nurses with the selected rotation
            List<Nurse> rotationNurses = nurseDAO.findByRotation(selectedRotation);

            // Process each nurse
            for (Nurse nurse : rotationNurses) {
                Map<String, Object> row = new HashMap<>();
                row.put("employeeId", nurse.getEmployeeId());
                row.put("fullName", nurse.getFullName());
                row.put("departmentName", departmentNames.getOrDefault(
                        nurse.getDepartmentCode(), nurse.getDepartmentCode()));
                row.put("salary", nurse.getSalary());
                row.put("phone", nurse.getPhone());

                reportData.add(row);
            }

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Nurses on " + selectedRotation + " Rotation");
            lblRecordCount.setText(reportData.size() + " nurses found");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Rotation: ").append(selectedRotation).append("\n");
            summary.append("Total Nurses: ").append(reportData.size()).append("\n");

            // Count nurses by department
            Map<String, Integer> nursesByDepartment = new HashMap<>();
            for (Nurse nurse : rotationNurses) {
                String departmentName = departmentNames.getOrDefault(
                        nurse.getDepartmentCode(), nurse.getDepartmentCode());
                nursesByDepartment.put(departmentName,
                        nursesByDepartment.getOrDefault(departmentName, 0) + 1);
            }

            if (!nursesByDepartment.isEmpty()) {
                summary.append("Nurses by Department:\n");
                nursesByDepartment.forEach((department, count) ->
                        summary.append("  ").append(department).append(": ").append(count).append("\n"));
            }

            // Calculate average salary
            if (!rotationNurses.isEmpty()) {
                double totalSalary = rotationNurses.stream()
                        .mapToDouble(n -> n.getSalary().doubleValue())
                        .sum();
                double avgSalary = totalSalary / rotationNurses.size();
                summary.append("Average Salary: $").append(String.format("%.2f", avgSalary)).append("\n");
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated nurses by rotation report");
        } catch (Exception e) {
            logger.error("Error generating nurses by rotation report", e);
            AlertUtils.showError("Report Error", "Failed to generate nurses by rotation report", e.getMessage());
            setStatus("Error generating nurses by rotation report");
        }
    }

    /**
     * Handles the action to generate staff statistics.
     */
    @FXML
    private void handleStaffStatisticsAction(ActionEvent event) {
        try {
            // Set the current report type
            currentReportType = "STAFF_STATISTICS";

            // Clear the report data
            reportData.clear();

            // Get all employees
            List<Employee> employees = new ArrayList<>();
            employees.addAll(doctorList);
            employees.addAll(nurseList);

            // Declare avgSalary at the method level
            double avgSalary = 0.0;
            boolean hasSalaryData = false;

            // Add statistics to the report data
            addStatistic("Total Staff", Integer.toString(employees.size()));
            addStatistic("Doctors", Integer.toString(doctorList.size()));
            addStatistic("Nurses", Integer.toString(nurseList.size()));

            // Department statistics
            addStatistic("Departments", Integer.toString(departmentList.size()));

            // Nurse rotation statistics
            Map<String, Long> rotationCounts = new HashMap<>();
            for (Nurse nurse : nurseList) {
                String rotation = nurse.getRotation();
                rotationCounts.put(rotation, rotationCounts.getOrDefault(rotation, 0L) + 1);
            }

            for (Map.Entry<String, Long> entry : rotationCounts.entrySet()) {
                addStatistic(entry.getKey() + " Rotation Nurses", Long.toString(entry.getValue()));
            }

            // Doctor speciality statistics
            Map<String, Long> specialityCounts = new HashMap<>();
            for (Doctor doctor : doctorList) {
                String speciality = doctor.getSpeciality();
                specialityCounts.put(speciality, specialityCounts.getOrDefault(speciality, 0L) + 1);
            }

            for (Map.Entry<String, Long> entry : specialityCounts.entrySet()) {
                addStatistic(entry.getKey() + " Doctors", Long.toString(entry.getValue()));
            }

            // Average nurse salary
            if (!nurseList.isEmpty()) {
                double totalSalary = nurseList.stream()
                        .mapToDouble(n -> n.getSalary().doubleValue())
                        .sum();
                avgSalary = totalSalary / nurseList.size();
                hasSalaryData = true;
                addStatistic("Average Nurse Salary", String.format("$%.2f", avgSalary));
            }

            // Ward statistics
            List<Ward> wards = wardDAO.findAll();
            addStatistic("Total Wards", Integer.toString(wards.size()));

            // Total bed capacity
            int totalBeds = wards.stream().mapToInt(Ward::getBedCount).sum();
            addStatistic("Total Bed Capacity", Integer.toString(totalBeds));

            // Set up the report table
            setupReportTable();

            // Update report title and record count
            lblReportTitle.setText("Staff Statistics");
            lblRecordCount.setText(reportData.size() + " statistics");

            // Generate summary
            StringBuilder summary = new StringBuilder();
            summary.append("Staff Statistics Summary\n");
            summary.append("Total Staff: ").append(employees.size())
                    .append(" (Doctors: ").append(doctorList.size())
                    .append(", Nurses: ").append(nurseList.size()).append(")\n");

            summary.append("Departments: ").append(departmentList.size()).append("\n");
            summary.append("Wards: ").append(wards.size()).append(" (Total Capacity: ").append(totalBeds).append(" beds)\n");

            if (hasSalaryData) {
                summary.append("Average Nurse Salary: $").append(String.format("%.2f", avgSalary)).append("\n");
            }

            // Set the summary text
            txtReportSummary.setText(summary.toString());

            setStatus("Generated staff statistics report");
        } catch (Exception e) {
            logger.error("Error generating staff statistics", e);
            AlertUtils.showError("Report Error", "Failed to generate staff statistics", e.getMessage());
            setStatus("Error generating staff statistics");
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
        String filename = "HealthTrack_StaffReport_" +
                LocalDate.now().toString() + ".csv";
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
            case "ID": return "employeeId";
            case "First Name": return "firstName";
            case "Surname": return "surname";
            case "Name": return "fullName";
            case "Speciality": return "speciality";
            case "Rotation": return "rotation";
            case "Department": return "departmentName";
            case "Ward Number": return "wardNumber";
            case "Bed Count": return "bedCount";
            case "Supervisor ID": return "supervisorId";
            case "Supervisor Name": return "supervisorName";
            case "Phone": return "phone";
            case "Salary": return "salary";
            case "Role": return "role";
            case "Speciality/Rotation": return "specialityRotation";
            case "Patient ID": return "patientId";
            case "Patient Name": return "patientName";
            case "Diagnosis": return "diagnosis";
            case "Admission Date": return "admissionDate";
            case "Discharge Date": return "dischargeDate";
            case "Statistic": return "statistic";
            case "Value": return "value";
            default: return columnTitle.toLowerCase().replace(" ", "");
        }
    }
}