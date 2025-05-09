package main.java.hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a ward in the HealthTrack System.
 * A ward belongs to a department and is supervised by a nurse.
 */
public class Ward {
    private String departmentCode;
    private Integer wardNumber;
    private Integer bedCount;
    private Integer supervisorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Ward() {
    }

    /**
     * Constructor with required fields
     *
     * @param departmentCode The code of the department the ward belongs to
     * @param wardNumber     The ward number (local to the department)
     * @param bedCount       The number of beds in the ward
     * @param supervisorId   The ID of the nurse who supervises the ward
     */
    public Ward(String departmentCode, Integer wardNumber, Integer bedCount, Integer supervisorId) {
        this.departmentCode = departmentCode;
        this.wardNumber = wardNumber;
        this.bedCount = bedCount;
        this.supervisorId = supervisorId;
    }

    /**
     * Full constructor
     *
     * @param departmentCode The code of the department the ward belongs to
     * @param wardNumber     The ward number (local to the department)
     * @param bedCount       The number of beds in the ward
     * @param supervisorId   The ID of the nurse who supervises the ward
     * @param createdAt      The timestamp when the record was created
     * @param updatedAt      The timestamp when the record was last updated
     */
    public Ward(String departmentCode, Integer wardNumber, Integer bedCount, Integer supervisorId,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.departmentCode = departmentCode;
        this.wardNumber = wardNumber;
        this.bedCount = bedCount;
        this.supervisorId = supervisorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the code of the department the ward belongs to.
     *
     * @return The department code
     */
    public String getDepartmentCode() {
        return departmentCode;
    }

    /**
     * Sets the code of the department the ward belongs to.
     *
     * @param departmentCode The department code
     */
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    /**
     * Gets the ward number (local to the department).
     *
     * @return The ward number
     */
    public Integer getWardNumber() {
        return wardNumber;
    }

    /**
     * Sets the ward number (local to the department).
     *
     * @param wardNumber The ward number
     */
    public void setWardNumber(Integer wardNumber) {
        this.wardNumber = wardNumber;
    }

    /**
     * Gets the number of beds in the ward.
     *
     * @return The bed count
     */
    public Integer getBedCount() {
        return bedCount;
    }

    /**
     * Sets the number of beds in the ward.
     *
     * @param bedCount The bed count
     */
    public void setBedCount(Integer bedCount) {
        this.bedCount = bedCount;
    }

    /**
     * Gets the ID of the nurse who supervises the ward.
     *
     * @return The supervisor's employee ID
     */
    public Integer getSupervisorId() {
        return supervisorId;
    }

    /**
     * Sets the ID of the nurse who supervises the ward.
     *
     * @param supervisorId The supervisor's employee ID
     */
    public void setSupervisorId(Integer supervisorId) {
        this.supervisorId = supervisorId;
    }

    /**
     * Gets the timestamp when the record was created.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the record was created.
     *
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the record was last updated.
     *
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the record was last updated.
     *
     * @param updatedAt The update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns a unique identifier for the ward (combination of department code and ward number).
     *
     * @return A string representation of the ward's composite key
     */
    public String getWardId() {
        return departmentCode + "-" + wardNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ward ward = (Ward) o;
        return Objects.equals(departmentCode, ward.departmentCode) &&
                Objects.equals(wardNumber, ward.wardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentCode, wardNumber);
    }

    @Override
    public String toString() {
        return "Ward{" +
                "departmentCode='" + departmentCode + '\'' +
                ", wardNumber=" + wardNumber +
                ", bedCount=" + bedCount +
                ", supervisorId=" + supervisorId +
                '}';
    }
}