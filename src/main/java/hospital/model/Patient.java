package main.java.hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a patient in the HealthTrack System.
 */
public class Patient {
    private Integer patientId;
    private String firstName;
    private String surname;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Patient() {
    }

    /**
     * Constructor with required fields
     *
     * @param firstName The patient's first name
     * @param surname   The patient's surname
     * @param address   The patient's address
     * @param phone     The patient's phone number
     */
    public Patient(String firstName, String surname, String address, String phone) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
    }

    /**
     * Full constructor
     *
     * @param patientId The patient's ID
     * @param firstName The patient's first name
     * @param surname   The patient's surname
     * @param address   The patient's address
     * @param phone     The patient's phone number
     * @param createdAt The timestamp when the record was created
     * @param updatedAt The timestamp when the record was last updated
     */
    public Patient(Integer patientId, String firstName, String surname, String address, String phone,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
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
     * Returns the full name of the patient.
     *
     * @return A string containing the patient's first name and surname
     */
    public String getFullName() {
        return firstName + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(patientId, patient.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}