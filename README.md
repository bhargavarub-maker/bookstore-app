# Bookstore API (Java + Spring Boot + database)

A small, working version of the bookstore from the Week 0 notes. It has two REST APIs,
**Books** and **Orders**, a database, interactive **Swagger** documentation, and a simple
web app (the "Frontend") for browsing books, managing the catalog and placing orders.

![Bookstore system design](docs/images/system-design.png)

| Box in the diagram | Where it lives in this project |
|---|---|
| Frontend (web app) | `src/main/resources/static/` (HTML, CSS and JavaScript pages), served at http://localhost:8080/ |
| Books service (REST API) | `src/main/java/com/vone8/bookstore/books/` at `/v1/books` |
| Orders service (REST API) | `src/main/java/com/vone8/bookstore/orders/` at `/v1/orders` |
| Books DB / Orders DB | The `books`, `orders` and `order_items` tables (H2 by default, PostgreSQL or MySQL optional) |

To keep things simple both services run inside **one** application and share one database.
In a real company they would usually be two separate applications with their own databases.
Notice that an order stores only the book's id, title and price, not a database link to the
`books` table: that is how it would work if the two services were separate.

---

## 1. Install what you need

You need only **one** thing: a **Java Development Kit (JDK), version 17 or newer** (21 recommended).

You do **not** need to install Maven. The project includes the *Maven Wrapper* (`mvnw` /
`mvnw.cmd`), which downloads the right Maven version the first time you run it.

If you already followed the training's Developer Tools install guide
(https://claude.ai/code/artifact/d05117a3-881c-4c25-9d36-947a1a443d26), you already have
Postman, curl, Git and VS Code. Add the JDK as follows.

### Windows

Open **PowerShell** and run:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Or download the `.msi` installer from https://adoptium.net (choose JDK 21, Windows x64) and,
during setup, turn on **"Set JAVA_HOME variable"**. Close and reopen PowerShell afterwards.

### macOS

```bash
brew install --cask temurin@21
```

No Homebrew? Download the `.pkg` installer from https://adoptium.net (choose macOS, aarch64
for Apple Silicon M1/M2/M3/M4, x64 for Intel).

### Linux (Ubuntu / Debian)

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
```

### Check that it worked

```bash
java -version
```

You should see something like `openjdk version "21.0.x"`. Any version 17 or higher is fine.

### Optional tools

- **An IDE**: IntelliJ IDEA Community Edition, or VS Code with the "Extension Pack for Java".
  You can open the project folder directly; both understand Maven projects.
- **Docker Desktop**: only if you want to try PostgreSQL or MySQL instead of the built-in database.

---

## 2. Run the application

1. Unzip `bookstore-app.zip` somewhere easy, for example your Desktop.
2. Open a terminal **in the `bookstore-app` folder** (the one that contains `pom.xml`).
3. Start the app:

   **macOS / Linux**
   ```bash
   ./mvnw spring-boot:run
   ```
   **Windows (PowerShell or Command Prompt)**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

The first run downloads Maven and the libraries, which can take a few minutes. It is ready when
you see a line ending in:

```
Started BookstoreApplication in 4.2 seconds
```

Leave this terminal open; the app runs until you press **Ctrl+C**.

### Open it in the browser

| What | URL |
|---|---|
| **Bookstore web app** | http://localhost:8080/ |
| **Swagger UI** (interactive API docs) | http://localhost:8080/swagger-ui.html |
| OpenAPI document (JSON) | http://localhost:8080/v3/api-docs |
| OpenAPI document (YAML) | http://localhost:8080/v3/api-docs.yaml |
| Database console | http://localhost:8080/h2-console |

The database starts with 8 sample books and 1 sample order. Because the default database
lives in memory, **everything resets when you restart the app**. That is handy for practice.

### Sign in: the API uses HTTP Basic authentication

Every call to `/v1/...` needs a username and password, sent as an HTTP Basic
`Authorization` header. Without it the API answers **401 Unauthorized**.

| | Default | Change it with |
|---|---|---|
| Username | `student` | environment variable `BOOKSTORE_USER` |
| Password | `bookstore123` | environment variable `BOOKSTORE_PASSWORD` |

The defaults live in `application.properties` (`spring.security.user.*`), and the rules are in
`SecurityConfig.java`: `/v1/**` needs a login, while the web pages, Swagger UI and the H2
console stay open. **Always set your own password before you deploy the app anywhere public**,
for example on Cloud Run:

```bash
gcloud run deploy bookstore --source . --set-env-vars BOOKSTORE_USER=student,BOOKSTORE_PASSWORD=<a-long-random-password>
```

How Basic auth works: the client joins `username:password`, encodes it in Base64 and sends
`Authorization: Basic c3R1ZGVudDpib29rc3RvcmUxMjM=`. Base64 is **not** encryption, so only
use Basic auth over HTTPS (Cloud Run and Apigee always use HTTPS).

### Use the web app

Open http://localhost:8080/ and sign in. The web app is plain HTML, CSS and JavaScript
(no framework, no build step), and it only talks to the REST API, never to the database.

![Shop page](docs/images/frontend.png)

| Page | What you can do | API calls it makes |
|---|---|---|
| **Sign in** (`login.html`) | Checks your username and password | `GET /v1/books?limit=1` |
| **Shop** (`index.html`) | Browse books, filter by genre and author | `GET /v1/books?genre=&author=&limit=` |
| | **Add to cart** (the cart is kept in the browser) | none |
| **Cart** (🛒 button, every page) | Change quantities, enter name and email, **Place order** | `POST /v1/orders` |
| **Orders** (`orders.html`) | See all orders or one customer's orders, filter by status | `GET /v1/orders?customerEmail=` |
| | **Cancel order** (its books go back in stock) | `POST /v1/orders/{id}/cancel` |
| **Manage books** (`books.html`) | **Add**, **Edit** and **Delete** books | `POST`, `PUT`, `DELETE /v1/books/{id}` |

![Orders page](docs/images/frontend-orders.png)
![Cart](docs/images/frontend-cart.png)
![Manage books page](docs/images/frontend-books.png)

The **API calls** button (bottom right) lists every request the app sent, with its status
code; open *headers and bodies* to see the JSON going each way. API errors appear on the page
so you can watch them happen: sign in with a wrong password for a **401**, save a book
without a title for a **400**, or order more copies than are in stock for a **409**. Your
browser's developer tools (F12, *Network* tab) show the same calls, including the
`Authorization` header.

| File | What it does |
|---|---|
| `common.js` | Shared code: `api()` adds the `Authorization` header to every call, plus the top bar, cart and API calls panel |
| `login.html` / `login.js` | Sign-in page |
| `index.html` / `shop.js` | Shop page |
| `orders.html` / `orders.js` | Orders page |
| `books.html` / `books.js` | Manage books page |
| `style.css` | All the styling |

All API URLs are relative (`const API_BASE = 'v1'` in `common.js`), so the same files work on
`localhost`, on Cloud Run, and behind an Apigee proxy that serves the whole app under a base
path. Change `API_BASE` to a full URL only if the pages and the API are on different hosts.

---

## 3. Swagger documentation

Swagger UI is generated automatically from the Java code (the `@Operation`, `@ApiResponse`
and `@Schema` annotations in the controllers and models), so it is always in sync with the API.

![Swagger UI](docs/images/swagger-ui.png)

**Log in first:** click **Authorize** (top right), enter `student` / `bookstore123`, and click
**Authorize**. Swagger then sends the `Authorization` header with every request.

**Try it out:** click an endpoint, for example **GET /v1/books/{id}**, then **Try it out**,
enter `1` for `id`, and click **Execute**. Swagger sends a real request to your running app
and shows the curl command, the response body, and the status code.

![Try it out](docs/images/swagger-try-it-out.png)

A copy of the OpenAPI file is also saved in [`docs/openapi.yaml`](docs/openapi.yaml). You can:

- open it in the Swagger Editor (https://editor.swagger.io) via **File > Import file**;
- import it into Postman via **Import** to get a ready-made collection of all requests;
- use it later in the course to create an Apigee proxy and to publish the API on a developer portal.

---

## 4. The API at a glance

All request and response bodies are JSON.

### Books

| Method | Path | What it does | Success | Errors |
|---|---|---|---|---|
| GET | `/v1/books` | List books. Optional `?genre=scifi`, `?author=weir`, `?limit=5` | 200 | 400 bad `limit` |
| GET | `/v1/books/{id}` | Get one book | 200 | 404 |
| POST | `/v1/books` | Add a book | 201 + `Location` header | 400 |
| PUT | `/v1/books/{id}` | Replace a book's details | 200 | 400, 404 |
| DELETE | `/v1/books/{id}` | Delete a book | 204 | 404 |

### Orders

| Method | Path | What it does | Success | Errors |
|---|---|---|---|---|
| GET | `/v1/orders` | List orders. Optional `?customerEmail=asha@vone8.com` | 200 | |
| GET | `/v1/orders/{id}` | Get one order | 200 | 404 |
| POST | `/v1/orders` | Place an order (reduces stock) | 201 | 400, 404 unknown book, 409 not enough stock |
| POST | `/v1/orders/{id}/cancel` | Cancel an order (puts stock back) | 200 | 404, 409 already cancelled |

Every error has the same shape:

```json
{
  "timestamp": "2026-10-14T02:23:23.585Z",
  "status": 404,
  "error": "Not Found",
  "message": "Book 99 not found",
  "path": "/v1/books/99"
}
```

### Try it with curl

Open a **second** terminal (the first one is running the app).

`-u username:password` makes curl send the Basic `Authorization` header.

```bash
# No password -> 401 Unauthorized
curl -i http://localhost:8080/v1/books

# List all books
curl -u student:bookstore123 http://localhost:8080/v1/books

# Only science fiction, at most 5
curl -u student:bookstore123 "http://localhost:8080/v1/books?genre=scifi&limit=5"

# One book
curl -u student:bookstore123 http://localhost:8080/v1/books/1

# A book that does not exist -> 404 (-i shows the status line and headers)
curl -i -u student:bookstore123 http://localhost:8080/v1/books/99

# Add a book -> 201 Created
curl -i -u student:bookstore123 -X POST http://localhost:8080/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Refactoring","author":"Martin Fowler","genre":"programming","price":41.00,"stock":3}'

# Place an order for 2 copies of book 3
curl -u student:bookstore123 -X POST http://localhost:8080/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Ravi","customerEmail":"ravi@vone8.com","items":[{"bookId":3,"quantity":2}]}'

# Ask for more copies than exist -> 409 Conflict
curl -i -u student:bookstore123 -X POST http://localhost:8080/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Ravi","customerEmail":"ravi@vone8.com","items":[{"bookId":8,"quantity":500}]}'

# Cancel order 1
curl -u student:bookstore123 -X POST http://localhost:8080/v1/orders/1/cancel

# The same login written as a header by hand (Base64 of "student:bookstore123")
curl -H "Authorization: Basic c3R1ZGVudDpib29rc3RvcmUxMjM=" http://localhost:8080/v1/books/1
```

**Windows PowerShell note:** type `curl.exe` instead of `curl`, write each command on one line
(remove the `\` line breaks), and escape the JSON quotes, for example:

```powershell
curl.exe -u student:bookstore123 -X POST http://localhost:8080/v1/books -H "Content-Type: application/json" -d '{\"title\":\"Refactoring\",\"author\":\"Martin Fowler\",\"price\":41.00,\"stock\":3}'
```

In Postman, open the **Authorization** tab, pick **Basic Auth** and enter the username and password.
Swagger UI or Postman are easier on Windows.

---

## 5. Look inside the database

Open http://localhost:8080/h2-console and log in with:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:bookstore` |
| User Name | `sa` |
| Password | *(leave empty)* |

Then run SQL such as `SELECT * FROM BOOKS;` or `SELECT * FROM ORDERS;`. Place an order through
the API and run the query again to watch the `STOCK` column change.

---

## 6. Optional: use PostgreSQL or MySQL

The diagram uses PostgreSQL. The app can switch databases with a **Spring profile** and no code
changes, because JPA generates the SQL for whichever database it is connected to.

1. Start a database. With Docker Desktop running, from this folder:
   ```bash
   docker compose up -d postgres      # or: docker compose up -d mysql
   ```
   This creates a database called `bookstore` with user `bookstore` and password `bookstore`.
   (If you installed PostgreSQL or MySQL yourself, create that database and user instead.)
2. Start the app with the matching profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres      # or: mysql
   ```
   On Windows PowerShell put the argument in quotes:
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
   ```

The tables are created automatically and the sample data is added on the first run. Unlike H2,
the data **stays** after a restart. To use a different server or password, set the environment
variables `DB_URL`, `DB_USER` and `DB_PASSWORD` (see `application-postgres.properties`).
Stop the database with `docker compose down`.

---

## 7. Run the tests and build a JAR

```bash
./mvnw test          # runs 11 automated API tests (Windows: .\mvnw.cmd test)
./mvnw package       # builds target/bookstore-1.0.0.jar
java -jar target/bookstore-1.0.0.jar
```

The JAR contains everything, including the web server, so `java -jar` is all you need to run it.

---

## 8. Project layout

```
bookstore-app/
├── pom.xml                         Maven build file: Java version and libraries
├── mvnw, mvnw.cmd, .mvn/           Maven Wrapper (no Maven install needed)
├── docker-compose.yml              Optional PostgreSQL / MySQL
├── docs/openapi.yaml               Exported OpenAPI 3.1 document
└── src/
    ├── main/java/com/vone8/bookstore/
    │   ├── BookstoreApplication.java   Starts the app
    │   ├── OpenApiConfig.java          Title/description and Authorize button in Swagger UI
    │   ├── SecurityConfig.java         HTTP Basic auth for /v1/**
    │   ├── DataSeeder.java             Sample books and order on first run
    │   ├── books/    Book (table), BookRequest (JSON in), BookRepository (DB),
    │   │             BookService (rules), BookController (HTTP endpoints)
    │   ├── orders/   Order, OrderItem, OrderRequest, OrderRepository,
    │   │             OrderService, OrderController
    │   └── common/   ApiError + GlobalExceptionHandler (consistent error JSON)
    ├── main/resources/
    │   ├── application.properties          Default settings (H2, login, Swagger paths)
    │   ├── application-postgres.properties  PostgreSQL profile
    │   ├── application-mysql.properties     MySQL profile
    │   └── static/                         Web app: login, shop, orders and manage-books pages
    └── test/java/.../BookstoreApiTests.java Automated tests
```

Each feature follows the same layers, which you will see in most Java backends:

```
HTTP request -> Controller -> Service -> Repository -> Database
```

- **Controller**: maps a URL and HTTP verb to a Java method, and turns objects into JSON.
- **Service**: business rules (for example "you can't order more copies than are in stock").
- **Repository**: reads and writes the database. Spring Data writes the SQL for you.
- **Entity** (`Book`, `Order`): a Java class that maps to a database table.

Technology: Java 17+, Spring Boot 4.1, Spring Data JPA (Hibernate), Bean Validation,
Spring Security (HTTP Basic), springdoc-openapi 3.1 (Swagger UI), H2 / PostgreSQL / MySQL, JUnit 5.

---

## 9. Troubleshooting

| Problem | Fix |
|---|---|
| `java: command not found` / `'java' is not recognized` | The JDK isn't installed or the terminal was opened before installing. Reopen the terminal; on Windows reinstall with "Set JAVA_HOME" turned on. |
| `JAVA_HOME is not set` / `JAVA_HOME ... invalid` | Point `JAVA_HOME` at the JDK folder, e.g. macOS `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`, Windows: *System Properties > Environment Variables*. |
| `./mvnw: Permission denied` (macOS/Linux) | Run `chmod +x mvnw` once. |
| `Port 8080 was already in use` | Stop the other program, or run on another port: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`. |
| Download errors on first run behind a company proxy | Ask IT for the proxy address and add it to `~/.m2/settings.xml`, or run from a network without the proxy once. |
| `release version 17 not supported` | Your JDK is older than 17. Install JDK 21 (step 1). |
| `401 Unauthorized` from the API | Send the username and password (`curl -u student:bookstore123 ...`, Swagger **Authorize**, Postman *Basic Auth*). On Cloud Run use the `BOOKSTORE_USER` / `BOOKSTORE_PASSWORD` you deployed with. |
| The web app looks old or has no Orders page | You are running an older copy. Stop the app, unzip the latest `bookstore-app.zip` into a fresh folder and start it again; on Cloud Run, deploy again. Then hard-refresh the browser (Ctrl+Shift+R / Cmd+Shift+R). |
| Postgres profile: `Connection refused` | The database isn't running. Check `docker compose ps`. |

---

## 10. How this connects to Apigee

In the Apigee weeks you will put an **API proxy** in front of this API, in the "API gateway"
position. The proxy adds API keys, quotas, caching and analytics without changing this Java code.
Apigee runs in Google Cloud, so it can't reach `localhost` on your laptop: to use this app as a
proxy target it has to be deployed somewhere public (for example Cloud Run), or you can keep
using `https://mocktarget.apigee.net` as the course does. The `docs/openapi.yaml` file can be
used to generate the proxy and to publish the API on the developer portal.

Because the backend now needs HTTP Basic auth, the Apigee proxy must send it too. Either let
clients pass their own `Authorization` header through the proxy unchanged, or (more common)
have the proxy check its own credentials, such as an API key, and add the backend's Basic
login with a **BasicAuthentication** policy (`Operation` = `Encode`) in the target request
flow, reading the username and password from a KVM or Secret Manager so they are never
stored in the proxy bundle.
