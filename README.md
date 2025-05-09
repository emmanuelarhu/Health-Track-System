# HealthTrack Hospital Management System

![Hospital Management System](https://img.shields.io/badge/Status-Development-green)
![Version](https://img.shields.io/badge/Version-1.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

A comprehensive JavaFX application for managing hospital operations, including patient records, staff management, hospitalization tracking, and departmental organization.

## 📋 Table of Contents

- [Features](#features)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## 🌟 Features

### Patient Management
- Register and maintain comprehensive patient records
- Track patient admissions and discharges
- Search patients by various criteria

### Staff Management
- Manage doctors with their specialties
- Track nursing staff with rotations and department assignments
- Handle staff assignments to wards and departments

### Department and Ward Management
- Organize hospital by departments and wards
- Track bed allocation and availability
- Monitor ward supervision

### Hospitalization Tracking
- Record patient hospitalizations with diagnosis
- Assign patients to specific wards, departments, and beds
- Manage admission and discharge dates

### Reporting
- Generate patient history reports
- Create department staffing reports
- Analyze bed occupancy and patient statistics
- Export reports to CSV for further analysis

## 🏗️ System Architecture

The application follows the Model-View-Controller (MVC) architectural pattern:

- **Model**: Java classes representing hospital entities such as Patient, Doctor, Department, etc.
- **View**: JavaFX FXML files defining the user interface
- **Controller**: Java classes managing user interaction and business logic

The system uses a Data Access Object (DAO) pattern to separate database interactions from business logic.

## 🗄️ Database Schema

The application uses a relational database with the following entity relationship:

![ER Diagram](https://github.com/user-attachments/assets/352ecace-e906-48b9-98e8-bcbb572e9728)


### Key Entities:
- **Employee**: Base entity for all staff members
- **Doctor/Nurse**: Specialized employee types with specific attributes
- **Patient**: Stores patient personal information
- **Department**: Hospital departments with directors
- **Ward**: Subdivisions within departments with bed capacity
- **Hospitalization**: Records of patient stays linking patients, wards, and doctors

## 📸 Screenshots

![Dashboard](https://github.com/user-attachments/assets/0a372629-9f84-44cf-8dcd-33a637b8c9a2)


## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- JavaFX 11 or higher
- MySQL 8.0 or higher
- Maven (optional, for building)

### Installation

1. Clone the repository
   ```bash
   git clone [https://github.com/emmanuelarhu/Health-Track-System/git]
   cd Health-Track-System
   ```

2. Set up the database
   ```bash
   mysql -u yourusername -p < sql/create_tables.sql
   mysql -u yourusername -p < sql/sample_data.sql
   ```

3. Configure database connection
   Update the database connection properties in `src/main/java/hospital/dao/DatabaseConnection.java` with your MySQL credentials.

4. Build and run the application
   ```bash
   mvn clean javafx:run
   ```
   Or run the `Main` class from your IDE.

## 💻 Usage

1. Launch the application
2. Use the dashboard or menu to navigate to different modules:
   - Patient Management
   - Doctor Management
   - Nurse Management
   - Department Management
   - Ward Management
   - Hospitalization Management
   - Reports

## 📁 Project Structure

```
src/main/java/hospital/
├── controller/       # Controllers for UI views
├── dao/              # Data Access Objects
├── model/            # Domain models
├── util/             # Utility classes
└── Main.java         # Application entry point

src/main/resources/
├── css/              # CSS stylesheets
├── view/             # FXML view files
└── log4j2.xml        # Logging configuration

sql/
├── create_tables.sql # Database schema creation script
└── sample_data.sql   # Sample data for testing
```

## 🛠️ Technologies Used

- **Java**: Core programming language
- **JavaFX**: UI framework
- **FXML**: XML-based UI markup language
- **MySQL**: Database management system
- **Log4j2**: Logging framework
- **CSS**: Styling the user interface

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Contact

Emmanuel Arhu - [emmanuelarhu706@gmail.com](mailto:emmanuelarhu706@gmai.com)

Project Link: [Health-Track-System](https://github.com/emmanuelarhu/Health-Track-System)

---

Made with ❤️ by Emmanuel Arhu
