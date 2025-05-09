package main.java.hospital.dao;

import main.java.hospital.model.Nurse;
import main.java.hospital.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Nurse entity.
 * Provides CRUD operations for interacting with the Nurse table in the database.
 */
public class NurseDAO {
    private static final Logger logger = LogManager.getLogger(NurseDAO.class);
    private final DatabaseConnection dbConnection;
    private final EmployeeDAO employeeDAO;

    /**
     * Constructor that initializes the database connection.
     */
    public NurseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Inserts a new nurse into the database.
     * Assumes the employee data has already been inserted.
     *
     * @param nurse The Nurse object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Nurse nurse) {
        String sql = "INSERT INTO Nurse (employee_id, rotation, salary, department_code) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nurse.getEmployeeId());
            stmt.setString(2, nurse.getRotation());
            stmt.setBigDecimal(3, nurse.getSalary());
            stmt.setString(4, nurse.getDepartmentCode());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Created nurse with ID: {}", nurse.getEmployeeId());
                return true;
            } else {
                logger.warn("Creating nurse failed, no rows affected");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error inserting nurse", e);
            return false;
        }
    }

    /**
     * Retrieves a nurse by their employee ID.
     *
     * @param employeeId The ID of the nurse to retrieve
     * @return An Optional containing the Nurse if found, or empty if not found
     */
    public Optional<Nurse> findById(int employeeId) {
        String sql = "SELECT n.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Nurse n " +
                "JOIN Employee e ON n.employee_id = e.employee_id " +
                "WHERE n.employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Nurse nurse = mapResultSetToNurse(rs);
                    logger.info("Found nurse with ID: {}", employeeId);
                    return Optional.of(nurse);
                } else {
                    logger.info("No nurse found with ID: {}", employeeId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding nurse by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all nurses from the database.
     *
     * @return A list of all nurses
     */
    public List<Nurse> findAll() {
        String sql = "SELECT n.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Nurse n " +
                "JOIN Employee e ON n.employee_id = e.employee_id";

        List<Nurse> nurses = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Nurse nurse = mapResultSetToNurse(rs);
                nurses.add(nurse);
            }

            logger.info("Retrieved {} nurses", nurses.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all nurses", e);
        }

        return nurses;
    }

    /**
     * Updates an existing nurse in the database.
     *
     * @param nurse The Nurse object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Nurse nurse) {
        String sql = "UPDATE Nurse SET rotation = ?, salary = ?, department_code = ? WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nurse.getRotation());
            stmt.setBigDecimal(2, nurse.getSalary());
            stmt.setString(3, nurse.getDepartmentCode());
            stmt.setInt(4, nurse.getEmployeeId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated nurse with ID: {}", nurse.getEmployeeId());
                return true;
            } else {
                logger.warn("No nurse found with ID: {}", nurse.getEmployeeId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating nurse", e);
            return false;
        }
    }

    /**
     * Deletes a nurse from the database.
     *
     * @param employeeId The ID of the nurse to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int employeeId) {
        String sql = "DELETE FROM Nurse WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted nurse with ID: {}", employeeId);
                return true;
            } else {
                logger.warn("No nurse found with ID: {}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting nurse", e);
            return false;
        }
    }

    /**
     * Finds nurses by department.
     *
     * @param departmentCode The department code to search for
     * @return A list of nurses in the specified department
     */
    public List<Nurse> findByDepartment(String departmentCode) {
        String sql = "SELECT n.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Nurse n " +
                "JOIN Employee e ON n.employee_id = e.employee_id " +
                "WHERE n.department_code = ?";

        List<Nurse> nurses = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Nurse nurse = mapResultSetToNurse(rs);
                    nurses.add(nurse);
                }
            }

            logger.info("Found {} nurses in department: {}", nurses.size(), departmentCode);
        } catch (SQLException e) {
            logger.error("Error finding nurses by department", e);
        }

        return nurses;
    }

    /**
     * Finds nurses by rotation.
     *
     * @param rotation The rotation to search for
     * @return A list of nurses with the specified rotation
     */
    public List<Nurse> findByRotation(String rotation) {
        String sql = "SELECT n.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Nurse n " +
                "JOIN Employee e ON n.employee_id = e.employee_id " +
                "WHERE n.rotation = ?";

        List<Nurse> nurses = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rotation);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Nurse nurse = mapResultSetToNurse(rs);
                    nurses.add(nurse);
                }
            }

            logger.info("Found {} nurses with rotation: {}", nurses.size(), rotation);
        } catch (SQLException e) {
            logger.error("Error finding nurses by rotation", e);
        }

        return nurses;
    }

    /**
     * Helper method to map a ResultSet row to a Nurse object.
     *
     * @param rs The ResultSet containing nurse data
     * @return A new Nurse object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Nurse mapResultSetToNurse(ResultSet rs) throws SQLException {
        // First create an Employee with all employee data
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setSurname(rs.getString("surname"));
        employee.setAddress(rs.getString("address"));
        employee.setPhone(rs.getString("phone"));

        // Convert SQL timestamps to LocalDateTime for employee
        Timestamp employeeCreatedAt = rs.getTimestamp("employee_created_at");
        if (employeeCreatedAt != null) {
            employee.setCreatedAt(employeeCreatedAt.toLocalDateTime());
        }

        Timestamp employeeUpdatedAt = rs.getTimestamp("employee_updated_at");
        if (employeeUpdatedAt != null) {
            employee.setUpdatedAt(employeeUpdatedAt.toLocalDateTime());
        }

        // Now create a Nurse and set its fields
        Nurse nurse = new Nurse();
        nurse.setEmployeeId(rs.getInt("employee_id"));
        nurse.setFirstName(employee.getFirstName());
        nurse.setSurname(employee.getSurname());
        nurse.setAddress(employee.getAddress());
        nurse.setPhone(employee.getPhone());
        nurse.setCreatedAt(employee.getCreatedAt());
        nurse.setUpdatedAt(employee.getUpdatedAt());
        nurse.setRotation(rs.getString("rotation"));
        nurse.setSalary(rs.getBigDecimal("salary"));
        nurse.setDepartmentCode(rs.getString("department_code"));

        // Convert SQL timestamps to LocalDateTime for nurse
        Timestamp nurseCreatedAt = rs.getTimestamp("created_at");
        if (nurseCreatedAt != null) {
            // This is already set from employee, but we set it again for clarity
            nurse.setCreatedAt(nurseCreatedAt.toLocalDateTime());
        }

        Timestamp nurseUpdatedAt = rs.getTimestamp("updated_at");
        if (nurseUpdatedAt != null) {
            // This is already set from employee, but we set it again for clarity
            nurse.setUpdatedAt(nurseUpdatedAt.toLocalDateTime());
        }

        return nurse;
    }
}