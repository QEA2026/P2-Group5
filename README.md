# Revature Expense Manager

The project contains a Python employee application and a Java/Javalin manager
application. Both applications use the SQLite database in the root `db` folder.

## Run the manager app with Docker

Docker Desktop must be installed and running.

From the repository root, build and start the manager container:

```bash
docker compose up --build manager-app
```

Open the manager application at <http://localhost:8080>.

The Compose configuration mounts the root `db` folder at `/app/db` inside the
container. This keeps the SQLite database outside the image and lets the Python
employee application use the same database later.

Stop the container with:

```bash
docker compose down
```
