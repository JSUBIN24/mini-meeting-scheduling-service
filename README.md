
# Mini Doodle – Meeting Scheduling Platform

This project is a high-performance simulation of a meeting scheduling platform built with **Spring Boot** and **Java**.  
It enables users to manage their **time slots**, **schedule meetings**, and **view calendar availability**.

The goal is to provide a simple but scalable backend service resembling Doodle, where users can book slots and manage their calendars.
## Functionalities to implement:
### Time slot management
- allow users to create available time slots with configurable duration in calendars, delete or modify existing time slots, and mark time slots as busy or free according to their availability. 
### Meeting scheduling:
- enable users to convert available slots into meetings, add meeting details such as title, description, and participants.
- Assume the platform may be used by hundreds of users with thousands of slots. Strive to design your solution according to that.

---

## Features

- **User Management**
    - Each user has a personal calendar.
    - Calendar exists only in the domain model (not exposed as an entity to clients).

- **Time Slot Management**
    - Create available slots with configurable duration.
    - Modify or delete existing slots.
    - Mark slots as `FREE` or `BUSY`.
    - Prevent overlapping slots.

- **Meeting Scheduling**
    - Convert a `FREE` slot into a meeting.
    - Add meeting details: title, description, and participants.
    - Participants are linked to users in the system.

- **Availability Queries**
    - Query free/busy slots for a given user within a time range.
    - View aggregated availability.

- **Persistence**
    - Data stored in a relational database (PostgreSQL by default).
    - JPA + Hibernate for persistence layer.

- **Tests**
    - Unit and integration tests implemented with JUnit 5 + Spring Boot Test.

- **Metrics & Logging**
    - Exposes actuator endpoints for health and metrics.
    - Structured logging with SLF4J + Logback.

---

## Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL**
- **Docker & Docker Compose**
- **JUnit 5, Mockito**
- **Lombok**
- **Spring Boot Actuator**

---

## Running the Application

### Prerequisites
- Java 21
- PostgresSQL
- Docker & Docker Compose installed.
- Port `8080` available (or change in `application.yml`).

### Start with Docker Compose

## Step 1

```bash
  ./mvnw clean install
```

## Step 2
```bash
docker-compose up --build
```

This will start:
- `mini-doodle-service` (Spring Boot app)
- `postgres` (database)

### Stop the Application
```bash
docker-compose down -v
```

---

## API Endpoints

### User APIs
- `POST /api/users` → Create new user.
- `GET /api/users/{id}` → Fetch user details.

### Slot APIs
- `POST /api/users/{userId}/slots` → Create a slot.
- `GET /api/users/{userId}/slots` → Query slots.
- `PATCH /api/users/{userId}/slots/{slotId}` → Modify slot times.
- `PATCH /api/users/{userId}/slots/{slotId}/status?status=BUSY` → Update slot status.
- `DELETE /api/users/{userId}/slots/{slotId}` → Delete a slot.

### Meeting APIs
- `POST /api/users/{userId}/meetings` → Schedule a meeting from a slot.
- `GET /api/users/{userId}/meetings` → List meetings.

### Availability APIs
- `GET /api/users/{userId}/availability?from=...&to=...` → Get free/busy overview.

---

## Here are some example API calls to interact with the Mini Doodle service:


### Create User
```POST
curl -X POST "http://localhost:8080/api/users?email=subin@example.com&name=Subin"
````

### Create Slot
```http
POST /api/users/1/slots
Content-Type: application/json

{
  "startTime": "2025-08-18T10:00:00Z",
  "durationMinutes": 60
}
```

Response:
```json
{
  "id": 101,
  "startTime": "2025-08-18T10:00:00Z",
  "endTime": "2025-08-18T11:00:00Z",
  "status": "FREE"
}

```

### Schedule meeting
```POST
curl -X POST "http://localhost:8080/api/users/1/meetings" \
  -H "Content-Type: application/json" \
  -d '{
        "slotId": 5,
        "title": "Design Review",
        "description": "Reviewing slot service improvements",
        "participantUserIds": [2,3]
      }'
```

### Query Free/Busy Availability
```GET
curl "http://localhost:8080/api/users/1/availability/freebusy?from=2025-08-20T00:00:00Z&to=2025-08-20T23:59:59Z"

```



## Metrics & Health Check

- `GET /actuator/health`
- `GET /actuator/metrics`

---

## Tests

Run all tests:
```bash
./mvnw test
```

---

## Design Decisions

- **Domain-Driven**: Calendar exists only in the domain, not as a top-level resource.
- **Optimistic Locking**: Prevents concurrent slot modifications (via `@Version` field).
- **Validation**: Bean Validation (Jakarta Validation) ensures request correctness.
- **Scalability**: Slot queries indexed (`calendar_id,startTime`) for performance.
- **Extensibility**: Easy to extend with group calendars or recurring events.

---

## Next Steps (Future Improvements)

- Authentication & authorization with Spring Security.
- Notifications for meeting participants.
- GraphQL API for flexible queries.
- Improve Granularity to Fetch user availability.

---



