# DB Automation

## Overview

**DB Automation** is a Spring Boot application that automates the extraction, validation, and processing of property configuration files from various sources such as Bitbucket and JFrog. The application performs automated database interactions using SQL scripts and ensures the proper configuration of property files.

## Features

- Automated file retrieval from Bitbucket and JFrog.
- Handles database operations like SELECT, UPDATE, DELETE, and INSERT.
- Supports H2 and MariaDB databases for different environments.
- Property validation using custom regex patterns defined in validation files.
- Logging support with customizable log formats.
- Environment-based execution modes (e.g., UAT, Deployment).
- Uses Hibernate and JPA for ORM-based database interaction.

## Project Structure

- **application.properties**: Contains configuration settings for the database and environment.
- **validation.properties**: Defines validation rules for various input types using regex patterns.
- **pom.xml**: The Maven configuration file for managing dependencies, plugins, and building the project.
- **src/**: Contains the core Java code for the application including services, configuration, and exception handling.

## Key Technologies

- **Spring Boot**: Simplifies microservice and backend application development.
- **JPA**: Provides ORM functionality to map Java objects to database tables.
- **MariaDB & H2**: Support for both MariaDB and H2 as the database for different environments.
- **Logback**: For efficient logging and tracking.
- **Jasypt**: Provides encryption capabilities for secure property files.

## Requirements

- Java 8 or higher
- Maven 3.6.0+
- MariaDB or H2 Database
- Access to Bitbucket or JFrog for property file retrieval

## Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/db-automation.git
   cd db-automation
   ```
2. **Configure the application**:
	•	Update the application.properties file to include your specific database URL, username, and password.
	•	Modify validation.properties to include any custom validation patterns for your use case.

4. **Build the project**:
   ```bash
   mvn clean install
   ```
5. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
