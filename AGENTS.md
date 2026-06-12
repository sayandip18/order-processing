# AI Agent Rules and Coding Standards (AGENTS.md)

This document outlines the strict guidelines, constraints, and coding standards that all AI agents must follow when generating, refactoring, or modifying code for this project.

---

## 1. Core Architecture & Stack Constraints

- **Framework & Build Tool:** Spring Boot (Java) using Maven.
- **Asynchronous Messaging:** Kafka is the exclusive backbone for event-driven orchestration. Do not introduce synchronous REST or gRPC calls between services for distributed transactions.
- **State & Consistency:** The Saga Pattern must be strictly followed. Every forward transaction must have a corresponding, well-defined compensating transaction (rollback event).
- **Distributed State:** Use Redis explicitly for distributed locking, idempotency tracking, and caching. Do not rely on in-memory application state for distributed synchronization.

---

## 2. Coding Standards & Conventions

### Spring Boot & Java

- **Layered Architecture:** Adhere to a strict separation of concerns: `Controller` ➔ `Service` ➔ `Repository` / `Consumer`.
- **Lombok Usage:** Use Lombok (`@Data`, `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`) to reduce boilerplate, but ensure it doesn't conflict with Spring/JPA annotations.
- **Dependency Injection:** Always prefer constructor injection over `@Autowired` on fields. Use `@RequiredArgsConstructor` on the class level.

### Package & Naming Conventions

- **Events/Topics:** Name Kafka events using past-tense verbs (e.g., `OrderCreatedEvent`, `InventoryReservedEvent`, `PaymentFailedEvent`).
- **Immutability:** DTOs and Event Payloads must be immutable. Use Java records or Lombok's `@Value` annotation where applicable.

---

## 3. Distributed Saga & Kafka Rules

### Forward and Compensating Transactions

- Every consumer handling a Saga step must be wrapped in a `try-catch` block.
- **On Failure:** The `catch` block **must** emit a specific compensating event to the appropriate Kafka rollback topic. Never let a transaction fail silently.

### Idempotency (Strict Requirement)

- Distributed transactions mean messages will be retried. **All Kafka consumers must be idempotent.**
- Before processing any event, check Redis using an idempotency key (e.g., `idempotency:order:{orderId}:{step}`).
- If the key exists and is marked as processed, log a warning and skip processing immediately.

### Distributed Locking

- When updating inventory rows or shared resources, acquire a Redis distributed lock (e.g., using Redisson or custom Redis strings) before performing the database operation.
- Always release the lock in a `finally` block to prevent deadlocks.

---

## 4. Resilience & Error Handling

### Database Transactions

- Use `@Transactional` only for local, short-lived database writes. Do not hold a database transaction open while waiting for a Kafka acknowledgement or Redis lock response.

### Dead Letter Queues (DLQ)

- Unrecoverable errors (e.g., serialization issues, invalid payloads) must not halt the consumer. Route these messages to a designated `.DLQ` topic.

---

## 5. Definition of Done for Agents

When generating or modifying code, ensure you provide:

1.  **The complete implementation** without placeholders or `// TODO` comments for logic.
2.  **Unit Tests:** Corresponding unit or integration tests using JUnit 5 and Mockito.
3.  **No Architecture Changes:** Do not add new database technologies, third-party libraries, or architectural layers unless explicitly requested by the user.
