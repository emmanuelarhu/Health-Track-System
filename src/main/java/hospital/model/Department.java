package main.java.hospital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a department in the HealthTrack System.
 */
public class Department {
    private String departmentCode;
    private String name;
    private String building;
    private Integer directorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Department() {
    }

    /**
     * Constructor with required fields
     *
     * @param departmentCode The department's code
     * @param name           The department's name
     * @param building       The building where the department is located
     * @param directorId     The ID of the doctor who directs the department
     */
    public Department(String departmentCode, String name, String building, Integer directorId) {
        this.departmentCode = departmentCode;
        this.name = name;
        this.building = building;
        this.directorId = directorId;
    }

    /**
     * Full constructor
     *
     * @param departmentCode The department's code
     * @param name           The department's name
     * @param building       The building where the department is located
     * @param directorId     The ID of the doctor who directs the department
     * @param createdAt      The timestamp when the record was created
     * @param updatedAt      The timestamp when the record was last updated
     */
    public Department(String departmentCode, String name, String building, Integer directorId,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.departmentCode = departmentCode;
        this.name = name;
        this.building = building;
        this.directorId = directorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the department's code.
     *
     * @return The department code
     */
    public String getDepartmentCode() {
        return departmentCode;
    }

    /**
     * Sets the department's code.
     *
     * @param departmentCode The department code
     */
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    /**
     * Gets the department's name.
     *
     * @return The department name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the department's name.
     *
     * @param name The department name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the building where the department is located.
     *
     * @return The building name
     */
    public String getBuilding() {
        return building;
    }

    /**
     * Sets the building where the department is located.
     *
     * @param building The building name
     */
    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * Gets the ID of the doctor who directs the department.
     *
     * @return The director's employee ID
     */
    public Integer getDirectorId() {
        return directorId;
    }

    /**
     * Sets the ID of the doctor who directs the department.
     *
     * @param directorId The director's employee ID
     */
    public void setDirectorId(Integer directorId) {
        this.directorId = directorId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(departmentCode, that.departmentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentCode);
    }

    @Override
    public String toString() {
        return "Department{" +
                "departmentCode='" + departmentCode + '\'' +
                ", name='" + name + '\'' +
                ", building='" + building + '\'' +
                ", directorId=" + directorId +
                '}';
    }
}