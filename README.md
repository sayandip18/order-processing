# Order Processing

## Prerequisites

- [Docker](https://www.docker.com/) (for the database)
- Java 21
- Maven (or use the included `mvnw` wrapper)

---

## Running the Project

### 1. Start the PostgreSQL database

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 container on port `5432`. Data is persisted in a Docker volume (`pgdata`), so it survives container restarts.

To stop it:

```bash
docker-compose down
```

### 2. Start the Spring Boot application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The app starts on `http://localhost:8080`. On first run, Hibernate automatically creates the database tables.

### 3. Seed item data

Orders reference items that must exist in the database. Connect to the running container and insert some sample items:

```bash
docker exec -it order-processing-db psql -U orderuser -d orderdb
```

Then run:

```sql
INSERT INTO items (name, price, stocked_qty) VALUES
  ('Widget A', 9.99, 100),
  ('Widget B', 24.99, 50),
  ('Widget C', 4.49, 200);
```

---

## API Endpoints

All endpoints are relative to `http://localhost:8080`.

### Authentication

#### Register

```
POST /api/auth/register
```

Request body:

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "secret123"
}
```

Response:

```json
{
  "token": "<jwt-token>"
}
```

#### Login

```
POST /api/auth/login
```

Request body:

```json
{
  "email": "jane@example.com",
  "password": "secret123"
}
```

Response:

```json
{
  "token": "<jwt-token>"
}
```

---

### Orders

All order endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

#### Create an order

```
POST /api/orders
```

Request body:

```json
{
  "items": [
    { "itemId": 1, "qty": 2 },
    { "itemId": 3, "qty": 5 }
  ]
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "price": 42.43,
  "createdAt": "2026-06-13T10:00:00",
  "items": [
    { "itemId": 1, "name": "Widget A", "qty": 2, "purchasePrice": 9.99 },
    { "itemId": 3, "name": "Widget C", "qty": 5, "purchasePrice": 4.49 }
  ]
}
```

#### Get all orders for the authenticated user

```
GET /api/orders
```

Response `200 OK`: array of order objects (same shape as above).

---

### Payments

All payment endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

#### Initiate a payment

```
POST /api/payments
```

Request body:

```json
{
  "orderId": 1,
  "amount": 42.43
}
```

Response `201 Created`:

```json
{
  "transactionId": 1,
  "orderId": 1,
  "amount": 42.43,
  "status": "PENDING"
}
```

Response `409 Conflict` — if a payment has already been initiated for the given `orderId`:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Payment already initiated for order 1"
}
```

The service enforces idempotency using `orderId` as the key — submitting the same `orderId` twice will always return `409` rather than creating a duplicate transaction.

---

### Shipments

All shipment endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

Shipment records are created automatically when a `PaymentCompleted` event is consumed from Kafka. The initial status is `PENDING`.

#### Update shipment status

```
PATCH /api/shipments/{id}
```

Path parameter: `id` is the shipment record ID.

Request body:

```json
{
  "status": "ON_TRANSIT"
}
```

Valid status values (must progress in order): `PENDING` → `ON_TRANSIT` → `OUT_FOR_DELIVERY` → `DELIVERED`

Response `200 OK`:

```json
{
  "id": 1,
  "orderId": 1,
  "status": "ON_TRANSIT",
  "dateOfDelivery": null
}
```

When `status` is set to `DELIVERED`, `dateOfDelivery` is automatically set to the current timestamp:

```json
{
  "id": 1,
  "orderId": 1,
  "status": "DELIVERED",
  "dateOfDelivery": "2026-06-14T15:30:00"
}
```

Response `404 Not Found` — if no shipment exists with the given ID.

Response `422 Unprocessable Entity` — if the requested status transition is not valid (e.g. skipping a step or going backwards).
