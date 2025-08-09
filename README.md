# Ticketing System - Microservices Architecture

A complete ticket booking system built with Spring Boot microservices, demonstrating event-driven architecture, API Gateway patterns, OAuth2 authentication, and distributed system design.

<img width="2372" height="1517" alt="ticketing-api-flow" src="https://github.com/user-attachments/assets/9ffc2f39-30a8-46db-9c22-69ea4c37a26d" />

## 🏗️ Architecture Overview

This application follows a microservices architecture pattern with the following components:

### Core Services

1. **API Gateway** (Port 8090)
   - Single entry point for all client requests
   - OAuth2/JWT authentication via Keycloak
   - Circuit breaker pattern with Resilience4j
   - Load balancing and routing
   - Centralized API documentation

2. **Inventory Service** (Port 8080)
   - Manages venues and events
   - Tracks ticket availability
   - Database-first approach with Flyway migrations
   - Provides inventory information to other services

3. **Booking Service** (Port 8081)
   - Handles ticket booking requests
   - Validates customer and inventory
   - Publishes booking events to Kafka
   - Synchronous communication with Inventory Service

4. **Order Service** (Port 8082)
   - Processes booking events asynchronously
   - Creates order records
   - Updates inventory after successful orders
   - Event-driven architecture with Kafka

### Infrastructure Components

- **Keycloak** (Port 8091) - Identity and Access Management
- **MySQL** (Port 3306) - Primary database for all services
- **Apache Kafka** (Port 9092) - Event streaming platform
- **Kafka UI** (Port 8084) - Kafka cluster management
- **Zookeeper** (Port 2181) - Kafka coordination

## 🔄 Application Flow

Based on the architecture diagram, here's how a typical booking flows through the system:

1. **Client Authentication**: User authenticates via Keycloak through the API Gateway
2. **Booking Request**: Client sends booking request to `/api/v1/booking` (POST)
3. **Gateway Routing**: API Gateway validates JWT and routes to Booking Service
4. **Inventory Check**: Booking Service synchronously calls Inventory Service to check availability
5. **Event Publishing**: Booking Service publishes `BookingEvent` to Kafka topic
6. **Order Processing**: Order Service consumes the event asynchronously
7. **Inventory Update**: Order Service updates inventory capacity
8. **Database Persistence**: Order details are saved to MySQL

## 🛠️ Technologies Used

### Backend Framework
- **Spring Boot 3.5.4** - Main framework
- **Spring Cloud Gateway** - API Gateway implementation
- **Spring Security** - OAuth2 Resource Server
- **Spring Data JPA** - Data persistence
- **Spring Kafka** - Event streaming

### Security & Authentication
- **Keycloak 24.0.1** - OpenID Connect/OAuth2 provider
- **JWT** - Stateless authentication tokens
- **OAuth2 Resource Server** - Token validation

### Database & Persistence
- **MySQL 8.3.0** - Relational database
- **Flyway** - Database migration tool
- **Hibernate** - ORM implementation

### Event Streaming
- **Apache Kafka 7.5.0** - Event streaming platform
- **Kafka Schema Registry** - Schema management
- **Confluent Platform** - Kafka ecosystem

### Monitoring & Documentation
- **Spring Boot Actuator** - Health checks and metrics
- **Resilience4j** - Circuit breaker, retry, timeout
- **SpringDoc OpenAPI** - API documentation
- **Swagger UI** - Interactive API explorer

### Development Tools
- **Lombok** - Boilerplate code reduction
- **Maven** - Dependency management
- **Docker Compose** - Container orchestration

## 📊 Database Schema

### Inventory Service Tables
```sql
-- Venues table
CREATE TABLE venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    total_capacity BIGINT NOT NULL
);

-- Events table
CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    venue_id BIGINT NOT NULL,
    total_capacity BIGINT NOT NULL,
    left_capacity BIGINT NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
    FOREIGN KEY (venue_id) REFERENCES venue(id)
);
```

### Booking Service Tables
```sql
-- Customers table
CREATE TABLE customer (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255)
);
```

### Order Service Tables
```sql
-- Orders table
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total DECIMAL(10, 2),
    quantity BIGINT,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    customer_id BIGINT,
    event_id BIGINT
);
```

## 🔌 API Endpoints

### Inventory Service
- `GET /api/v1/inventory/venue/{venueId}` - Get venue information
- `GET /api/v1/inventory/event/{eventId}` - Get event details and availability
- `PUT /api/v1/inventory/event/{eventId}/capacity/{ticketCount}` - Update event capacity

### Booking Service
- `POST /api/v1/booking` - Create a new booking
  ```json
  {
    "userId": 1,
    "eventId": 1,
    "ticketCount": 2
  }
  ```

### API Gateway Routes
- All endpoints are accessible through the gateway at `http://localhost:8090`
- Authentication required via `Authorization: Bearer <JWT_TOKEN>`
- Swagger UI available at `http://localhost:8090/swagger-ui.html`

## 🔐 Security Implementation

### Authentication Flow
1. Client authenticates with Keycloak using username/password
2. Keycloak returns Access Token and ID Token
3. Client includes Access Token in `Authorization: Bearer` header
4. API Gateway validates JWT using Keycloak's public keys
5. Valid requests are forwarded to backend services

### Keycloak Configuration
- **Realm**: `ticketing-security-realm`
- **Client ID**: `ticketing-client-id`
- **Auth Server**: `http://localhost:8091`

### Security Features
- JWT token validation
- Public/private key verification
- Route-based access control
- Swagger endpoints exempted from authentication

## 📨 Event-Driven Architecture

### Kafka Topics
- **booking** - Booking events from Booking Service to Order Service

### Event Schema
```json
{
  "userId": 1,
  "eventId": 1,
  "ticketCount": 2,
  "totalPrice": 20.00
}
```

### Message Flow
1. Booking Service publishes `BookingEvent` to `booking` topic
2. Order Service consumes events with consumer group `order-service`
3. Order Service processes events asynchronously
4. Inventory is updated after successful order creation

## 🔄 Circuit Breaker Pattern

Implemented using Resilience4j for fault tolerance:

### Configuration
- **Failure Rate Threshold**: 50%
- **Sliding Window Size**: 8 calls
- **Wait Duration**: 5 seconds
- **Half-open Calls**: 2

### Fallback Mechanism
- Circuit breaker protects Booking Service calls
- Fallback returns "Booking service is down" message
- Automatic recovery when service becomes healthy

## 🚀 How to Run the Application

### Prerequisites
- **Java 24** (or compatible version)
- **Docker & Docker Compose**
- **Maven 3.6+**
- **Git**

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd java-microservices
```

### Step 2: Start Infrastructure Services
```bash
cd inventoryservice
docker compose up -d
```

This will start:
- MySQL database
- Keycloak authentication server
- Kafka broker with Zookeeper
- Kafka UI and Schema Registry

### Step 3: Verify Infrastructure
Wait for all containers to be healthy (30-60 seconds), then verify:

```bash
# Check containers
docker ps

# Verify services
curl http://localhost:8091  # Keycloak (should return HTML)
curl http://localhost:8084  # Kafka UI
```

### Step 4: Start Microservices

**Terminal 1 - Inventory Service:**
```bash
cd inventoryservice
./mvnw spring-boot:run
```

**Terminal 2 - Booking Service:**
```bash
cd bookingservice  
./mvnw spring-boot:run
```

**Terminal 3 - Order Service:**
```bash
cd orderservice
./mvnw spring-boot:run
```

**Terminal 4 - API Gateway:**
```bash
cd apigateway
./mvnw spring-boot:run
```

### Step 5: Setup Keycloak (First Time Only)

1. **Access Keycloak Admin Console:**
   - URL: http://localhost:8091
   - Username: `admin`
   - Password: `admin`

2. **Create Realm:**
   - Create new realm: `ticketing-security-realm`

3. **Create Client:**
   - Client ID: `ticketing-client-id`
   - Client Type: `Public` (for testing)
   - Enable "Direct access grants"

4. **Create Test User:**
   - Username: `testuser`
   - Set password (disable temporary)
   - Save user

### Step 6: Test the Application

**Option 1: Using Swagger UI**
1. Open http://localhost:8090/swagger-ui.html
2. Use the authentication section to get a token
3. Test the APIs interactively

**Option 2: Using cURL**
```bash
# Get access token
TOKEN=$(curl -X POST http://localhost:8091/realms/ticketing-security-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=ticketing-client-id&username=testuser&password=yourpassword" \
  | jq -r '.access_token')

# Test booking endpoint
curl -X POST http://localhost:8090/api/v1/booking \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"eventId":1,"ticketCount":1}'
```

### Step 7: Monitor the System

**Health Checks:**
- API Gateway: http://localhost:8090/actuator/health
- Services: http://localhost:808{0,1,2}/actuator/health

**Kafka UI:**
- http://localhost:8084 - Monitor topics and messages

**Database:**
- Connect to MySQL on `localhost:3306`
- Database: `ticketing`
- User: `root`, Password: `password`

## 🔍 Troubleshooting

### Common Issues

**Kafka Connection Errors:**
```bash
# Restart Kafka services
cd inventoryservice
docker compose restart kafka-broker zookeeper
```

**Authentication 401 Errors:**
- Verify Keycloak is running on port 8091
- Check if client configuration matches
- Ensure "Direct access grants" is enabled
- Use Access Token, not ID Token

**Database Connection Issues:**
- Wait for MySQL container to be fully started
- Check if port 3306 is available
- Verify database credentials in application.properties

**Service Discovery Issues:**
- Ensure all services are running on correct ports
- Check for port conflicts
- Verify application.properties configurations

### Logs and Monitoring
```bash
# View service logs
./mvnw spring-boot:run --debug

# View container logs
docker compose logs -f mysql kafka-broker

# Check Kafka topics
docker exec kafka-broker kafka-topics --list --bootstrap-server localhost:29092
```

## 🎯 Testing Scenarios

1. **Happy Path:** Book tickets with valid user and sufficient inventory
2. **Insufficient Inventory:** Try booking more tickets than available
3. **Invalid User:** Attempt booking with non-existent customer
4. **Circuit Breaker:** Stop Booking Service and test fallback
5. **Authentication:** Test with invalid/expired tokens
---

This ticketing system demonstrates modern microservices patterns and provides a solid foundation for scalable, distributed applications.
