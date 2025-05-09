package main.java.hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an employee in the HealthTrack System.
 * This is the base class for Doctor and Nurse.
 */
public class Employee {
    private Integer employeeId;
    private String firstName;
    private String surname;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Employee() {
    }

    /**
     * Constructor with required fields
     *
     * @param firstName The employee's first name
     * @param surname   The employee's surname
     * @param address   The employee's address
     * @param phone     The employee's phone number
     */
    public Employee(String firstName, String surname, String address, String phone) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
    }

    /**
     * Full constructor
     *
     * @param employeeId The employee's ID
     * @param firstName  The employee's first name
     * @param surname    The employee's surname
     * @param address    The employee's address
     * @param phone      The employee's phone number
     * @param createdAt  The timestamp when the record was created
     * @param updatedAt  The timestamp when the record was last updated
     */
    public Employee(Integer employeeId, String firstName, String surname, String address, String phone,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the full name of the employee.
     *
     * @return A string containing the employee's first name and surname
     */
    public String getFullName() {
        return firstName + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}