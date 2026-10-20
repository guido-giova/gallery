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
|    PORT     | Determines the port of the applicaiton       |  8080   |
| DB_USERNAME | The user that can interact with the database |         |
| DB_PASSWORD | The password of the user                     |         |

## Item API

The item API is available under `/api/items`:

| Method | Path              | Description                |
|:------:|-------------------|----------------------------|
|  GET   | `/api/items`      | List items                 |
|  GET   | `/api/items/{id}` | Get one item               |
|  POST  | `/api/items`      | Create an item             |
|  PUT   | `/api/items/{id}` | Replace an item's metadata |
| DELETE | `/api/items/{id}` | Delete an item             |

Create and update requests use the JSON fields from the `Item` record. The ID in an
update URL is authoritative; the ID in its JSON body is ignored.

## Tests

The repository integration tests start a temporary PostgreSQL database in Docker and run the Flyway migrations against
it. Start Docker Desktop, then run:

```powershell
.\mvnw.cmd test
```

The tests use their own temporary database and do not modify your configured gallery database.
