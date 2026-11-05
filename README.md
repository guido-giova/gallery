# Gallery

Browse a private image and video collection locally.

## Plan

### Main Objectives

- Private, self-hosted media library for images and videos.
- No folders. Everything is organized by tags.
- Files can be grouped into ordered collections that represent a certain order to read/watch them.
- Files can have titles and descriptions in Markdown.

### Future

- Multiple users
- Docker
- AI tagging
- Image editing

## App information

### Stack

|    Name    | Version |
|:----------:|:-------:|
|    Java    |   25    |
| SpringBoot |  4.1.1  |

### Environment Variables

This is a table of the environment variables that the application uses.

|    Name     | Description                                  | Default |
|:-----------:|----------------------------------------------|:-------:|
|    PORT     | Determines the port of the application       |  8080   |
| DB_USERNAME | The user that can interact with the database |         |
| DB_PASSWORD | The password of the user                     |         |

## API

### Item

The item API is available under `/api/items`:

| Method | Path              | Description                | Implemented |
|:------:|-------------------|----------------------------|:-----------:|
|  GET   | `/api/items`      | List items                 |      N      |
|  GET   | `/api/items/{id}` | Get one item               |      N      |
|  POST  | `/api/items`      | Create an item             |      N      |
|  PUT   | `/api/items/{id}` | Replace an item's metadata |      N      |
| DELETE | `/api/items/{id}` | Delete an item             |      N      |

Create and update requests use the JSON fields from the `Item` record. The ID in an
update URL is authoritative; the ID in its JSON body is ignored.

## Instructions

### Run the Application

Make sure you have Java 25 installed.

Run the Spring Boot application using the Maven wrapper:

```bash
./mvnw spring-boot:run -e
```

On Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

The application will be available at [http://localhost:8080](http://localhost:8080) by default.
You can change the port by setting the `PORT` environment variable.

Make sure `DB_USERNAME` and `DB_PASSWORD` are configured if your database requires credentials.
