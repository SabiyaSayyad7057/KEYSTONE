# Keystone – Added Registration & Role Features

## Added
- Public customer registration at `/register`
- Duplicate-email check; existing users are told to log in
- BCrypt password hashing
- User name stored in `app_user.name`
- Current user name and role shown on dashboard
- Manager-only User Management at `/users`
- Manager can create Technician, Dispatcher, and Manager accounts
- Manager can delete users
- `/users/**` protected with Spring Security
- New account always registers as CUSTOMER for safety

## Default manager
- Email: `admin@gmail.com`
- Password: `admin123`

Change this password for real use.

## Run
1. Import the project into Spring Tools Suite.
2. Make sure MySQL is running and the `keystone` database exists.
3. Check `src/main/resources/application.properties` for your MySQL username/password.
4. Maven Update Project, then run `KeystoneApplication.java`.
5. Open `http://localhost:8080/login`.
6. Click **Create an account** to register a customer.
7. Log in as the manager to open **Users** and create staff accounts.

## Important
The existing project keeps Thymeleaf + Spring Boot + MySQL. No React conversion was made. Existing work-order/customer/site pages are retained.

## Realistic Role Flow

- CUSTOMER: public registration, own dashboard, own work orders, request service for own site.
- TECHNICIAN: manager-created account, sees only assigned jobs, updates job status and notes.
- DISPATCHER: manager-created account, sees the operations queue, creates work orders and assigns technicians.
- MANAGER: full operational view, creates staff accounts and manages customers/sites.

### Demo accounts
- Manager: manager@keystone.com / manager123
- Dispatcher: dispatcher@keystone.com / dispatcher123
- Technician: technician@keystone.com / tech123
- Customer: customer@keystone.com / customer123

A newly registered customer is automatically linked to a Customer record and a primary service location. This prevents the previous issue where a customer could log in but had no customer-specific data.
