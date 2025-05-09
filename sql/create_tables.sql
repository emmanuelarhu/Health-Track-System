-- HealthTrack System Database Schema
-- Create database
-- CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

-- Create Employee table (parent table for Doctor and Nurse)
CREATE TABLE Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Department table
CREATE TABLE Department (
    department_code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    building VARCHAR(50) NOT NULL,
    director_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (director_id) REFERENCES Employee(employee_id)
);

-- Create Doctor table (specializes Employee)
CREATE TABLE Doctor (
    employee_id INT PRIMARY KEY,
    speciality VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id) ON DELETE CASCADE
);

-- Create Nurse table (specializes Employee)
CREATE TABLE Nurse (
    employee_id INT PRIMARY KEY,
    rotation VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    department_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (department_code) REFERENCES Department(department_code)
);

-- Create Ward table
CREATE TABLE Ward (
    department_code VARCHAR(10),
    ward_number INT,
    bed_count INT NOT NULL,
    supervisor_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (department_code, ward_number),
    FOREIGN KEY (department_code) REFERENCES Department(department_code),
    FOREIGN KEY (supervisor_id) REFERENCES Nurse(employee_id)
);

-- Create Patient table
CREATE TABLE Patient (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Hospitalization table (for patient stays)
CREATE TABLE Hospitalization (
    hospitalization_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    department_code VARCHAR(10) NOT NULL,
    ward_number INT NOT NULL,
    bed_number INT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    doctor_id INT NOT NULL,
    admission_date DATE NOT NULL,
    discharge_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (department_code, ward_number) REFERENCES Ward(department_code, ward_number),
    FOREIGN KEY (doctor_id) REFERENCES Doctor(employee_id)
);