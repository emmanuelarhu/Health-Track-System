package main.java.hospital.dao;

import main.java.hospital.model.Hospitalization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Hospitalization entity.
 * Provides CRUD operations for interacting with the Hospitalization table in the database.
 */
public class HospitalizationDAO {
    private static final Logger logger = LogManager.getLogger(HospitalizationDAO.class);
    private final DatabaseConnection dbConnection;

    /**
     * Constructor that initializes the database connection.
     */
    public HospitalizationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserts a new hospitalization into the database.
     *
     * @param hospitalization The Hospitalization object to insert
     * @return The generated hospitalization ID if successful, or empty if failed
     */
    public Optional<Integer> insert(Hospitalization hospitalization) {
        String sql = "INSERT INTO Hospitalization (patient_id, department_code, ward_number, bed_number, " +
                "diagnosis, doctor_id, admission_date, discharge_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, hospitalization.getPatientId());
            stmt.setString(2, hospitalization.getDepartmentCode());
            stmt.setInt(3, hospitalization.getWardNumber());
            stmt.setInt(4, hospitalization.getBedNumber());
            stmt.setString(5, hospitalization.getDiagnosis());
            stmt.setInt(6, hospitalization.getDoctorId());
            stmt.setDate(7, java.sql.Date.valueOf(hospitalization.getAdmissionDate()));

            if (hospitalization.getDischargeDate() != null) {
                stmt.setDate(8, java.sql.Date.valueOf(hospitalization.getDischargeDate()));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("Creating hospitalization failed, no rows affected");
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int hospitalizationId = generatedKeys.getInt(1);
                    hospitalization.setHospitalizationId(hospitalizationId);
                    logger.info("Created hospitalization with ID: {}", hospitalizationId);
                    return Optional.of(hospitalizationId);
                } else {
                    logger.warn("Creating hospitalization failed, no ID obtained");
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting hospitalization", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves a hospitalization by its ID.
     *
     * @param hospitalizationId The ID of the hospitalization to retrieve
     * @return An Optional containing the Hospitalization if found, or empty if not found
     */
    public Optional<Hospitalization> findById(int hospitalizationId) {
        String sql = "SELECT * FROM Hospitalization WHERE hospitalization_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hospitalizationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Hospitalization hospitalization = mapResultSetToHospitalization(rs);
                    logger.info("Found hospitalization with ID: {}", hospitalizationId);
                    return Optional.of(hospitalization);
                } else {
                    logger.info("No hospitalization found with ID: {}", hospitalizationId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding hospitalization by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all hospitalizations from the database.
     *
     * @return A list of all hospitalizations
     */
    public List<Hospitalization> findAll() {
        String sql = "SELECT * FROM Hospitalization ORDER BY hospitalization_id DESC";
        List<Hospitalization> hospitalizations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Hospitalization hospitalization = mapResultSetToHospitalization(rs);
                hospitalizations.add(hospitalization);
            }

            logger.info("Retrieved {} hospitalizations", hospitalizations.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all hospitalizations", e);
        }

        return hospitalizations;
    }

    /**
     * Retrieves current hospitalizations (with no discharge date) from the database.
     *
     * @return A list of current hospitalizations
     */
    public List<Hospitalization> findCurrentHospitalizations() {
        String sql = "SELECT * FROM Hospitalization WHERE discharge_date IS NULL ORDER BY hospitalization_id DESC";
        List<Hospitalization> hospitalizations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Hospitalization hospitalization = mapResultSetToHospitalization(rs);
                hospitalizations.add(hospitalization);
            }

            logger.info("Retrieved {} current hospitalizations", hospitalizations.size());
        } catch (SQLException e) {
            logger.error("Error retrieving current hospitalizations", e);
        }

        return hospitalizations;
    }

    /**
     * Updates an existing hospitalization in the database.
     *
     * @param hospitalization The Hospitalization object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Hospitalization hospitalization) {
        String sql = "UPDATE Hospitalization SET patient_id = ?, department_code = ?, ward_number = ?, " +
                "bed_number = ?, diagnosis = ?, doctor_id = ?, admission_date = ?, discharge_date = ? " +
                "WHERE hospitalization_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hospitalization.getPatientId());
            stmt.setString(2, hospitalization.getDepartmentCode());
            stmt.setInt(3, hospitalization.getWardNumber());
            stmt.setInt(4, hospitalization.getBedNumber());
            stmt.setString(5, hospitalization.getDiagnosis());
            stmt.setInt(6, hospitalization.getDoctorId());
            stmt.setDate(7, java.sql.Date.valueOf(hospitalization.getAdmissionDate()));

            if (hospitalization.getDischargeDate() != null) {
                stmt.setDate(8, java.sql.Date.valueOf(hospitalization.getDischargeDate()));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }

            stmt.setInt(9, hospitalization.getHospitalizationId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated hospitalization with ID: {}", hospitalization.getHospitalizationId());
                return true;
            } else {
                logger.warn("No hospitalization found with ID: {}", hospitalization.getHospitalizationId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating hospitalization", e);
            return false;
        }
    }

    /**
     * Discharges a patient by setting the discharge date for a hospitalization.
     *
     * @param hospitalizationId The ID of the hospitalization to update
     * @param dischargeDate The discharge date to set
     * @return true if successful, false otherwise
     */
    public boolean dischargePatient(int hospitalizationId, LocalDate dischargeDate) {
        String sql = "UPDATE Hospitalization SET discharge_date = ? WHERE hospitalization_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(dischargeDate));
            stmt.setInt(2, hospitalizationId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Discharged patient with hospitalization ID: {}", hospitalizationId);
                return true;
            } else {
                logger.warn("No hospitalization found with ID: {}", hospitalizationId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error discharging patient", e);
            return false;
        }
    }

    /**
     * Deletes a hospitalization from the database.
     *
     * @param hospitalizationId The ID of the hospitalization to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int hospitalizationId) {
        String sql = "DELETE FROM Hospitalization WHERE hospitalization_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hospitalizationId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted hospitalization with ID: {}", hospitalizationId);
                return true;
            } else {
                logger.warn("No hospitalization found with ID: {}", hospitalizationId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting hospitalization", e);
            return false;
        }
    }

    /**
     * Finds hospitalizations by patient.
     *
     * @param patientId The ID of the patient to search for
     * @return A list of hospitalizations for the specified patient
     */
    public List<Hospitalization> findByPatient(int patientId) {
        String sql = "SELECT * FROM Hospitalization WHERE patient_id = ? ORDER BY admission_date DESC";
        List<Hospitalization> hospitalizations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Hospitalization hospitalization = mapResultSetToHospitalization(rs);
                    hospitalizations.add(hospitalization);
                }
            }

            logger.info("Found {} hospitalizations for patient with ID: {}",
                    hospitalizations.size(), patientId);
        } catch (SQLException e) {
            logger.error("Error finding hospitalizations by patient", e);
        }

        return hospitalizations;
    }

    /**
     * Finds hospitalizations by doctor.
     *
     * @param doctorId The ID of the doctor to search for
     * @return A list of hospitalizations under the specified doctor
     */
    public List<Hospitalization> findByDoctor(int doctorId) {
        String sql = "SELECT * FROM Hospitalization WHERE doctor_id = ? ORDER BY admission_date DESC";
        List<Hospitalization> hospitalizations = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Hospitalization hospitalization = mapResultSetToHospitalization(rs);
                    hospitalizations.add(hospitalization);
                }
            }

            logger.info("Found {} hospitalizations under doctor with ID: {}",
                    hospitalizations.size(), doctorId);
        } catch (SQLException e) {
            logger.error("Error finding hospitalizations by doctor", e);
        }

        return hospitalizations;
    }

    /**
     * Checks if a bed is currently occupied.
     *
     * @param departmentCode The department code
     * @param wardNumber The ward number
     * @param bedNumber The bed number
     * @return true if the bed is occupied, false otherwise
     */
    public boolean isBedOccupied(String departmentCode, int wardNumber, int bedNumber) {
        String sql = "SELECT COUNT(*) FROM Hospitalization " +
                "WHERE department_code = ? AND ward_number = ? AND bed_number = ? AND discharge_date IS NULL";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);
            stmt.setInt(2, wardNumber);
            stmt.setInt(3, bedNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking if bed is occupied", e);
        }

        return false;
    }

    /**
     * Helper method to map a ResultSet row to a Hospitalization object.
     *
     * @param rs The ResultSet containing hospitalization data
     * @return A new Hospitalization object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Hospitalization mapResultSetToHospitalization(ResultSet rs) throws SQLException {
        Hospitalization hospitalization = new Hospitalization();
        hospitalization.setHospitalizationId(rs.getInt("hospitalization_id"));
        hospitalization.setPatientId(rs.getInt("patient_id"));
        hospitalization.setDepartmentCode(rs.getString("department_code"));
        hospitalization.setWardNumber(rs.getInt("ward_number"));
        hospitalization.setBedNumber(rs.getInt("bed_number"));
        hospitalization.setDiagnosis(rs.getString("diagnosis"));
        hospitalization.setDoctorId(rs.getInt("doctor_id"));

        // Convert SQL dates to LocalDate
        Date admissionDate = rs.getDate("admission_date");
        if (admissionDate != null) {
            hospitalization.setAdmissionDate(admissionDate.toLocalDate());
        }

        Date dischargeDate = rs.getDate("discharge_date");
        if (dischargeDate != null) {
            hospitalization.setDischargeDate(dischargeDate.toLocalDate());
        }

        // Convert SQL timestamps to LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            hospitalization.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            hospitalization.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return hospitalization;
    }

    /**
     * Gets a connection to the database.
     *
     * @return A Connection object
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
}