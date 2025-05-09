package main.java.hospital.dao;

import main.java.hospital.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Employee entity.
 * Provides CRUD operations for interacting with the Employee table in the database.
 */
public class EmployeeDAO {
    private static final Logger logger = LogManager.getLogger(EmployeeDAO.class);
    private final DatabaseConnection dbConnection;

    /**
     * Constructor that initializes the database connection.
     */
    public EmployeeDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserts a new employee into the database.
     *
     * @param employee The Employee object to insert
     * @return The generated employee ID if successful, or empty if failed
     */
    public Optional<Integer> insert(Employee employee) {
        String sql = "INSERT INTO Employee (first_name, surname, address, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getSurname());
            stmt.setString(3, employee.getAddress());
            stmt.setString(4, employee.getPhone());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("Creating employee failed, no rows affected");
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int employeeId = generatedKeys.getInt(1);
                    employee.setEmployeeId(employeeId);
                    logger.info("Created employee with ID: {}", employeeId);
                    return Optional.of(employeeId);
                } else {
                    logger.warn("Creating employee failed, no ID obtained");
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting employee", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param employeeId The ID of the employee to retrieve
     * @return An Optional containing the Employee if found, or empty if not found
     */
    public Optional<Employee> findById(int employeeId) {
        String sql = "SELECT * FROM Employee WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Employee employee = mapResultSetToEmployee(rs);
                    logger.info("Found employee with ID: {}", employeeId);
                    return Optional.of(employee);
                } else {
                    logger.info("No employee found with ID: {}", employeeId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding employee by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all employees from the database.
     *
     * @return A list of all employees
     */
    public List<Employee> findAll() {
        String sql = "SELECT * FROM Employee";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee employee = mapResultSetToEmployee(rs);
                employees.add(employee);
            }

            logger.info("Retrieved {} employees", employees.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all employees", e);
        }

        return employees;
    }

    /**
     * Updates an existing employee in the database.
     *
     * @param employee The Employee object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Employee employee) {
        String sql = "UPDATE Employee SET first_name = ?, surname = ?, address = ?, phone = ? WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getSurname());
            stmt.setString(3, employee.getAddress());
            stmt.setString(4, employee.getPhone());
            stmt.setInt(5, employee.getEmployeeId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated employee with ID: {}", employee.getEmployeeId());
                return true;
            } else {
                logger.warn("No employee found with ID: {}", employee.getEmployeeId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating employee", e);
            return false;
        }
    }

    /**
     * Deletes an employee from the database.
     *
     * @param employeeId The ID of the employee to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int employeeId) {
        String sql = "DELETE FROM Employee WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted employee with ID: {}", employeeId);
                return true;
            } else {
                logger.warn("No employee found with ID: {}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting employee", e);
            return false;
        }
    }

    /**
     * Searches for employees by name (first name or surname).
     *
     * @param searchTerm The search term to look for in first name or surname
     * @return A list of matching employees
     */
    public List<Employee> searchByName(String searchTerm) {
        String sql = "SELECT * FROM Employee WHERE first_name LIKE ? OR surname LIKE ?";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = mapResultSetToEmployee(rs);
                    employees.add(employee);
                }
            }

            logger.info("Found {} employees matching search term: {}", employees.size(), searchTerm);
        } catch (SQLException e) {
            logger.error("Error searching employees by name", e);
        }

        return employees;
    }

    /**
     * Helper method to map a ResultSet row to an Employee object.
     *
     * @param rs The ResultSet containing employee data
     * @return A new Employee object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setSurname(rs.getString("surname"));
        employee.setAddress(rs.getString("address"));
        employee.setPhone(rs.getString("phone"));

        // Convert SQL timestamps to LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            employee.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            employee.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return employee;
    }
}