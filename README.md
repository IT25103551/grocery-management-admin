# Online Grocery Order Management System — Admin Module

A robust Spring Boot web application for managing administrators, staff accounts, roles, and privileges in an Online Grocery Order Management System. Built using core Object-Oriented Programming (OOP) principles and file-based persistence.

---

## 📌 Project Overview
The Admin Management module serves as the central administrative hub for the grocery order management platform. It allows authorized personnel (Super Admins, Managers, and Staff) to authenticate, view administrative dashboards, manage system users, and enforce role-based access control (RBAC).

---

## 🏛️ Core OOP Principles Applied

This project demonstrates core Object-Oriented Programming (OOP) concepts:

### 1. Abstraction
- **Abstract Class `User`**: Defines the shared contract and fundamental attributes (`id`, `username`, `email`, `password`, `phone`, `address`, `role`) for all system actors without allowing direct instantiation.
- **Abstract Methods**:
  - `toLine()`: Abstract serialization method enforcing each entity to define its file representation.
  - `getPermissionsSummary()`: Abstract method enforcing descriptive capability summaries for different roles.

### 2. Encapsulation
- All instance variables in `User` and `Admin` are declared `private` to protect the internal object state from unauthorized modification.
- Access to fields is strictly controlled via public getter and setter methods.
- Sensitive business rules (e.g. `checkPassword`, `canManageAdmins`, `canEdit`) are encapsulated within domain objects rather than exposed across layers.

### 3. Inheritance
- **`Admin extends User`**: Establishes an `IS-A` relationship where an Admin inherits common identity and contact fields while introducing administrative attributes (`adminLevel`, `lastLogin`, `active`).
- Reuses parent constructor via `super(...)` calls, eliminating code duplication and ensuring proper state initialization.

### 4. Polymorphism
- **Method Overriding**: `Admin` overrides abstract methods `toLine()` and `getPermissionsSummary()` to provide role-specific implementations for administrative formatting and permission strings.
- Allows polymorphic processing of users while maintaining subclass-specific behaviors.

---

## 💾 File Handling & Persistence
- Data is stored in a clean comma-separated format within `src/main/resources/data/admins.txt`.
- Safe file reading and writing using `BufferedReader` and `BufferedWriter` with Java's **try-with-resources** pattern, ensuring proper resource management and stream closure.
- Supports complete **CRUD** operations (Create, Read, Update, Delete) directly over file storage.

---

## 👥 Role-Based Access Control (RBAC)

| Role | Permissions |
| :--- | :--- |
| **SUPER** | Full administrative rights: Register new admins, edit/delete any admin, toggle active status, and modify privilege levels. |
| **MANAGER** | Access dashboard analytics, view all registered administrators, and update personal profile details. |
| **STAFF** | Access dashboard metrics, view the admin directory, and update personal account details. |

---

## 🛠️ Tech Stack & Technologies
- **Language**: Java 17
- **Framework**: Spring Boot 4.0.3 (Web MVC, Thymeleaf)
- **View Engine**: Thymeleaf & HTML5 / CSS3
- **Storage**: Flat-File Persistence (`admins.txt`)
- **Build Tool**: Apache Maven

---

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8+ (or Maven wrapper)
- Modern web browser (Chrome, Edge, Firefox)

### Running the Application
1. Clone the repository:
   ```bash
   git clone https://github.com/IT25103551/grocery-management-admin.git
   cd grocery-management-admin
   ```

2. Build and run the project:
   ```bash
   mvn clean spring-boot:run
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:8080/
   ```

### Default Credentials (for testing)

| Username | Password | Role | Level | Status |
| :--- | :--- | :--- | :--- | :--- |
| `superadmin` | `super123` | ADMIN | SUPER | Active |
| `manager1` | `manage123` | ADMIN | MANAGER | Active |
| `staff1` | `staff123` | ADMIN | STAFF | Active |
| `staff2` | `staff456` | ADMIN | STAFF | Inactive |

---

## 📁 Project Directory Structure
```
grocery-management-admin/
├── src/
│   ├── main/
│   │   ├── java/com/grocery/
│   │   │   ├── controller/
│   │   │   │   ├── AdminServlet.java
│   │   │   │   └── HomeServlet.java
│   │   │   ├── model/
│   │   │   │   ├── Admin.java
│   │   │   │   └── User.java
│   │   │   ├── service/
│   │   │   │   └── AdminService.java
│   │   │   ├── GroceryManagementApplication.java
│   │   │   └── ServletInitializer.java
│   │   └── resources/
│   │       ├── data/
│   │       │   └── admins.txt
│   │       ├── static/
│   │       │   └── style.css
│   │       ├── templates/
│   │       │   ├── admin/
│   │       │   │   ├── admin-edit.html
│   │       │   │   ├── admin-list.html
│   │       │   │   ├── admin-login.html
│   │       │   │   ├── admin-register.html
│   │       │   │   └── dashboard.html
│   │       └── layout.html
│   │       └── application.properties
│   └── test/java/com/grocery/
│       └── GroceryManagementApplicationTests.java
├── pom.xml
├── .gitignore
└── README.md
```

---

## 👤 Author
- **Student ID**: IT25103551
- **SLIIT** — Faculty of Computing
