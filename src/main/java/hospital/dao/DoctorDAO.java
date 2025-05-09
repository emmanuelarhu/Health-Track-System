package main.java.hospital.dao;

import main.java.hospital.model.Doctor;
import main.java.hospital.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Doctor entity.
 * Provides CRUD operations for interacting with the Doctor table in the database.
 */
public class DoctorDAO {
    private static final Logger logger = LogManager.getLogger(DoctorDAO.class);
    private final DatabaseConnection dbConnection;
    private final EmployeeDAO employeeDAO;

    /**
     * Constructor that initializes the database connection.
     */
    public DoctorDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Inserts a new doctor into the database.
     * Assumes the employee data has already been inserted.
     *
     * @param doctor The Doctor object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Doctor doctor) {
        String sql = "INSERT INTO Doctor (employee_id, speciality) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctor.getEmployeeId());
            stmt.setString(2, doctor.getSpeciality());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Created doctor with ID: {}", doctor.getEmployeeId());
                return true;
            } else {
                logger.warn("Creating doctor failed, no rows affected");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error inserting doctor", e);
            return false;
        }
    }

    /**
     * Retrieves a doctor by their employee ID.
     *
     * @param employeeId The ID of the doctor to retrieve
     * @return An Optional containing the Doctor if found, or empty if not found
     */
    public Optional<Doctor> findById(int employeeId) {
        String sql = "SELECT d.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Doctor d " +
                "JOIN Employee e ON d.employee_id = e.employee_id " +
                "WHERE d.employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = mapResultSetToDoctor(rs);
                    logger.info("Found doctor with ID: {}", employeeId);
                    return Optional.of(doctor);
                } else {
                    logger.info("No doctor found with ID: {}", employeeId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctor by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves all doctors from the database.
     *
     * @return A list of all doctors
     */
    public List<Doctor> findAll() {
        String sql = "SELECT d.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Doctor d " +
                "JOIN Employee e ON d.employee_id = e.employee_id";

        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = mapResultSetToDoctor(rs);
                doctors.add(doctor);
            }

            logger.info("Retrieved {} doctors", doctors.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all doctors", e);
        }

        return doctors;
    }

    /**
     * Updates an existing doctor in the database.
     *
     * @param doctor The Doctor object with updated values
     * @return true if successful, false otherwise
     */
    public boolean update(Doctor doctor) {
        String sql = "UPDATE Doctor SET speciality = ? WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getSpeciality());
            stmt.setInt(2, doctor.getEmployeeId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Updated doctor with ID: {}", doctor.getEmployeeId());
                return true;
            } else {
                logger.warn("No doctor found with ID: {}", doctor.getEmployeeId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating doctor", e);
            return false;
        }
    }

    /**
     * Deletes a doctor from the database.
     *
     * @param employeeId The ID of the doctor to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int employeeId) {
        String sql = "DELETE FROM Doctor WHERE employee_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Deleted doctor with ID: {}", employeeId);
                return true;
            } else {
                logger.warn("No doctor found with ID: {}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting doctor", e);
            return false;
        }
    }

    /**
     * Finds doctors by speciality.
     *
     * @param speciality The speciality to search for
     * @return A list of doctors with the specified speciality
     */
    public List<Doctor> findBySpeciality(String speciality) {
        String sql = "SELECT d.*, e.first_name, e.surname, e.address, e.phone, " +
                "e.created_at AS employee_created_at, e.updated_at AS employee_updated_at " +
                "FROM Doctor d " +
                "JOIN Employee e ON d.employee_id = e.employee_id " +
                "WHERE d.speciality = ?";

        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, speciality);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Doctor doctor = mapResultSetToDoctor(rs);
                    doctors.add(doctor);
                }
            }

            logger.info("Found {} doctors with speciality: {}", doctors.size(), speciality);
        } catch (SQLException e) {
            logger.error("Error finding doctors by speciality", e);
        }

        return doctors;
    }

    /**
     * Helper method to map a ResultSet row to a Doctor object.
     *
     * @param rs The ResultSet containing doctor data
     * @return A new Doctor object with data from the ResultSet
     * @throws SQLException If a database access error occurs
     */
    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
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

        // Now create a Doctor and set its fields
        Doctor doctor = new Doctor();
        doctor.setEmployeeId(rs.getInt("employee_id"));
        doctor.setFirstName(employee.getFirstName());
        doctor.setSurname(employee.getSurname());
        doctor.setAddress(employee.getAddress());
        doctor.setPhone(employee.getPhone());
        doctor.setCreatedAt(employee.getCreatedAt());
        doctor.setUpdatedAt(employee.getUpdatedAt());
        doctor.setSpeciality(rs.getString("speciality"));

        // Convert SQL timestamps to LocalDateTime for doctor
        Timestamp doctorCreatedAt = rs.getTimestamp("created_at");
        if (doctorCreatedAt != null) {
            // This is already set from employee, but we set it again for clarity
            doctor.setCreatedAt(doctorCreatedAt.toLocalDateTime());
        }

        Timestamp doctorUpdatedAt = rs.getTimestamp("updated_at");
        if (doctorUpdatedAt != null) {
            // This is already set from employee, but we set it again for clarity
            doctor.setUpdatedAt(doctorUpdatedAt.toLocalDateTime());
        }

        return doctor;
    }
}