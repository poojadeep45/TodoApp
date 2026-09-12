# Todo Application

A full-stack todo list manager built with Spring Boot, Thymeleaf, and MySQL — with user accounts, a REST API, and a Docker setup for easy deployment.

## Features

- **Task management** — create, edit, complete, and delete tasks
- **Priorities** — Low / Medium / High, shown as color-coded badges
- **Due dates** — with automatic overdue highlighting
- **Categories** — free-text tags (e.g. "Work", "Personal") with autocomplete suggestions
- **Search & filter** — by keyword, priority, status, and category, all combinable
- **Bulk actions** — mark all (filtered) tasks complete, or clear all completed tasks at once
- **Progress tracking** — completion percentage and a progress bar
- **Due-date reminders** — an in-app banner surfaces overdue, due-today, and due-soon tasks
- **User accounts** — registration and login, with every task scoped to its owner
- **REST API** — full CRUD under `/api/tasks`, secured separately from the web UI
- **Dockerized** — one command spins up the app and a MySQL database together

## Tech stack

| Layer          | Technology                          |
|----------------|--------------------------------------|
| Language       | Java 17                              |
| Framework      | Spring Boot 4.1.1                    |
| Web            | Spring MVC + Thymeleaf               |
| Persistence    | Spring Data JPA (Hibernate) + MySQL  |
| Security       | Spring Security (form login + HTTP Basic) |
| Validation     | Jakarta Bean Validation               |
| Build          | Maven                                |
| Frontend       | Bootstrap 5, Bootstrap Icons          |
| Containerization | Docker + Docker Compose             |

## Getting started

### Option A — Run with Docker (recommended)

Requires Docker and Docker Compose. No local Java, Maven, or MySQL installation needed.

```bash
docker compose up --build
```

This starts a MySQL container and the app together, waiting for the database to be ready before the app boots. Once it's up:

1. Visit `http://localhost:8080/register` and create an account.
2. Log in and start adding tasks at `http://localhost:8080/tasks`.

Your data persists in a Docker volume across restarts. To stop everything:

```bash
docker compose down          # stops containers, keeps your data
docker compose down -v       # stops containers AND deletes the database volume
```

### Option B — Run locally

**Prerequisites:**
- Java 17 (JDK)
- Maven (or use the included `mvnw` wrapper)
- A running MySQL 8 instance

**Setup:**

1. Create a database:
   ```sql
   CREATE DATABASE `todo-app`;
   ```
2. Configure your database credentials in `src/main/resources/application.properties`, or override them via environment variables:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/todo-app
   export SPRING_DATASOURCE_USERNAME=root
   export SPRING_DATASOURCE_PASSWORD=your_password
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Visit `http://localhost:8080/register` to create an account.

Tables are created and updated automatically (`spring.jpa.hibernate.ddl-auto=update`) — no manual schema setup required.

## Account requirements

- Username: 3–50 characters, must be unique
- Email: must be a valid, unique email address
- Password: at least 15 characters, including an uppercase letter, a lowercase letter, a number, and a special character

## REST API

The API is available under `/api/tasks` and is secured with **HTTP Basic authentication**, separate from the browser's session-based login — use the same username/password you registered with.

| Method | Endpoint                  | Description                          |
|--------|----------------------------|---------------------------------------|
| GET    | `/api/tasks`               | List tasks (supports `keyword`, `priority`, `completed`, `category` query params) |
| GET    | `/api/tasks/{id}`          | Get a single task                     |
| POST   | `/api/tasks`                | Create a task                         |
| PUT    | `/api/tasks/{id}`          | Update a task                         |
| PATCH  | `/api/tasks/{id}/toggle`   | Toggle a task's completed status      |
| DELETE | `/api/tasks/{id}`          | Delete a task                         |

**Example — list your tasks:**
```bash
curl -u your_username:your_password http://localhost:8080/api/tasks
```

**Example — create a task:**
```bash
curl -u your_username:your_password \
     -X POST http://localhost:8080/api/tasks \
     -H "Content-Type: application/json" \
     -d '{"title": "Buy groceries", "priority": "HIGH", "dueDate": "2026-09-20", "category": "Personal"}'
```

Every API request is scoped to the authenticated user — you can only ever see or modify your own tasks, even if you guess another task's ID.

## Project structure

```
src/main/java/com/app/todoapp/
├── api/                # REST API controllers, request/response DTOs, error handling
├── controller/         # Web (Thymeleaf) controllers: tasks, login, registration
├── entities/            # JPA entities: Task, User, Priority
├── exception/           # Custom exceptions
├── repository/          # Spring Data JPA repositories
├── security/             # Spring Security config, UserDetails implementation, registration DTO
└── service/              # Business logic

src/main/resources/
├── templates/            # Thymeleaf HTML pages
└── application.properties
```

## Security notes

- Passwords are hashed with BCrypt — never stored in plain text.
- Two separate security configurations: session/cookie-based form login for the web pages, and stateless HTTP Basic auth for the REST API.
- CSRF protection is enabled for the web UI (Thymeleaf forms include the token automatically) and disabled for the stateless API, where it isn't applicable.
- Task lookups are always scoped by owner at the database query level, so one user can never read, edit, or delete another user's task — even by guessing its numeric ID.

## License

Add a license of your choice here (e.g. MIT) if you plan to make this repository public.