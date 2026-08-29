# ZeroPing 💬

A real-time chat application built with Spring Boot and WebSocket (STOMP over SockJS). Users join with a name, chat in a shared public room, and see live join/leave notifications — with message history persisted in MySQL.

## Features

- Real-time messaging via WebSocket (STOMP + SockJS)
- Public chat room with live join/leave system notifications
- Persistent chat history (MySQL + JPA), loaded on join
- Auto-reconnect on dropped connection with live status indicator
- XSS-safe message rendering and input validation (no empty/blank messages)
- Simple, responsive vanilla JS/HTML/CSS frontend — no build step required

## Tech Stack

- **Backend:** Java 17, Spring Boot 4, Spring WebSocket, Spring Data JPA
- **Database:** MySQL
- **Frontend:** HTML, CSS, JavaScript, SockJS, Stomp.js
- **Build tool:** Maven

## Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- MySQL server running locally

## Setup

1. Create the database:
   ```sql
   CREATE DATABASE chatapp;
   ```
2. Configure credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
   spring.datasource.username=root
   spring.datasource.password=admin
   ```
   (Tables are created automatically via `hibernate.ddl-auto=update`.)
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Open **http://localhost:8080** in your browser.

## How It Works

- Client connects to `/chat` (SockJS) and subscribes to `/topic/public`.
- Sending a message publishes to `/app/sendMessage`, which is broadcast to all subscribers and saved to the DB.
- Joining publishes to `/app/join`, announcing the user and saving a `JOIN` system message; disconnecting triggers a `LEAVE` message.
- `GET /history` returns the last 50 chat messages, loaded when a user joins.

## Project Structure

```
src/main/java/com/org/archit/chatapp/
├── ChatAppApplication.java     # Entry point
├── config/WebSocketConfig.java # STOMP/SockJS endpoint & message broker setup
├── controller/ChatController.java # WebSocket message handlers + /history endpoint
├── model/ChatMessage.java      # JPA entity (sender, content, type, timestamp)
└── repository/ChatMessageRepository.java

src/main/resources/static/
├── index.html                  # Chat UI
└── style.css
```

## License

MIT
