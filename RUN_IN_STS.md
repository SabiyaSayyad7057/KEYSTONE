# KEYSTONE - Clean STS Import

This version is prepared as a Maven project so STS can create its own Java build path.

## IMPORTANT
Do NOT use the old `KeystoneApplication` Run Configuration.

### Import
1. Extract the ZIP.
2. Open Spring Tool Suite.
3. File -> Import -> Maven -> Existing Maven Projects.
4. Select the folder named `KEYSTONE`.
5. Finish.
6. Right-click the project -> Maven -> Update Project -> OK.
7. Project -> Clean.
8. Open `src/main/java/com/example/keystone/KeystoneApplication.java`.
9. Right-click it -> Run As -> Spring Boot App.

## Java
This project targets Java 17. Make sure STS has a JDK 17 installed/configured.

## MySQL
Make sure MySQL is running and the `keystone` database exists. Check
`src/main/resources/application.properties` for your local MySQL username/password.

If STS shows a Java version error, configure the installed JDK under:
Window -> Preferences -> Java -> Installed JREs.


## Status and delete fixes
This build includes working POST actions for status updates, technician assignment, logout, customer/site/user deletion, with friendly redirect messages.
