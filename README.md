# LibXpress

LibXpress is a library management system with integrated features such as book recommendation, wallet management, transaction handling, and more. This system leverages various Java technologies for backend services and a flexible, modular design for scalability.

## Folder Structure

### **APIResponse**
- **APIResponse.java**: Handles API responses across the application.

### **Config**
- **AsyncConfig.java**: Configuration for asynchronous operations.
- **ModelMapperConfig.java**: Configuration for model mapping between entities and DTOs.
- **SecurityConfig.java**: Security configuration for the application.
- **SwaggerConfig.java**: Swagger configuration for API documentation.

### **Controller**
- **AdminBookController.java**: Manages book-related operations for administrators.
- **AuthController.java**: Handles authentication operations.
- **BookController.java**: Manages general book-related actions such as borrowing and returning books.
- **DealingController.java**: Handles book transactions and interactions.
- **WalletController.java**: Manages user wallet transactions.

### **DTOs (Data Transfer Objects)**
- **BookDTO.java**: Book details for transfer between layers.
- **TransactionDTO.java**: Transaction-related data.
- **UserDTO.java**: User-related data.

### **Entities**
- **Books.java**: Entity representing the books in the system.
- **BorrowedBooks.java**: Represents the books borrowed by users.
- **Feedback.java**: Represents user feedback on books.
- **OTPVerification.java**: OTP verification entity for user registration.
- **Transaction.java**: Represents financial transactions.
- **User.java**: Represents users in the system.
- **Wallet.java**: Represents user wallet data.

### **JWTCnfig**
- **JwtAuthenticationFilter.java**: JWT authentication filter for verifying tokens.
- **JwtUtils.java**: Utility class for generating and validating JWT tokens.

### **Repositories**
- **BookRepo.java**: Repository for book-related database operations.
- **BorrowedRepo.java**: Repository for managing borrowed books.
- **FeedbackRepo.java**: Repository for managing feedback.
- **OTPVerificationRepo.java**: Repository for OTP verification operations.
- **TransactionRepo.java**: Repository for transaction-related operations.
- **UserRegistationRepo.java**: Repository for user registration.
- **UserRepo.java**: Repository for user-related database operations.
- **WalletRepo.java**: Repository for wallet-related operations.

### **Security**
- **CustomUserDetailService.java**: Custom user detail service for loading user details.
- **CustomUserDetails.java**: Custom user details implementation.

### **Services**
- **RecommendationSystem**
  - **BackgroundWork.java**: Background tasks for recommendation system operations.
  - **BookService.java**: Service for managing book operations.
  - **DealingService.java**: Service for handling book transactions.
  - **PaymentService.java**: Service for managing payments.
  - **TransationService.java**: Service for transaction operations.
  - **UserService.java**: Service for user-related operations.
  - **UserUitlsService.java**: Service for user utilities.
  - **WalletService.java**: Service for wallet-related operations.

### **LibXpressApplication.java**
- Main application class to run the Spring Boot application.

## Features

- **Book Recommendation System**: Hybrid-based recommendation system using collaborative and content-based filtering.
- **User Management**: User registration, authentication, and management.
- **Wallet System**: Users can deposit, withdraw, and manage their wallet balance.
- **Transaction Management**: Detailed tracking of book borrowing transactions and feedback.

## Prerequisites

- JDK 11 or higher
- Maven
- Spring Boot
- MySQL (or any preferred database)

## Setup

1. **Clone the repository**:

   ```bash
   git clone https://github.com/mrsharukh03/libXpress.git
   ```

2. **Navigate to the project directory**:

   ```bash
   cd libXpress
   ```

3. **Install dependencies**:

   Use Maven to install the necessary dependencies:

   ```bash
   mvn clean install
   ```

4. **Configure Database**:

   Update `src/main/resources/application.yml` with your database credentials.

5. **Run the Application**:

   Start the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**:

   After starting the application, you can access it via `http://localhost:8080`.

## API Documentation

Swagger UI is available to view and interact with all the available API endpoints. Once the application is running, access the documentation at:

```
http://localhost:8080/swagger-ui.html
```
