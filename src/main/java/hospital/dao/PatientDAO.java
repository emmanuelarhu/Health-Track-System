package main.java.hospital.dao;

import main.java.hospital.model.Patient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Patient entity.
 * Provides CRUD operations for interacting with the Patient table in the database.
 */
public class PatientDAO {
    private static final Logger logger = LogManager.getLogger(PatientDAO.class);
    private final DatabaseConnection dbConnection;

    /**
     * Constructor that initializes the database connection.
     */
    public PatientDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserts a new patient into the database.
     *
     * @param patient The Patient object to insert
     * @return The generated patient ID if successful, or empty if failed
     */
    public Optional<Integer> insert(Patient patient) {
        String sql = "INSERT INTO Patient (first_name, surname, address, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getSurname());
            stmt.setString(3, patient.getAddress());
            stmt.setString(4, patient.getPhone());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("Creating patient failed, no rows affected");
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int patientId = generatedKeys.getInt(1);
                    patient.setPatientId(patientId);
                    logger.info("Created patient with ID: {}", patientId);
                    return Optional.of(patientId);
                } else {
                    logger.warn("Creating patient failed, no ID obtained");
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting patient", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param patientId The ID of the patient to retrieve
     * @return An Optional containing the Patient if found, or empty if not found
     */
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT * FROM Patient WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Patient patient = mapResultSetToPatient(rs);
                    logger.info("Found patient with ID: {}", patientId);
                    return Optional.of(patient);
                } else {
                    logger.info("No patient found with ID: {}", patientId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all patients from the database.
     *
     * @return A list of all patients
     */
    public List<Patient> findAll() {
        String sql = "SELECT * FROM Patient";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = mapResultSetToPatient(rs);
                patients.add(patient);
            }

            logger.info("Retrieved {} patients", patients.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all patients", e);
        }

        return patients;
    }

    /**
     * Updates an existing patient in the database.
     *
     * @param patient The Patient object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Patient patient) {
        String sql = "UPDATE Patient SET first_name = ?, surname = ?, address = ?, phone = ? WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getSurname());
            stmt.setString(3, patient.getAddress());
            stmt.setString(4, patient.getPhone());
            stmt.setInt(5, patient.getPatientId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated patient with ID: {}", patient.getPatientId());
                return true;
            } else {
                logger.warn("No patient found with ID: {}", patient.getPatientId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating patient", e);
            return false;
        }
    }

    /**
     * Deletes a patient from the database.
     *
     * @param patientId The ID of the patient to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int patientId) {
        String sql = "DELETE FROM Patient WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted patient with ID: {}", patientId);
                return true;
            } else {
                logger.warn("No patient found with ID: {}", patientId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting patient", e);
            return false;
        }
    }

    /**
     * Searches for patients by name (first name or surname).
     *
     * @param searchTerm The search term to look for in first name or surname
     * @return A list of matching patients
     */
    public List<Patient> searchByName(String searchTerm) {
        String sql = "SELECT * FROM Patient WHERE first_name LIKE ? OR surname LIKE ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = mapResultSetToPatient(rs);
                    patients.add(patient);
                }
            }

            logger.info("Found {} patients matching search term: {}", patients.size(), searchTerm);
        } catch (SQLException e) {
            logger.error("Error searching patients by name", e);
        }

        return patients;
    }

    /**
     * Helper method to map a ResultSet row to a Patient object.
     *
     * @param rs The ResultSet containing patient data
     * @return A new Patient object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setSurname(rs.getString("surname"));
        patient.setAddress(rs.getString("address"));
        patient.setPhone(rs.getString("phone"));

        // Convert SQL timestamps to LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return patient;
    }
}