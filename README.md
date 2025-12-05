# ToDo Master - Secure Task Management System

**A production-ready RESTful API built with Spring Boot, PostgreSQL, Redis, and OAuth2 Security.**

[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://todo--master.vercel.app/)

> **Note to Visitors:** This project is hosted on the generic free tier of Render. If the backend is "cold," the initial request may take up to **2-5 minutes** to wake up the server. Please be patient; subsequent requests will be instant.

## 📖 Overview
ToDo Master is an architectural demonstration of a secure, stateless authentication system using Java and Spring Boot.

While the frontend utilizes React (AI-assisted), the core focus of this project is the **Backend Architecture**, specifically handling complex security flows like OAuth2, JWT rotation, and high-performance caching.

## 🚀 Key Features

### 🔐 Advanced Security & Auth
* **OAuth2 Integration:** Seamless login via Google and GitHub.
* **Dual-Token Architecture:**
    * **Access Token:** Short-lived (15 minutes) for high security.
    * **Refresh Token:** Long-lived (7 days) for user convenience.
    * **Token Rotation:** Issuing a new Refresh Token upon use to prevent replay attacks.
* **Secure Logout (Redis):** Implementation of **Token Blacklisting**. When a user logs out, the access token is invalidated immediately in **Redis** to prevent misuse, and the session is cleared.
* **Fall-back Logic:** Intelligent user creation handles cases where Email Providers do not return a name.

### 🛠 Role-Based Access Control (RBAC)
* **User Role:** Create, Read, Update, Delete (CRUD) personal Tasks and Categories.
* **Admin Role:** User Management, Global Category creation, and privileged data access.

## 🛠 Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.5+
* **Security:** Spring Security 6, OAuth2 Client, JWT
* **Database:** PostgreSQL (Primary), **Redis** (Caching & Blacklisting)
* **ORM:** Spring Data JPA / Hibernate
* **Frontend:** React.js (Deployed on Vercel)
* **Cloud/DevOps:** Render (Backend), Docker

## ⚙️ Architecture Flow

1.  **Authentication:** User logs in via OAuth2 -> Backend generates JWT Pair.
2.  **Session:** Browser stores Refresh Token (HTTPOnly Cookie) and Access Token.
3.  **Logout:** * Backend deletes Refresh Token from PostgreSQL.
    * Backend adds Access Token to **Redis Blacklist** with a TTL (Time-To-Live) matching the token expiry.

## 🔌 API Endpoints (Snapshot)

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | OAuth2 Handshake | Public |
| `POST` | `/auth/refresh` | Get new Access Token | Public |
| `GET` | `/api/tasks` | Get all user tasks | User |
| `DELETE`| `/admin/users/{id}`| Ban/Delete a user | **Admin** |

## 🏃‍♂️ How to Run Locally

1.  **Clone the repo**
    ```bash
    git clone https://github.com/Enigmazer/todo_app.git
    ```
2.  **Environment Variables**
    This project relies on specific environment variables for database connections, security, and notifications. You must configure these in your IDE (Edit Configurations) or a `.env` file before running the application.

    ```properties
    # Application Config
    FRONTEND_URL=http://localhost:5173  # URL of your React Frontend

    # Database (PostgreSQL)
    DB_URL=jdbc:postgresql://localhost:5432/your_db_name
    DB_USERNAME=your_db_username
    DB_PASSWORD=your_db_password

    # Security (OAuth2)
    GOOGLE_CLIENT_ID=your_google_client_id
    GOOGLE_CLIENT_SECRET=your_google_client_secret
    GITHUB_CLIENT_ID=your_github_client_id
    GITHUB_CLIENT_SECRET=your_github_client_secret

    # Redis (Caching & Token Blacklist)
    REDIS_URL=redis://localhost:6379

    # SMTP Email (For Notifications)
    EMAIL=your_email@gmail.com
    EMAIL_PASSWORD=your_app_specific_password
    ```
3.  **Build and Run**
    ```bash
    ./mvnw spring-boot:run
    ```

---
### 📬 Contact
**Arun Dangi** [LinkedIn Profile](https://www.linkedin.com/in/arundangi)  
Email: arundangi660@gmail.com