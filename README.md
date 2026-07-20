# Professional Networking Platform Backend - Spring Boot Microservices

A LinkedIn-inspired backend built with Spring Boot and Java 21. The project uses independently deployable services behind an API Gateway, service discovery through Eureka, synchronous calls through OpenFeign, and Kafka events for cross-service workflows.

## Architecture

```text
Client
  |
  v
API Gateway (JWT validation and routing)
  |-------------------|-----------------------|
  v                   v                       v
User Service      Posts Service       Connections Service
  |                   |                       |
  |                   |----> Uploader Service  |
  |                   |       (Cloudinary)      |
  |                   v                         |
  |                Kafka events <---------------|
  |                   |
  v                   v
PostgreSQL      Notification Service
                    |
                    v
                PostgreSQL

Connections Service uses Neo4j for its social graph.
All services register with Eureka Discovery Server.
```

## Services

| Service | Purpose | Storage / integrations |
|---|---|---|
| `discoverServer` | Eureka server for service registration and discovery. | Eureka |
| `ApiGateway` | Single entry point; routes requests, validates JWT bearer tokens on protected routes, and forwards `X-User-Id`. | Spring Cloud Gateway, Eureka |
| `userService` | User signup and login; hashes passwords and generates JWT access tokens. Publishes user-created events. | PostgreSQL, Kafka |
| `connectionsService` | Connection requests, acceptance/rejection, and first-degree connection lookups. Consumes user-created events. | Neo4j, Kafka |
| `postsService` | Creates posts with an image, reads posts, and likes/unlikes posts. | PostgreSQL, Kafka, OpenFeign |
| `uploader-service` | Uploads media files to Cloudinary and returns a secure URL. | Cloudinary |
| `notification-service` | Creates notifications when connected users create or like posts. | PostgreSQL, Kafka |

## Event flow

```text
User signup
  -> user_created_topic
  -> connectionsService creates the matching graph person

Post creation
  -> postsService uploads image through uploader-service
  -> post_created_topic
  -> notification-service notifies first-degree connections

Post like
  -> post_liked_topic
  -> notification-service notifies the post owner
```

## Technology

- Java 21
- Spring Boot 4.1.0
- Spring Cloud 2025.1.2
- Spring Cloud Gateway (WebFlux)
- Netflix Eureka
- OpenFeign
- Apache Kafka
- PostgreSQL with Spring Data JPA
- Neo4j
- Cloudinary
- JJWT
- Maven Wrapper and Jib

## Prerequisites

- JDK 21
- Maven is optional; each service includes Maven Wrapper (`mvnw` / `mvnw.cmd`)
- PostgreSQL instances/databases for `userService`, `postsService`, and `notification-service`
- Neo4j for `connectionsService`
- Apache Kafka
- A Cloudinary account for `uploader-service`
- Docker Hub credentials only if you build/push images with Jib

## Configuration

Each service contains a `.env` file with the required variable names. Do not commit real credentials. Configure these as environment variables in your IDE, shell, or deployment environment.

| Service | Required variables |
|---|---|
| `discoverServer` | `SERVER_PORT` |
| `ApiGateway` | `SERVER_PORT`, `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE`, `JWT_SECRET_KEY` |
| `userService` | `SERVER_PORT`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET_KEY`, `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| `postsService` | `SERVER_PORT`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| `connectionsService` | `SERVER_PORT`, `SPRING_NEO4J_URL`, `SPRING_NEO4J_AUTHENTICATION_USERNAME`, `SPRING_NEO4J_AUTHENTICATION_PASSWORD`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_KAFKA_CONSUMER_PROPERTIES_SPRING_JSON_TRUSTED_PACKAGES` |
| `uploader-service` | `SERVER_PORT`, `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE`, `cloudinary_cloud_name`, `cloudinary_api_key`, `cloudinary_api_secret` |
| `notification-service` | `SERVER_PORT`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_KAFKA_CONSUMER_PROPERTIES_SPRING_JSON_TRUSTED_PACKAGES` |

Example values for local development:

```properties
EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE=http://localhost:8761/eureka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_NEO4J_URL=bolt://localhost:7687
```

Use the same `JWT_SECRET_KEY` in `userService` and `ApiGateway`, because the user service signs the token and the gateway verifies it.

## Run locally

Start supporting infrastructure first: PostgreSQL, Neo4j, Kafka, and Cloudinary credentials.

Then start services in this order:

1. `discoverServer`
2. `userService`
3. `connectionsService`
4. `uploader-service`
5. `postsService`
6. `notification-service`
7. `ApiGateway`

From any service directory in PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Open Eureka at the port configured by `discoverServer` to confirm registrations.

## API Gateway routes

All client-facing requests should go through the API Gateway. Route paths below assume the gateway is running at `http://localhost:<gateway-port>`.

| Gateway prefix | Target service | Authentication |
|---|---|---|
| `/api/v1/users/**` | `USER-SERVICE` | Not currently protected by the gateway filter |
| `/api/v1/posts/**` | `POSTS-SERVICE` | Bearer JWT required |
| `/api/v1/connections/**` | `CONNECTIONS-SERVICE` | Bearer JWT required |
| `/api/v1/uploads/**` | `UPLOADER-SERVICE` | Bearer JWT required |

The gateway strips `/api/v1` before forwarding. For protected requests send:

```http
Authorization: Bearer <access-token>
```

The gateway validates the token and forwards the user ID internally through `X-User-Id`. Internal services should not be publicly exposed; otherwise callers could forge that header.

## API reference

### Authentication

| Method | Gateway path | Body |
|---|---|---|
| `POST` | `/api/v1/users/auth/signup` | `{"name":"...","email":"...","password":"..."}` |
| `POST` | `/api/v1/users/auth/login` | `{"email":"...","password":"..."}` |

Login returns a JWT access token. The current token lifetime is 100 minutes.

### Connections

| Method | Gateway path | Description |
|---|---|---|
| `GET` | `/api/v1/connections/core/{userId}/first-degree` | Get a user's first-degree connections. |
| `POST` | `/api/v1/connections/core/request/{userId}` | Send a connection request. |
| `POST` | `/api/v1/connections/core/accept/{userId}` | Accept a request from a user. |
| `POST` | `/api/v1/connections/core/reject/{userId}` | Reject a request from a user. |

### Posts

| Method | Gateway path | Description |
|---|---|---|
| `POST` | `/api/v1/posts/core` | Create a post with an image. |
| `GET` | `/api/v1/posts/core/{postId}` | Get a post. |
| `GET` | `/api/v1/posts/core/users/{userId}/allPosts` | List a user's posts. |
| `POST` | `/api/v1/posts/likes/{postId}` | Like a post. |
| `DELETE` | `/api/v1/posts/likes/{postId}` | Remove a like. |

Create a post using `multipart/form-data`:

```text
post           application/json: {"content":"Post with image"}
multipartFile  file: image.png
```

In a web frontend, create the JSON part with a Blob:

```javascript
const formData = new FormData();

formData.append(
  "post",
  new Blob([JSON.stringify({ content: "Post with image" })], {
    type: "application/json"
  })
);
formData.append("multipartFile", imageFile);

await fetch("http://localhost:<gateway-port>/api/v1/posts/core", {
  method: "POST",
  headers: { Authorization: `Bearer ${token}` },
  body: formData
});
```

Do not manually set the request `Content-Type`; the browser adds the multipart boundary.

### Media upload

| Method | Gateway path | Body |
|---|---|---|
| `POST` | `/api/v1/uploads/file` | `multipart/form-data` with a `multipartFile` file part |

The posts service normally calls the uploader service internally through OpenFeign.

## Build and container images

Build a service:

```powershell
.\mvnw.cmd clean package
```

Jib is bound to the Maven `package` phase and is configured to publish images to Docker Hub under:

```text
docker.io/bhanu8505/networking-application-<service-name>:<version>
```

Log in to Docker Hub before a build that pushes images:

```powershell
docker login
```

Docker image names must be lowercase.

## Current scope and next improvements

- Add Spring Security at the gateway for centralized authentication and role-based authorization.
- Use short-lived access tokens with refresh tokens and token revocation for immediate logout.
- Restrict direct access to internal microservices.
- Add input validation, file type/size validation, API tests, and OpenAPI documentation.
- Add Docker Compose or Kubernetes manifests for local infrastructure and deployment.
