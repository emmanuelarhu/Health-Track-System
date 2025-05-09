package main.java.hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a doctor in the HealthTrack System.
 * Extends the Employee class.
 */
public class Doctor extends Employee {
    private String speciality;

    /**
     * Default constructor
     */
    public Doctor() {
        super();
    }

    /**
     * Constructor with required fields
     *
     * @param firstName  The doctor's first name
     * @param surname    The doctor's surname
     * @param address    The doctor's address
     * @param phone      The doctor's phone number
     * @param speciality The doctor's speciality
     */
    public Doctor(String firstName, String surname, String address, String phone, String speciality) {
        super(firstName, surname, address, phone);
        this.speciality = speciality;
    }

    /**
     * Full constructor
     *
     * @param employeeId The doctor's employee ID
     * @param firstName  The doctor's first name
     * @param surname    The doctor's surname
     * @param address    The doctor's address
     * @param phone      The doctor's phone number
     * @param speciality The doctor's speciality
     * @param createdAt  The timestamp when the record was created
     * @param updatedAt  The timestamp when the record was last updated
     */
    public Doctor(Integer employeeId, String firstName, String surname, String address, String phone,
                  String speciality, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(employeeId, firstName, surname, address, phone, createdAt, updatedAt);
        this.speciality = speciality;
    }

    /**
     * Gets the doctor's speciality.
     *
     * @return The doctor's speciality
     */
    public String getSpeciality() {
        return speciality;
    }

    /**
     * Sets the doctor's speciality.
     *
     * @param speciality The doctor's speciality
     */
    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(speciality, doctor.speciality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), speciality);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "employeeId=" + getEmployeeId() +
                ", firstName='" + getFirstName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", speciality='" + speciality + '\'' +
                '}';
    }
}