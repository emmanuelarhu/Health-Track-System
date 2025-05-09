package main.java.hospital.dao;

import main.java.hospital.model.Department;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Department entity.
 * Provides CRUD operations for interacting with the Department table in the database.
 */
public class DepartmentDAO {
    private static final Logger logger = LogManager.getLogger(DepartmentDAO.class);
    private final DatabaseConnection dbConnection;

    /**
     * Constructor that initializes the database connection.
     */
    public DepartmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserts a new department into the database.
     *
     * @param department The Department object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Department department) {
        String sql = "INSERT INTO Department (department_code, name, building, director_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentCode());
            stmt.setString(2, department.getName());
            stmt.setString(3, department.getBuilding());

            if (department.getDirectorId() != null) {
                stmt.setInt(4, department.getDirectorId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Created department with code: {}", department.getDepartmentCode());
                return true;
            } else {
                logger.warn("Creating department failed, no rows affected");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error inserting department", e);
            return false;
        }
    }

    /**
     * Retrieves a department by its code.
     *
     * @param departmentCode The code of the department to retrieve
     * @return An Optional containing the Department if found, or empty if not found
     */
    public Optional<Department> findByCode(String departmentCode) {
        String sql = "SELECT * FROM Department WHERE department_code = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Department department = mapResultSetToDepartment(rs);
                    logger.info("Found department with code: {}", departmentCode);
                    return Optional.of(department);
                } else {
                    logger.info("No department found with code: {}", departmentCode);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding department by code", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all departments from the database.
     *
     * @return A list of all departments
     */
    public List<Department> findAll() {
        String sql = "SELECT * FROM Department";
        List<Department> departments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Department department = mapResultSetToDepartment(rs);
                departments.add(department);
            }

            logger.info("Retrieved {} departments", departments.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all departments", e);
        }

        return departments;
    }

    /**
     * Updates an existing department in the database.
     *
     * @param department The Department object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Department department) {
        String sql = "UPDATE Department SET name = ?, building = ?, director_id = ? WHERE department_code = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getName());
            stmt.setString(2, department.getBuilding());

            if (department.getDirectorId() != null) {
                stmt.setInt(3, department.getDirectorId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            stmt.setString(4, department.getDepartmentCode());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated department with code: {}", department.getDepartmentCode());
                return true;
            } else {
                logger.warn("No department found with code: {}", department.getDepartmentCode());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating department", e);
            return false;
        }
    }

    /**
     * Deletes a department from the database.
     *
     * @param departmentCode The code of the department to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String departmentCode) {
        String sql = "DELETE FROM Department WHERE department_code = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted department with code: {}", departmentCode);
                return true;
            } else {
                logger.warn("No department found with code: {}", departmentCode);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting department", e);
            return false;
        }
    }

    /**
     * Finds departments by building.
     *
     * @param building The building to search for
     * @return A list of departments in the specified building
     */
    public List<Department> findByBuilding(String building) {
        String sql = "SELECT * FROM Department WHERE building = ?";
        List<Department> departments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, building);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Department department = mapResultSetToDepartment(rs);
                    departments.add(department);
                }
            }

            logger.info("Found {} departments in building: {}", departments.size(), building);
        } catch (SQLException e) {
            logger.error("Error finding departments by building", e);
        }

        return departments;
    }

    /**
     * Finds departments by director.
     *
     * @param directorId The ID of the director to search for
     * @return A list of departments directed by the specified director
     */
    public List<Department> findByDirector(int directorId) {
        String sql = "SELECT * FROM Department WHERE director_id = ?";
        List<Department> departments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, directorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Department department = mapResultSetToDepartment(rs);
                    departments.add(department);
                }
            }

            logger.info("Found {} departments directed by employee with ID: {}", departments.size(), directorId);
        } catch (SQLException e) {
            logger.error("Error finding departments by director", e);
        }

        return departments;
    }

    /**
     * Helper method to map a ResultSet row to a Department object.
     *
     * @param rs The ResultSet containing department data
     * @return A new Department object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setDepartmentCode(rs.getString("department_code"));
        department.setName(rs.getString("name"));
        department.setBuilding(rs.getString("building"));

        int directorId = rs.getInt("director_id");
        if (!rs.wasNull()) {
            department.setDirectorId(directorId);
        }

        // Convert SQL timestamps to LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            department.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            department.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return department;
    }
}