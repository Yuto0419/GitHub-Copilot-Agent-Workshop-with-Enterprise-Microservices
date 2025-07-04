# GitHub Copilot Agent Workshop - Ski Shop Microservices Edition (Java 21 + Spring Boot)

![Ski Shop Image](./design-docs/ski-shop.png)

You are a new team member. Due to a company transfer or organizational change, you have been assigned to a new project. You are required to quickly understand the existing implementation using GitHub Copilot Agent Mode and implement new features for the existing project.

In this workshop, You will use a comprehensive ski shop microservice system built with Java 21 + Spring Boot as a subject to understand the entire system using GitHub Copilot Agent and learn how to analyze, extend, and optimize enterprise-grade microservices.

The contents of the workshop are as follows.

*This workshop is based on a real enterprise-level Spring Boot microservices architecture and provides practical learning experiences that directly contribute to participants' practical skill improvement in modern Java development.*


> Note:  
> This microservices project, consisting of 9 microservices and over 460 Java files, was created using GitHub Copilot's Agent mode in just a few days. By effectively leveraging AI-driven development with GitHub Copilot, you can implement a project of this scale in a similarly short timeframe.

## Workshop Preparation

### Required Environment

- Visual Studio Code/Eclipse/IntelliJ IDEA + GitHub Copilot
- Java 21 (OpenJDK or Oracle JDK)
- Maven 3.8+
- Browser (Edge/Chrome/Firefox recommended)
- Docker & Docker Compose  (Optional)
- PostgreSQL (Optional: in Docker container)
- Redis (Optional: in Docker container)

### Pre-installation Commands

```bash
# Verify Java 21 installation
java -version

# Verify Maven installation
mvn -version
```

### Project Structure

```text
ski-shop-microservices/
├── frontend-service/             # Thymeleaf frontend (dual theme)
├── api-gateway/                  # Spring Cloud Gateway
├── authentication-service/       # Azure Entra ID OAuth 2.0
├── user-management-service/      # User management with RBAC
├── inventory-management-service/ # Inventory with JPA
├── payment-cart-service/         # Payment with Redis
├── sales-management-service/     # Order management
├── point-service/                # Point and tier system
├── coupon-service/               # Coupon and campaign management
├── ai-support-service/           # LangChain4j AI features
├── monitoring/                   # Prometheus & Grafana
├── scripts/                      # Database initialization
└── all-endpoint-list.md          # Complete API specification (163 endpoints)
```


## "Efficient System Understanding and Development with AI Assistance"

### Workshop Overview

- **Duration**: 2 hours
- **Target Audience**: Beginners to Advanced (with progressive difficulty levels)
- **Theme**: Understanding and extending a complete ski shop system consisting of 9 microservices built with Java 21 + Spring Boot
- **Goal**: Use GitHub Copilot Agent to understand the entire system architecture and implement system enhancements

### System Overview

This workshop covers a complete ski shop system consisting of the following 9 microservices built with **Java 21** and **Spring Boot 3.2**:

1. **Authentication Service** - Authentication and authorization service with Azure Entra ID OAuth 2.0 integration
2. **User Management Service** - User management service with role-based access control
3. **Inventory Management Service** - Inventory management service with JPA and PostgreSQL
4. **Sales Management Service** - Sales management service with distributed transaction support
5. **Payment Cart Service** - Payment and cart service with Redis session management
6. **Point Service** - Point service with tier management system
7. **Coupon Service** - Coupon service with campaign management capabilities
8. **AI Support Service** - AI support service using LangChain4j framework
9. **API Gateway** - API gateway with Spring Cloud Gateway
10. **Frontend Service** - Thymeleaf-based web frontend with dual themes (customer/admin)

- **Total Endpoints**: 163 (General Users: 99, Administrators: 64)
- **Technology Stack**: Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, Spring Cloud Gateway, Thymeleaf, PostgreSQL, Redis, LangChain4j, Maven, Docker

## Part 1: Beginner Level (30 minutes) - "Understanding the System Overview"

### 1.1 System Exploration (10 minutes)

**Story**: As a newly assigned developer, let's first understand the overall system

**Practical Content**:

```text
"What services exist in this system? Please explain each service's role."
"Please show me the folder structure of each service."
"What technology stack is used in this system?"
"Please explain the files in the project root."
```

**Learning Points**:

- GitHub Copilot Agent's workspace analysis capabilities
- Automatic understanding of project structure
- Technology stack identification abilities
- Document analysis functionality

**Expected Response Examples**:

- Roles and responsibilities of the 9 microservices
- Spring Boot 3.2 + Java 21 architecture analysis
- Maven multi-module project configuration
- Dockerized service configuration with docker-compose
- PostgreSQL and Redis integration patterns

### 1.2 API Endpoint Understanding (10 minutes)

**Story**: As a support engineer representative, let's understand the APIs

**Practical Content**:

```text
"Which API endpoint should users use to log in?"
"What APIs are available for searching products?"
"Please explain the flow of APIs for creating an order."
"Please tell me about the APIs for checking and using points."
"Please explain the process for applying coupons."
```

**Learning Points**:

- Natural language API search capabilities
- Understanding the relationship between business functions and APIs
- Understanding dependencies between endpoints
- Learning RESTful API design patterns

### 1.3 Database Design Understanding (10 minutes)

**Story**: As a data analysis team member, let's understand what data is stored

**Practical Content**:

```text
"What database tables exist in this system?"
"Where is user information stored?"
"Please explain the relationship between order data and product data."
"Please explain the data structure of points and coupons."
```

**Learning Points**:

- Automatic database schema analysis
- Understanding entity relationships
- Learning data modeling patterns
- Data distribution design between microservices

## Part 2: Intermediate Level (45 minutes) - "Deep Understanding of Business Logic of Authentication & User Management"

This specialized workshop section focuses on understanding the critical integration between Authentication Service and User Management Service, which forms the foundation of the entire system's security and user lifecycle management.

- **Focus**: Deep analysis of Authentication Service ↔ User Management Service integration. 
- **Duration**: 45 minutes. 
- **Learning Goal**: Master the patterns of service-to-service communication, data consistency, and security token flow and Event propagation. 

### 2.1: High-Level Integration Understanding (10 minutes)

#### 2.1.1 Service Relationship Overview

```text
"Please analyze the relationship between Authentication Service and User Management Service:
- What are the primary responsibilities of each service?
- How do these services collaborate during user registration?
- How do they work together during user login?
- What data is shared between these services?
- What are the boundaries and separation of concerns?
- How is user identity managed across both services?"
```

#### 2.1.2 Integration Architecture Analysis

```text
"Please explain the integration architecture between Authentication Service and User Management Service:
- How do these services communicate with each other?
- What communication protocols are used (REST, messaging, events)?
- Are there any direct database connections between services?
- How is data consistency maintained across services?
- What happens if one service is unavailable?
- How are distributed transactions handled?"
```

#### 2.1.3 Data Flow Overview

```text
"Please describe the data flow between Authentication Service and User Management Service:
- What user information flows from User Management to Authentication?
- What authentication data flows from Authentication to User Management?
- How are user profile updates synchronized between services?
- How are role and permission changes propagated?
- What triggers data synchronization between these services?"
```

### Stage 2.2: Detailed Implementation Analysis (15 minutes)

#### 2.2.1 Authentication Service Deep Dive

```text
"Please analyze the Authentication Service implementation in detail:
- How is Azure Entra ID OAuth 2.0 integration implemented?
- Where are the OAuth configuration settings defined?
- How are JWT tokens generated and validated?
- What user information is stored locally vs. fetched from external services?
- How does the service handle token refresh and expiration?
- What security filters and interceptors are implemented?
- How are user roles and permissions managed within this service?"
```

#### 2.2.2 User Management Service Deep Dive

```text
"Please analyze the User Management Service implementation in detail:
- What user entities and database tables are defined?
- How is user registration processed and validated?
- What user profile information is managed by this service?
- How are user roles and permissions stored and managed?
- What business rules are implemented for user lifecycle management?
- How does this service handle user activation, deactivation, and deletion?
- What APIs are exposed for other services to query user information?"
```

#### 2.2.3 Inter-Service Communication Patterns

```text
"Please analyze how Authentication Service and User Management Service communicate:
- What REST endpoints does Authentication Service call on User Management Service?
- What REST endpoints does User Management Service call on Authentication Service?
- How are service-to-service authentication and authorization handled?
- What error handling and retry mechanisms are implemented?
- How are service discovery and load balancing configured?
- Are there any asynchronous communication patterns (events, messaging)?
- How are API contracts and versioning managed between these services?"
```

#### 2.2.4 Data Consistency and Synchronization

```text
"Please analyze data consistency mechanisms between Authentication and User Management services:
- How is eventual consistency handled between user authentication data and profile data?
- What happens when user information is updated in one service but not the other?
- How are conflicts resolved when the same user data exists in both services?
- What database transaction patterns are used across services?
- How is data integrity maintained during service failures?
- Are there any data reconciliation processes implemented?"
```

### Stage 2.3: Security and Token Flow Analysis (10 minutes)

#### 2.3.1 Authentication Flow Deep Dive

```text
"Please trace the complete authentication flow between these services:
- Step-by-step process when a user logs in via Azure Entra ID
- How is the OAuth callback handled and processed?
- What information is exchanged with User Management Service during login?
- How are user roles and permissions retrieved and cached?
- How is the user session established and maintained?
- What happens during token validation for subsequent requests?"
```

#### 2.3.2 Authorization and Role Management

```text
"Please analyze the authorization mechanisms across both services:
- How are user roles defined and stored?
- Which service is authoritative for role-based access control?
- How are @PreAuthorize annotations used across both services?
- How are admin privileges managed and validated?
- What security contexts are shared between services?
- How are cross-service authorization calls secured?"
```

#### 2.3.3 Security Token Lifecycle

```text
"Please explain the security token lifecycle across both services:
- How are JWT tokens structured and what claims do they contain?
- How is token validation performed by User Management Service?
- What happens during token refresh scenarios?
- How are expired or revoked tokens handled?
- How is logout processed across both services?
- What security headers and CORS configurations are implemented?"
```

### Stage 2.4: Local Environment Testing and Verification (10 minutes)

#### 2.4.1 Local Setup and Configuration

```text
"Please provide detailed steps to set up and run Authentication Service and User Management Service locally for testing their integration:
- What are the prerequisite software and configuration requirements?
- How should I configure the database connections for both services?
- What Azure Entra ID configuration is needed for local development?
- How should I set up the application.yml/properties files?
- What environment variables need to be configured?
- How should I handle secrets and sensitive configuration locally?
- What is the correct startup sequence for these services?"
```

#### 2.4.2 Integration Testing Scenarios

```text
"Please provide specific test scenarios to verify the integration between Authentication and User Management services:
- How can I test user registration flow end-to-end?
- How can I verify Azure Entra ID OAuth login works correctly?
- How can I test user profile updates and synchronization?
- How can I verify role-based access control across services?
- What tools can I use to monitor inter-service communication?
- How can I test error scenarios (service unavailability, timeout)?
- What logs should I examine to troubleshoot integration issues?"
```

#### 2.4.3 API Testing and Verification

```text
"Please provide step-by-step instructions for testing the APIs between Authentication and User Management services:
- How can I test the authentication endpoints using curl or Postman?
- How can I obtain and use JWT tokens for subsequent API calls?
- How can I test admin-only endpoints and verify proper authorization?
- How can I test user management endpoints from the authentication context?
- What are the expected request/response formats for key integration points?
- How can I verify cross-service API calls are working correctly?
- What monitoring endpoints should I check for service health?"
```

#### 2.4.4 Database and Data Verification

```text
"Please guide me through verifying data consistency between Authentication and User Management services:
- How can I examine the database schemas and data for both services?
- What SQL queries can I run to verify user data synchronization?
- How can I check if user roles and permissions are correctly stored?
- How can I verify authentication tokens are properly linked to user profiles?
- What database tools are recommended for local development?
- How can I reset or clean up test data between test runs?
- What database migration scripts should I be aware of?"
```

#### 2.4.5 Troubleshooting and Debugging

```text
"Please provide guidance for troubleshooting common issues with Authentication and User Management service integration:
- What are the most common configuration issues and how to resolve them?
- How can I debug authentication failures between services?
- What log levels and patterns should I enable for effective debugging?
- How can I trace inter-service API calls and their performance?
- What are the typical error messages and their meanings?
- How can I verify network connectivity and service discovery?
- What monitoring tools can help identify integration problems?"
```

### 2.5 Team Presentation of Analysis Results (3-5 minutes)

**Story**: Representative presents the analysis results

**Learning Points**:

- Understanding complex business logic in Spring Boot microservices
- Understanding inter-service coordination with Spring Cloud
- Understanding error handling patterns and exception management
- Verification of Domain-Driven Design (DDD) patterns in Java
- Analysis of JPA entity relationships and transaction management

**Expected Learning Outcomes**:

- Understanding collaborative processing between Spring Boot microservices
- Verification of distributed transaction patterns with Spring Data
- Understanding integration of inventory management with point/coupon systems using JPA

## Part 3: Advanced Level (45 minutes) - "New Frontend Service Implementation"

**Before conducting this workshop, please rename the existing `Frontend Service` directory or delete the directory.**

### 3.1. Frontend Service Requirements Analysis (10 minutes)

**Story**: As a frontend architect, you need to implement a new frontend microservice from scratch to replace the existing one

**Practical Content**:

```text
"Please analyze the design-docs/frontend-design.md to understand the frontend requirements."
"Please review all-endpoint-list.md to identify which endpoints the frontend should consume."
"What are the functional requirements for general users vs administrators?"
"How should we design the architecture for a dual-theme frontend service?"
"What technology stack should we use for the new frontend microservice?"
"How should we structure the project to support both customer and admin interfaces?"
"What authentication and authorization integration is required?"
"Please create a development plan for implementing the new frontend service."
```

**Analysis Target**:

Based on the specifications, the new frontend service should provide:

- **General Users**: Customer web interface with light blue theme accessing 99 endpoints
- **Administrators**: Admin dashboard with light red theme accessing 64 endpoints
- **Technology Stack**: Java 21 + Spring Boot 3.2 + Thymeleaf + Bootstrap
- **Authentication**: Azure Entra ID OAuth 2.0 integration
- **Responsive Design**: Mobile-friendly interface for both themes

### 3.2 Customer Frontend Implementation (15 minutes)

**Story**: As a Spring Boot developer, implement the customer-facing frontend with light blue theme

**Practical Content**:

```text
"Please create a new Spring Boot microservice project for the frontend service."
"Implement the customer interface with a light blue theme based on the design specifications."
"Create the following customer pages based on all-endpoint-list.md:
- Homepage with product showcase and search
- Product catalog and detail pages
- Shopping cart and checkout flow
- User profile and order history
- Point and coupon management
- AI chat support interface"

"For each page, please:
- Use Thymeleaf templates with light blue color scheme (#E3F2FD, #BBDEFB, #90CAF9)
- Implement responsive design with Bootstrap
- Integrate with appropriate backend APIs from all-endpoint-list.md
- Add proper error handling and user feedback
- Include navigation and user authentication status"

"Specific implementation requirements:
- Product search should call GET /api/products/search
- Cart operations should use POST /api/v1/cart/items
- Order creation should use POST /api/v1/orders
- Point checking should use GET /api/v1/points/balance
- AI recommendations should use GET /api/v1/recommendations/{userId}"
```

**Technical Implementation Areas**:

- Spring Boot 3.2 project setup with Maven
- Spring MVC controllers for customer pages
- Thymeleaf templates with light blue theme
- Bootstrap responsive design implementation
- RestTemplate/WebClient for API integration
- Spring Security for authentication
- Form validation and error handling
- AJAX for dynamic content loading

### 3.3. Administrator Dashboard Implementation (15 minutes)

**Story**: As a backend developer, implement the administrator dashboard with light red theme

**Practical Content**:

```text
"Implement the administrator interface with a light red theme based on the design specifications."
"Create the following admin pages based on all-endpoint-list.md:
- Admin dashboard with system overview and statistics
- Product and inventory management
- Order and shipping management
- User management and role assignment
- Reports and analytics
- Campaign and coupon management"

"For each admin page, please:
- Use Thymeleaf templates with light red color scheme (#FFEBEE, #FFCDD2, #EF9A9A)
- Implement data tables for management interfaces
- Add batch operations for efficiency
- Include data visualization charts
- Implement proper admin role-based access control
- Add audit logging for admin actions"

"Specific implementation requirements:
- Product management should use POST /api/products, PUT /api/categories/{id}
- Order management should use PUT /api/v1/orders/{orderId}/status
- User management should use GET /api/admin/users
- Reports should use GET /api/v1/reports/sales
- Campaign management should use POST /api/v1/campaigns"

"Advanced features to implement:
- Real-time monitoring dashboard using WebSocket
- Export functionality for reports (PDF, Excel)
- Bulk operations for user and product management
- Advanced search and filtering capabilities
- System health monitoring and alerts"
```

**Technical Implementation Areas**:

- Admin-specific Spring MVC controllers
- Thymeleaf templates with light red theme
- Data table components with pagination
- Chart.js integration for data visualization
- WebSocket for real-time updates
- File export functionality
- Role-based security configuration
- Batch processing capabilities

### 3.4 Frontend Service Integration and Testing (5 minutes)

**Story**: As a full-stack developer, integrate and test the complete frontend service

**Practical Content**:

```text
"Please help me integrate and test the new frontend service:"
"How should I configure the application.yml for the frontend service?"
"What dependencies should be added to pom.xml for all required features?"
"How should I implement the API Gateway integration for the frontend service?"
"Please create comprehensive integration tests for both customer and admin interfaces."
"How should I implement error handling and fallback mechanisms for API calls?"
"What security configurations are needed for the dual-theme frontend?"
"How can I test the authentication flow with Azure Entra ID?"
"Please help me implement health checks and monitoring for the frontend service."

"Testing scenarios to implement:
- End-to-end customer purchase flow
- Admin user management workflows
- API integration error handling
- Authentication and authorization flows
- Cross-browser compatibility testing
- Mobile responsiveness testing
- Performance testing for page load times"
```

**Integration Points**:

- API Gateway routing configuration
- Authentication service integration
- Error handling and circuit breaker patterns
- Caching strategies for better performance
- Security headers and CORS configuration
- Monitoring and logging integration

**GitHub Copilot Agent Utilization Points**:

```text
"Please help me create a new Spring Boot project structure for the frontend service."
"Generate Thymeleaf templates with light blue theme for customer pages."
"Generate Thymeleaf templates with light red theme for admin pages."
"Create Spring MVC controllers for all customer and admin functionalities."
"Generate DTO classes for API request/response structures based on all-endpoint-list.md."
"Implement RestTemplate/WebClient integration for calling backend APIs."
"Create comprehensive error handling and validation for both interfaces."
"Generate responsive CSS/Bootstrap components for both themes."
"Implement Spring Security configuration for dual-theme authentication."
"Create integration tests for both customer and admin workflows."
"Generate configuration files (application.yml, pom.xml) with all necessary dependencies."
"Implement caching strategies for better frontend performance."
"Create monitoring and health check endpoints for the frontend service."
```

### 3.5: Review and Demonstration (10 minutes)

### Comprehensive Exercise and Demo

**Story**: As a project manager, let's introduce the newly implemented frontend system to stakeholders

**Practical Content**:

```text
"Please demonstrate the new customer website functionality with light blue theme."
"Please introduce the new administrator dashboard with light red theme."
"Please explain how the frontend integrates with all backend microservices."
"Please create an overall architecture diagram showing the new frontend service integration."
"Please summarize how GitHub Copilot Agent was helpful during the frontend implementation process."
"Please explain the design decisions for dual-theme implementation."
"Please demonstrate the responsive design features on different devices."
"Please show the API integration patterns used in the frontend service."
```

## Workshop Learning Effects and Usage Patterns

### 1. Progressive Skill Development

- **Beginners**: Basic information gathering and system understanding
- **Intermediate**: Business logic analysis and service integration understanding
- **Advanced**: Complete a new microservice implementation from scratch

### 2. Diverse Usage Methods of GitHub Copilot Agent

- **Information Gathering and Analysis**: Workspace analysis, Spring Boot application analysis, enterprise architecture understanding
- **Code Generation**: Complete Spring Boot microservice implementation, Thymeleaf templates, Spring MVC controllers
- **Integration Implementation**: API client generation, authentication integration, error handling
- **Design Implementation**: Dual-theme UI generation, responsive design, user experience optimization

### 3. Application to Practical Work

- Understanding and documenting existing Spring Boot microservices systems
- Impact analysis when developing new features in enterprise Java applications
- Complete frontend microservice implementation from scratch
- Productivity improvement through AI pair programming in Java ecosystem
- Dual-theme web application development with enterprise-grade features

### 4. Deliverables

- **Understanding Outcomes**: Comprehensive microservices architecture documentation and analysis
- **Implementation Outcomes**:
  - Complete a new microservice
  - Customer interface with light blue theme and 99 endpoint integrations
  - Administrator dashboard with light red theme and 64 endpoint integrations
  - Responsive design supporting multiple device types
  - Full API integration with all backend microservices
- **Knowledge Outcomes**: Best practices for GitHub Copilot Agent utilization in enterprise Java development

## Expected Learning Outcomes

Through this workshop, participants will acquire:

1. **System Understanding Skills**: Efficient enterprise Java application analysis techniques using GitHub Copilot Agent
2. **Development Efficiency**: Productivity improvement methods through AI pair programming in Java ecosystem
3. **New Function Implementation Skills**: Complete Spring Boot + Thymeleaf frontend microservice development
4. **Integration Knowledge**: Microservices communication patterns and API integration techniques
5. **UI/UX Design Skills**: Dual-theme implementation and responsive design patterns
6. **Practical Application**: Methods for utilizing GitHub Copilot Agent in enterprise Java projects

## Additional Workshop: Implementation of Unimplemented Features

This project is still incomplete. There are many unimplemented features remaining in the project marked as `//TODO`. Please implement these unimplemented features one by one using `GitHub Copilot Agent Mode` or `Coding Agent`.

## Appendix: Practical Prompt Examples for GitHub Copilot Agent

This section provides specific prompt examples that participants can use during the workshop to effectively analyze and understand the Spring Boot microservices system.

### 1. System Overview Analysis Prompts

#### 1.1 Complete System Understanding

- **Purpose**  
 	Use this prompt to quickly gain a holistic understanding of the entire microservices project. It enables participants to comprehend how the system is architected, what services it encompasses, and which technologies are used, laying the groundwork for all further analysis and development.

- **Expected Outcome**  
	You will obtain a comprehensive overview of the system architecture, services, technology stack, data persistence strategies, and security approach. This foundational knowledge will help orient new developers or stakeholders to the system’s landscape.

	```text
	"Please analyze this ski shop microservices project and provide a comprehensive overview including:
	- List of all microservices and their primary responsibilities
	- Technology stack used (Java version, Spring Boot version, databases, etc.)
	- Overall architecture pattern (API Gateway, service discovery, etc.)
	- Inter-service communication methods
	- Security implementation approach
	- Data persistence strategies for each service"
	```

#### 1.2 Project Structure Analysis

- **Purpose:**  
	Apply this prompt to understand how the codebase and supporting components are structured. This allows participants to identify how projects are modularized, where essential assets and configurations are located, and how the system is set up for deployment and monitoring.

- **Expected Outcome:**  
	You will gain insights into the Maven configuration, containerization details, scripts for initializing databases, and observability assets. This knowledge is useful for both local development and production deployment.

	```text
	"Please examine the project structure and explain:
	- Maven multi-module configuration and dependencies
	- Docker and containerization setup
	- Database initialization scripts and configurations
	- Monitoring and observability setup (Prometheus, Grafana)
	- Development and deployment workflows"
	```

#### 1.3 Business Domain Understanding

- **Purpose:**  
	Use this prompt to explore the business context of the system. It helps participants appreciate what the shop actually does, the key user journeys, the main business rules, and any value-added services such as AI-powered features.

- **Expected Outcome:**  
	You’ll be able to articulate the business features of the ski shop including its offerings, customer flows, loyalty programs, and advanced support functionalities. This understanding is critical for meaningful system enhancement.

	```text
	"Based on the code and documentation, please explain the business domain of this ski shop system:
	- What products and services does this ski shop offer?
	- What are the main customer journeys supported?
	- What business rules are implemented across services?
	- How does the point and coupon system work?
	- What AI-powered features are available to customers?"
	```

### 1.4 Presentation of Analysis Results by Participant Teams (3-5 minutes)

The representatives of the participant teams will present the content of their analysis.

### 2. Microservice-Specific Analysis Prompts

#### 2.1 Service Entry Point Discovery

- **Purpose:**  
	Leverage this prompt to identify where to start analyzing a specific microservice. It guides participants directly to the main application class, controllers, models, and key configurations relevant to the domain logic and technical implementation.

- **Expected Outcome:**  
	You’ll receive a roadmap that pinpoints the main files and packages for effective codebase navigation and deeper technical understanding, accelerating onboarding and troubleshooting.

	```text
	"For the [SERVICE_NAME] microservice, please help me understand where to start my analysis:
	- What is the main application class and configuration?
	- Which controller classes handle the main business logic?
	- What are the key entity classes and their relationships?
	- Where are the service layer implementations?
	- What configuration files should I examine first?
	- Are there any README files or documentation specific to this service?"
	
	Example for specific services:
	- "For the authentication-service, please guide me through the key files..."
	- "For the ai-support-service, please show me the LangChain4j implementation..."
	- "For the payment-cart-service, please explain the Redis integration..."
	```

#### 2.2 Service Dependencies Analysis

- **Purpose:**  
	Use this prompt to map out the dependencies and integrations of a given microservice. This includes external service calls, database usage, third-party libraries, and secret management techniques.

- **Expected Outcome:**  
	You’ll have a clear picture of what resources each service depends on, which is essential for debugging, performance tuning, or scaling the service.

	```text
	"For the [SERVICE_NAME] microservice, please analyze:
	- What external services does it depend on?
	- What databases or data stores does it use?
	- How does it communicate with other microservices?
	- What third-party libraries or frameworks are integrated?
	- What configuration properties are required for operation?
	- How are secrets and sensitive data managed?"
	```

#### 2.3 Service Configuration Understanding

- **Purpose:**  
	Apply this prompt to assess how each microservice is configured. It helps participants understand application properties files, security settings, database bindings, and other aspects crucial to service operation.

- **Expected Outcome:**  
	You’ll understand how to configure, run, and secure the service in various environments, and be equipped to troubleshoot related issues.

	```text
	"Please analyze the Spring Boot configuration for [SERVICE_NAME]:
	- Application properties and profiles
	- Security configuration (Spring Security setup)
	- Database configuration (JPA, connection pooling)
	- Caching configuration (Redis, local cache)
	- Integration configurations (OAuth, external APIs)
	- Monitoring and health check setup"
	```

### 3. Endpoint and API Analysis Prompts

#### 3.1 Complete Endpoint Discovery

- **Purpose:**  
	Use this prompt to discover all API endpoints across the microservices, including their access restrictions, HTTP methods, and assigned roles.

- **Expected Outcome:**  
	A complete API map, categorized by access level, to facilitate communication with backend services and guide frontend or integration development.

	```text
	"Please analyze all REST endpoints in this microservices system:
	- List all controllers across all services
	- Categorize endpoints by user type (public, authenticated user, admin)
	- Show the HTTP methods and paths for each endpoint
	- Identify which endpoints require specific permissions or roles
	- Explain the purpose of each major endpoint group
	- Highlight any deprecated or experimental endpoints"
	```

#### 3.2 Service-Specific Endpoint Analysis

- **Purpose:**  
	Use this prompt to focus on the API surface of a particular microservice, including endpoint definitions, payloads, and validation rules.

- **Expected Outcome:**  
	Tailored understanding of a service’s API for consuming, testing, or extending its functionality.

	```text
	"For the [SERVICE_NAME] microservice, please provide:
	- Complete list of all REST endpoints with HTTP methods and paths
	- Request and response data structures for each endpoint
	- Authentication and authorization requirements
	- Validation rules and constraints
	- Error handling and response codes
	- Usage examples for key endpoints"
	
	Example queries:
	- "Show me all endpoints in the inventory-management-service"
	- "List all admin-only endpoints in the user-management-service"
	- "Explain the AI chat endpoints in the ai-support-service"
	```

#### 3.3 API Integration Patterns

- **Purpose:**  
	Apply this prompt to understand the patterns and mechanisms behind inter-service API communication, including how calls are made, handled, and managed.

- **Expected Outcome:**  
	You’ll learn how services interact with each other, which technologies they use for remote calls, and how they ensure reliability and consistency.

	```text
	"Please analyze how this microservice integrates with others:
	- What APIs does [SERVICE_NAME] call from other services?
	- How are service-to-service calls implemented (RestTemplate, WebClient, Feign)?
	- What error handling and retry mechanisms are in place?
	- How is service discovery handled?
	- Are there any circuit breaker patterns implemented?
	- How are distributed transactions managed?"
	```

### 4. System Architecture Diagram Prompts

#### 4.1 High-Level Architecture Diagram

- **Purpose:**  
	Use this prompt to get a visual big-picture representation of the system’s architecture, showing all major components and their relationships.

- **Expected Outcome:**  
	You’ll have a conceptual or ASCII diagram that can be shared with team members or converted into a proper architectural diagram for documentation or presentations.

	```text
	"Please create a comprehensive system architecture diagram for this ski shop microservices system showing:
	- All 10 microservices with their primary responsibilities
	- Data flow between services
	- Database connections for each service
	- External integrations (Azure Entra ID, AI services)
	- Load balancing and API Gateway routing
	- Monitoring and logging infrastructure
	- Security boundaries and authentication flow
	
	Format the diagram using ASCII art or describe it in a way that can be easily converted to a visual diagram."
	```

#### 4.2 Service Interaction Diagram

- **Purpose:**  
	Use this prompt to understand the runtime interactions between services, especially focusing on request routing and authentication flows.

- **Expected Outcome:**  
	You’ll receive an interaction flow that’s useful for developing new features, identifying bottlenecks, or troubleshooting integration issues.

	```text
	"Create a service interaction diagram that shows:
	- How the frontend service communicates with backend services
	- The role of the API Gateway in routing requests
	- Authentication token flow across services
	- Data synchronization patterns between services
	- Event-driven communication (if any)
	- Caching layers and their interactions"
	```

#### 4.3 Database Architecture Diagram

- **Purpose:**  
	Apply this prompt to visualize the database landscape of the microservices and how data is organized, distributed, and interrelated among services.

- **Expected Outcome:**  
	You’ll have clarity on which service owns which data, inter-entity relationships, and any cross-service data patterns, aiding in planning migrations or extending functionality.

	```text
	"Please create a database architecture diagram showing:
	- Which services use which databases (PostgreSQL, Redis)
	- Entity relationships within each service's database
	- Cross-service data dependencies
	- Data consistency patterns
	- Backup and replication strategies (if implemented)
	- Migration and versioning approaches"
	```

### 5. Frontend Service Call Sequence Diagrams

#### 5.1 Customer Purchase Flow Sequence

- **Purpose:**  
	Use this prompt to decompose and visualize the multi-step process of a critical business workflow such as a customer purchase, including all services involved.

- **Expected Outcome:**  
	A detailed sequence diagram that helps both backend and frontend developers implement, test, and troubleshoot this end-to-end business flow.

	```text
	"Please create a detailed sequence diagram for a customer making a purchase, showing the interaction between:
	- Frontend Service (Thymeleaf controller)
	- API Gateway
	- Authentication Service (user verification)
	- Inventory Management Service (product availability check)
	- Point Service (point balance and usage)
	- Coupon Service (coupon validation and application)
	- Payment Cart Service (payment processing)
	- Sales Management Service (order creation)
	
	Include:
	- HTTP request/response details
	- Authentication token passing
	- Error handling scenarios
	- Database transactions
	- Time estimates for each step"
	```

#### 5.2 Admin Dashboard Data Loading Sequence

- **Purpose:**  
	Apply this prompt to understand how administrator-facing pages gather and synthesize information from various backend sources for real-time visibility.

- **Expected Outcome:**  
	A clear workflow view for implementing optimal dashboard loading and ensuring timely information for administrators.

	```text
	"Create a sequence diagram for an administrator accessing the dashboard, showing:
	- Frontend Service admin controller loading dashboard
	- Authentication and authorization checks
	- Multiple parallel API calls to gather dashboard data:
	  - Sales Management Service (recent orders, sales statistics)
	  - Inventory Management Service (low stock alerts, product metrics)
	  - User Management Service (user registration trends)
	  - Point Service (point redemption statistics)
	  - Coupon Service (campaign performance)
	- Data aggregation and presentation in the Thymeleaf template
	- Real-time updates (WebSocket connections if implemented)"
	```

#### 5.3 AI Chat Feature Sequence

- **Purpose:**  
	Use this prompt to decompose the workflow for AI-powered customer interaction, including all backend integrations required for chat services and personalized recommendations.

- **Expected Outcome:**  
	A granular view of the real-time AI support feature to inform implementation, extension, or troubleshooting.

	```text
	"Please create a sequence diagram for the AI chat feature showing:
	- Customer initiating chat from frontend
	- Frontend Service handling the chat interface
	- API Gateway routing to AI Support Service
	- LangChain4j processing the user query
	- Integration with product recommendation algorithms
	- Response generation and streaming back to frontend
	- Chat history persistence
	- Integration with other services for contextual information (user profile, purchase history)"
	```

#### 5.4 Real-time Features Sequence

- **Purpose:**  
	Use this prompt to model real-time interactions (WebSocket, live updates) supported by the system and their infrastructure.

- **Expected Outcome:**  
	An operational blueprint to implement, test, and optimize real-time features like inventory updates, notifications, or order tracking.

	```text
	"Create a sequence diagram for real-time features in the system:
	- WebSocket connection establishment between frontend and backend
	- Real-time inventory updates during shopping
	- Live order status updates
	- Real-time point balance changes
	- Push notifications for promotions or alerts
	- Concurrent user session management
	- Connection handling and reconnection strategies"
	```

#### 5.5 Payment Processing Sequence

- **Purpose:**  
	Apply this prompt to visualize the multi-step, multi-service flow of customer payments, including all business logic, validation, and confirmation steps.

- **Expected Outcome:**  
	A sequence diagram ensuring a robust, secure, and user-friendly payment experience implemented across the stack.

	```text
	"Please create a sequence diagram for the payment processing flow showing:
	- Customer checking out in the frontend
	- Frontend Service validating cart and calculating total
	- API Gateway routing to Payment Cart Service
	- Payment Cart Service processing payment with Redis session
	- Sales Management Service creating order and updating inventory
	- Point Service and Coupon Service applying discounts and points
	- Sending confirmation email to customer
	- Updating order status in User Management Service"
	```

### 6: Advanced Integration Patterns

#### 6.1 Performance Optimization

- **Purpose:**  
	Use this prompt to identify opportunities for improving the performance of inter-service communication, especially between core services.

- **Expected Outcome:**  
	Actionable recommendations for implementing caching, connection optimization, and tracking performance bottlenecks.

	```text
	"Please analyze performance optimization opportunities for Authentication and User Management service integration:
	- How can caching be implemented to reduce inter-service calls?
	- What data can be cached and for how long?
	- How can connection pooling be optimized between services?
	- What are the performance bottlenecks in the current integration?
	- How can response times be improved for authentication flows?
	- What metrics should be monitored for performance optimization?"
	```

#### 6.2 Scalability and Resilience

- **Purpose:**  
	Apply this prompt to examine how the system manages high loads, failures, and ensures business continuity through resilience and scaling strategies.

- **Expected Outcome:**  
	Practical insights into circuit breakers, load balancing, backup plans, and disaster recovery within the system.

	```text
	"Please analyze scalability and resilience patterns for these services:
	- How can these services scale independently?
	- What happens when User Management Service is under high load?
	- How are circuit breaker patterns implemented (if any)?
	- What backup and failover mechanisms exist?
	- How can the integration handle increased authentication traffic?
	- What disaster recovery considerations exist for these services?"
	```

### 7. Frontend Service Implementation Prompts (for Part 3 Workshop)

#### 7.1 Project Setup and Structure

- **Purpose:**  
	Use this prompt to generate a new Spring Boot frontend microservice project and lay a solid foundation for both customer and admin interfaces.

- **Expected Outcome:**  
	You receive a ready-to-use project structure, dependency list, and recommendations for scalable and maintainable code organization.

	```text
	"Please help me create a new Spring Boot frontend microservice project from scratch:
	- Generate the Maven project structure for a Spring Boot 3.2 application
	- What dependencies should I include in pom.xml for Thymeleaf, Spring Security, and API integration?
	- How should I structure the project to support both customer and admin interfaces?
	- What package structure would you recommend for controllers, services, and configuration?
	- Please create the main application class and basic configuration files"
	```

#### 7.2 Design Specification Analysis

- **Purpose:**  
	Apply this prompt to deeply understand the requirements and design specifications for the dual-theme frontend.

- **Expected Outcome:**  
	A checklist of required features, page structures, and endpoint integrations for both customer and admin user roles.

	```text
	"Please analyze the design-docs/frontend-design.md and all-endpoint-list.md files:
	- What are the key functional requirements for the customer interface?
	- What are the key functional requirements for the admin interface?
	- How should I implement the dual-theme system (light blue for customers, light red for admins)?
	- What are the main page layouts and navigation structures needed?
	- Which endpoints from all-endpoint-list.md should be integrated for each user type?"
	```

#### 7.3 Customer Interface Implementation

- **Purpose:**  
Use this prompt to rapidly prototype and implement the customer-facing site with theme, layout, and API integration.

- **Expected Outcome:**  
Ready-to-integrate Thymeleaf templates, controller skeletons, and theme-specific resources for customer flows.

	```text
	"Please help me implement the customer interface with light blue theme:
	- Create Thymeleaf templates for the homepage, product catalog, and product detail pages
	- Implement the shopping cart and checkout flow templates
	- Create user profile and order history pages
	- Design the AI chat support interface
	- Generate CSS styles using light blue color palette (#E3F2FD, #BBDEFB, #90CAF9)
	- Implement responsive design with Bootstrap for mobile compatibility
	- Create Spring MVC controllers for all customer functionalities"
	
	"For API integration, please help me:
	- Implement product search using GET /api/products/search
	- Integrate cart operations with POST /api/v1/cart/items
	- Connect order creation with POST /api/v1/orders
	- Add point balance checking with GET /api/v1/points/balance
	- Integrate AI recommendations with GET /api/v1/recommendations/{userId}"
	```

#### 7.4 Administrator Interface Implementation

- **Purpose:**  
Leverage this prompt to implement the admin dashboard and management features, including necessary visual components and batch operations.

- **Expected Outcome:**  
Thymeleaf templates and controllers for the admin dashboard, with the proper theme, table components, and backend API integration.

	```text
	"Please help me implement the administrator interface with light red theme:
	- Create Thymeleaf templates for the admin dashboard with system overview
	- Implement product and inventory management interfaces
	- Create order and shipping management pages
	- Design user management and role assignment interfaces
	- Implement reports and analytics pages with data visualization
	- Generate CSS styles using light red color palette (#FFEBEE, #FFCDD2, #EF9A9A)
	- Create data tables with pagination and search functionality
	- Implement batch operations for administrative efficiency"
	
	"For admin API integration, please help me:
	- Connect product management with POST /api/products and PUT /api/categories/{id}
	- Integrate order management with PUT /api/v1/orders/{orderId}/status
	- Implement user management with GET /api/admin/users
	- Add reporting functionality with GET /api/v1/reports/sales
	- Connect campaign management with POST /api/v1/campaigns"
	```

#### 7.5 Authentication and Security Implementation

- **Purpose:**  
	Use this prompt to implement proper security, user authentication, and access control, ensuring safe operation for both general and admin users.

- **Expected Outcome:**  
	A secure, maintainable frontend with role-based access, session handling, CSRF protection, and graceful handling of authentication errors.

	```text
	"Please help me implement authentication and security for the dual-theme frontend:
	- Configure Spring Security for Azure Entra ID OAuth 2.0 integration
	- Implement role-based access control for customer vs admin interfaces
	- Create login and logout flows for both user types
	- Set up session management and security contexts
	- Implement CSRF protection and security headers
	- Configure different access rules for customer and admin endpoints
	- Handle authentication errors and unauthorized access gracefully"
	```

#### 7.6 API Integration and Error Handling

- **Purpose:**  
	Leverage this prompt to ensure all backend interactions are robust, resilient, and observable, with proper logging and fallbacks.

- **Expected Outcome:**  
	A reliable, error-tolerant frontend with smart retry, circuit breakers, logging, and graceful fallback mechanisms.

	```text
	"Please help me implement robust API integration:
	- Create RestTemplate or WebClient configurations for calling backend services
	- Implement proper error handling for API failures and timeouts
	- Add retry mechanisms and circuit breaker patterns
	- Create DTO classes for API request/response mapping
	- Implement caching strategies for frequently accessed data
	- Add proper logging for API calls and responses
	- Handle service unavailability gracefully with fallback mechanisms"
	```

#### 7.7 Testing and Quality Assurance

- **Purpose:**  
Use this prompt to automate testing and validation of both customer and admin functions, securing quality throughout development.

- **Expected Outcome:**  
Comprehensive tests for essential business flows, security, performance, and compatibility, enabling regression-free releases.

	```text
	"Please help me create comprehensive tests for the frontend service:
	- Generate unit tests for Spring MVC controllers
	- Create integration tests for API connectivity
	- Implement end-to-end tests for critical user workflows
	- Add tests for both customer and admin authentication flows
	- Create performance tests for page load times
	- Implement cross-browser compatibility tests
	- Add responsive design tests for different screen sizes
	- Generate test data for both customer and admin scenarios"
	```

#### 7.8 Configuration and Deployment

- **Purpose:**  
	Apply this prompt to produce robust runtime configurations and streamline deployment, monitoring, and scaling operations.

- **Expected Outcome:**  
	Deployment-ready configuration, Docker support, health checks, and strategies for safe, observable, and scalable production roll-out.

	```text
	"Please help me configure the frontend service for deployment:
	- Create application.yml configuration with profiles for different environments
	- Configure API Gateway integration and service discovery
	- Set up monitoring and health check endpoints
	- Create Docker configuration for containerized deployment
	- Configure logging and metrics collection
	- Set up environment-specific configuration management
	- Implement graceful shutdown and startup procedures
	- Configure load balancing and scaling considerations"
	```

