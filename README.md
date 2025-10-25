# 📖 Book Exchange System

[![Java](https://img.shields.io/badge/Language-Java-blue?style=for-the-badge&logo=java)](https://www.java.com/)
[![JSP/Servlet](https://img.shields.io/badge/Technology-JSP%20%26%20Servlet-orange?style=for-the-badge&logo=apache)](https://www.oracle.com/java/technologies/java-ee-glance.html)
[![Database](https://img.shields.io/badge/Database-MySQL-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Frontend](https://img.shields.io/badge/Frontend-HTML%2CSS%2CJS-yellow?style=for-the-badge&logo=html5)](https://developer.mozilla.org/en-US/docs/Web/Guide/HTML/HTML5)

A comprehensive web-based platform built with Java (Servlets/JSP) that allows users to register, list their books, and exchange them with other users. This project demonstrates a full-stack application development life cycle, from backend logic and database management to a dynamic, user-facing frontend.

## 🚀 Overview

The Book Exchange System is a platform designed for book lovers who want to share their physical books with a community. Users can create an account, upload details of the books they own, and browse books available from other users. The system facilitates a simple request-and-approval process for book exchanges, creating a "library" built on sharing.

This project is an ideal showcase of skills in Java web technologies, database design, and building a complete, stateful web application.

## ✨ Key Features

* **User Authentication:** Secure user registration and login system with session management.
* **User Dashboard:** A personalized space where users can view their profile, manage their books, and track their exchange requests.
* **Book Management:**
    * **Add Books:** Users can easily add books to their collection with details like Title, Author, Genre, and Condition.
    * **View & Search:** A public catalog of all available books, searchable and filterable by genre, title, or author.
    * **Edit/Delete:** Users can manage their own book listings.
* **Book Exchange Module:**
    * **Request a Book:** Users can send an exchange request to a book owner.
    * **Manage Requests:** Owners can accept or decline incoming exchange requests.
    * **Track Status:** Both parties can track the status of an exchange (e.g., "Pending," "Accepted," "Completed").

## 💻 Technology Stack

* **Backend:** Java, Servlets, JSP (JavaServer Pages)
* **Frontend:** HTML, CSS, JavaScript
* **Database:** MySQL
* **Web Server:** Apache Tomcat
* **IDE:** Eclipse / IntelliJ IDEA

## 🔧 Project Setup & Installation

To get a local copy up and running, follow these simple steps.

### Prerequisites

You will need the following tools installed on your system:

* [Java Development Kit (JDK) 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) or higher
* [Apache Tomcat 9](https://tomcat.apache.org/download-90.cgi) or higher
* A database management system, such as [MySQL Server](https://dev.mysql.com/downloads/mysql/)
* An IDE like [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads/packages/release/2021-06/r/eclipse-ide-java-ee-developers) or [IntelliJ IDEA Ultimate](https://www.jetbrains.com/idea/download/)

### Installation

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/shivanshmishra54/Book-Exchange-System.git](https://github.com/shivanshmishra54/Book-Exchange-System.git)
    cd Book-Exchange-System
    ```

2.  **Database Setup:**
    * Open your database management tool (e.g., MySQL Workbench).
    * Create a new database.
        ```sql
        CREATE DATABASE book_exchange_db;
        ```
    
        ```sh
        mysql -u [your_username] -p book_exchange_db < [path_to_your_sql_file.sql]
        ```

3.  **Configure Database Connection:**
    * Navigate to your project's database connection configuration file (e.g., a `DBConnection.java` or `context.xml`).
    * Update the database URL, username, and password to match your local setup:
        ```java
        String url = "jdbc:mysql://localhost:3306/book_exchange_db";
        String user = "your_username";
        String password = "your_password";
        ```

4.  **Deploy to Tomcat:**
    * Open the project in your IDE (Eclipse/IntelliJ).
    * Configure the project to run on your Apache Tomcat server.
    * Right-click the project and select **Run As > Run on Server**.

## 🚀 Usage

1.  Once the server is running, open your web browser and navigate to:
    `http://localhost:8080/Book-Exchange-System/` (or the specific URL for your project)
2.  **Register** for a new account.
3.  **Log in** to your new account.
4.  Navigate to your **Dashboard** and **Add a New Book**.
5.  Go to the **Home** or **Browse** page to see books from other users.
6.  **Request a book** to initiate an exchange.

## 👤 Author

**Shivansh Mishra**

* GitHub: [@shivanshmishra54](https://github.com/shivanshmishra54)
* LinkedIn: (https://www.linkedin.com/in/shivansh-mishra54)
