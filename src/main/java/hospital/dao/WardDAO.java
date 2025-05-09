package main.java.hospital.dao;

import main.java.hospital.model.Ward;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Ward entity.
 * Provides CRUD operations for interacting with the Ward table in the database.
 */
public class WardDAO {
    private static final Logger logger = LogManager.getLogger(WardDAO.class);
    private final DatabaseConnection dbConnection;

    /**
     * Constructor that initializes the database connection.
     */
    public WardDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserts a new ward into the database.
     *
     * @param ward The Ward object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Ward ward) {
        String sql = "INSERT INTO Ward (department_code, ward_number, bed_count, supervisor_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ward.getDepartmentCode());
            stmt.setInt(2, ward.getWardNumber());
            stmt.setInt(3, ward.getBedCount());
            stmt.setInt(4, ward.getSupervisorId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Created ward with department code: {} and ward number: {}",
                        ward.getDepartmentCode(), ward.getWardNumber());
                return true;
            } else {
                logger.warn("Creating ward failed, no rows affected");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error inserting ward", e);
            return false;
        }
    }

    /**
     * Retrieves a ward by its composite ID (department_code, ward_number).
     *
     * @param departmentCode The department code
     * @param wardNumber The ward number
     * @return An Optional containing the Ward if found, or empty if not found
     */
    public Optional<Ward> findById(String departmentCode, int wardNumber) {
        String sql = "SELECT * FROM Ward WHERE department_code = ? AND ward_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);
            stmt.setInt(2, wardNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Ward ward = mapResultSetToWard(rs);
                    logger.info("Found ward with department code: {} and ward number: {}",
                            departmentCode, wardNumber);
                    return Optional.of(ward);
                } else {
                    logger.info("No ward found with department code: {} and ward number: {}",
                            departmentCode, wardNumber);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding ward by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all wards from the database.
     *
     * @return A list of all wards
     */
    public List<Ward> findAll() {
        String sql = "SELECT * FROM Ward ORDER BY department_code, ward_number";
        List<Ward> wards = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ward ward = mapResultSetToWard(rs);
                wards.add(ward);
            }

            logger.info("Retrieved {} wards", wards.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all wards", e);
        }

        return wards;
    }

    /**
     * Updates an existing ward in the database.
     *
     * @param originalDepartmentCode The original department code of the ward
     * @param originalWardNumber The original ward number
     * @param ward The Ward object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(String originalDepartmentCode, Integer originalWardNumber, Ward ward) {
        String sql = "UPDATE Ward SET department_code = ?, ward_number = ?, bed_count = ?, supervisor_id = ? "
                + "WHERE department_code = ? AND ward_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ward.getDepartmentCode());
            stmt.setInt(2, ward.getWardNumber());
            stmt.setInt(3, ward.getBedCount());
            stmt.setInt(4, ward.getSupervisorId());
            stmt.setString(5, originalDepartmentCode);
            stmt.setInt(6, originalWardNumber);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated ward with original department code: {} and ward number: {}",
                        originalDepartmentCode, originalWardNumber);
                return true;
            } else {
                logger.warn("No ward found with department code: {} and ward number: {}",
                        originalDepartmentCode, originalWardNumber);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating ward", e);
            return false;
        }
    }

    /**
     * Deletes a ward from the database.
     *
     * @param departmentCode The department code of the ward to delete
     * @param wardNumber The ward number of the ward to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String departmentCode, int wardNumber) {
        String sql = "DELETE FROM Ward WHERE department_code = ? AND ward_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);
            stmt.setInt(2, wardNumber);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted ward with department code: {} and ward number: {}",
                        departmentCode, wardNumber);
                return true;
            } else {
                logger.warn("No ward found with department code: {} and ward number: {}",
                        departmentCode, wardNumber);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting ward", e);
            return false;
        }
    }

    /**
     * Finds wards by department.
     *
     * @param departmentCode The department code to search for
     * @return A list of wards in the specified department
     */
    public List<Ward> findByDepartment(String departmentCode) {
        String sql = "SELECT * FROM Ward WHERE department_code = ? ORDER BY ward_number";
        List<Ward> wards = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ward ward = mapResultSetToWard(rs);
                    wards.add(ward);
                }
            }

            logger.info("Found {} wards in department: {}", wards.size(), departmentCode);
        } catch (SQLException e) {
            logger.error("Error finding wards by department", e);
        }

        return wards;
    }

    /**
     * Finds wards by supervisor.
     *
     * @param supervisorId The ID of the supervisor to search for
     * @return A list of wards supervised by the specified nurse
     */
    public List<Ward> findBySupervisor(int supervisorId) {
        String sql = "SELECT * FROM Ward WHERE supervisor_id = ? ORDER BY department_code, ward_number";
        List<Ward> wards = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supervisorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ward ward = mapResultSetToWard(rs);
                    wards.add(ward);
                }
            }

            logger.info("Found {} wards supervised by nurse with ID: {}", wards.size(), supervisorId);
        } catch (SQLException e) {
            logger.error("Error finding wards by supervisor", e);
        }

        return wards;
    }

    /**
     * Helper method to map a ResultSet row to a Ward object.
     *
     * @param rs The ResultSet containing ward data
     * @return A new Ward object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Ward mapResultSetToWard(ResultSet rs) throws SQLException {
        Ward ward = new Ward();
        ward.setDepartmentCode(rs.getString("department_code"));
        ward.setWardNumber(rs.getInt("ward_number"));
        ward.setBedCount(rs.getInt("bed_count"));
        ward.setSupervisorId(rs.getInt("supervisor_id"));

        // Convert SQL timestamps to LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            ward.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            ward.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return ward;
    }
}