# Keystone - Corrected Project

## Stack
- Spring Boot 3.5.5
- Java 21
- Thymeleaf
- Spring Security
- Spring Data JPA
- MySQL

## Import into Spring Tool Suite (STS)
1. Extract this ZIP.
2. In STS choose **File -> Import -> Maven -> Existing Maven Projects**.
3. Select the extracted project folder.
4. Finish the import.
5. Right-click the project -> **Maven -> Update Project**.
6. Run the class containing `@SpringBootApplication` as **Spring Boot App**.

## MySQL
Create the database configured in `application.properties` or `application.yml`, then update the
username/password to your local MySQL credentials.

## Important
If STS still shows old Maven errors after importing:
- Right-click project -> Maven -> Update Project
- Project -> Clean
- Restart STS if necessary
