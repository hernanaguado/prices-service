# prices-service

REST service that returns the price list and final price that apply to a brand product at a given date.

Built with Java 25, Spring Boot 4.1.1 and Maven, following a hexagonal architecture split into Maven modules.

## Requirements

- JDK 25 (only to build and run locally; Maven is provided by the Maven Wrapper).
- Docker (optional, to build and run the container image).

## Build and run

```bash
# Build, running unit, slice and end-to-end tests
./mvnw verify

# Run the executable jar
./mvnw package -DskipTests
java -jar prices-boot/target/prices-boot-0.0.1-SNAPSHOT.jar

# Or run with the Spring Boot Maven plugin (the modules must be installed in the local repository first)
./mvnw install -DskipTests
./mvnw -pl prices-boot spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

### Docker

```bash
docker build -t prices-service .
docker run --rm -p 8080:8080 prices-service
```

The image build runs `package -DskipTests` because `./mvnw verify` (locally or in CI) already runs the tests before
an image is built, and running them again in every image build would only make it slower.

## Tests

| Command          | What runs                                                                  |
|------------------|----------------------------------------------------------------------------|
| `./mvnw test`    | Unit tests (domain, application) and slice tests (`@WebMvcTest`, `@DataJpaTest`) with Surefire |
| `./mvnw verify`  | All of the above plus the end-to-end tests (`*IT`, REST Assured against the running app) with Failsafe |

- **Unit**: priority and tie-break rule (`PriceSelectionPolicyTest`) and the use case with Mockito
  (`GetApplicablePriceServiceTest`).
- **Slice**: controller with a mocked use case covering 200, 400, 404 and 500 (`PriceControllerTest`); persistence
  adapter against H2 using the real `schema.sql` and `data.sql` (`PricePersistenceAdapterTest`).
- **End-to-end** (`PricesApiIT`): the five required scenarios checking every response field, plus 404 for an unknown
  product or brand and 400 for missing or invalid parameters.

## API

Swagger UI: <http://localhost:8080/swagger-ui.html> (it renders the static contract served at
<http://localhost:8080/openapi.yaml>).

`GET /prices?applicationDate={date}&productId={id}&brandId={id}`

| Parameter         | Type                                              | Rules                 |
|-------------------|---------------------------------------------------|-----------------------|
| `applicationDate` | ISO-8601 local date-time, no zone (`2020-06-14T10:00:00`) | required              |
| `productId`       | integer                                           | required, positive    |
| `brandId`         | integer                                           | required, positive    |

Example response:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

Errors are returned as `application/problem+json` ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)):
`400` for a missing parameter, malformed date, wrong type or non-positive id; `404` when no price applies; `500` for
unexpected errors, with a generic message and no internal details.

### Examples

```bash
# Test 1: 2020-06-14 10:00 -> price list 1, 35.50
curl "http://localhost:8080/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"

# Test 2: 2020-06-14 16:00 -> price list 2, 25.45
curl "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"

# Test 3: 2020-06-14 21:00 -> price list 1, 35.50
curl "http://localhost:8080/prices?applicationDate=2020-06-14T21:00:00&productId=35455&brandId=1"

# Test 4: 2020-06-15 10:00 -> price list 3, 30.50
curl "http://localhost:8080/prices?applicationDate=2020-06-15T10:00:00&productId=35455&brandId=1"

# Test 5: 2020-06-16 21:00 -> price list 4, 38.95
curl "http://localhost:8080/prices?applicationDate=2020-06-16T21:00:00&productId=35455&brandId=1"

# Error: no applicable price -> 404
curl "http://localhost:8080/prices?applicationDate=2019-01-01T00:00:00&productId=35455&brandId=1"
```

## Modules and hexagonal architecture

```
prices-service (parent POM: versions and plugins)
├── prices-domain                plain Java
│   ├── model                    Price
│   ├── policy                   PriceSelectionPolicy (priority and tie-break rule)
│   ├── port/in, port/out        GetApplicablePriceUseCase, PriceRepositoryPort
│   └── exception                PriceNotFoundException
├── prices-application           use case implementation — depends only on prices-domain
├── prices-infrastructure        adapters
│   ├── adapter/in/rest          controller implementing the generated API, MapStruct mapper, error handler
│   ├── adapter/out/persistence  JPA entities, Spring Data repository, adapter implementing the output port
│   ├── config                   Swagger UI setup serving the static contract
│   └── resources                openapi.yaml, schema.sql, data.sql
└── prices-boot                  Spring Boot application, use case wiring, configuration, end-to-end tests
```

- **Ports** live in the domain: `GetApplicablePriceUseCase` (input) and `PriceRepositoryPort` (output).
- **Adapters** live in infrastructure: the REST controller drives the input port and the persistence adapter
  implements the output port.
- **Dependency direction is enforced by Maven**: `prices-domain` has no dependencies, `prices-application` only sees
  the domain, and only `prices-infrastructure` and `prices-boot` know Spring. Code that breaks the rule does not
  compile.
- `prices-boot` is the composition root: `UseCaseConfiguration` registers the framework-free application service as
  a bean, and it is the only module that produces the executable jar.

## Design decisions

- **Priority resolved in the domain.** The repository returns all prices valid at the date; `PriceSelectionPolicy` picks the highest `priority`, and on a tie the most recent `startDate`. The rule stays in the domain and is unit tested without a database.
- **`LocalDateTime` without time zone.** The source data has no zone, so none is added.
- **`BigDecimal` / `DECIMAL(10,2)`** for exact money values.
- **`BRAND` table with a foreign key**, as the statement defines `BRAND_ID`. The domain only uses `brandId`.
- **Spring Data derived query**, no SQL or JPQL in Java code.
- **`schema.sql` and `data.sql`** for the in-memory H2 database. In production, Flyway or Liquibase.
- **API first.** `openapi.yaml` generates the API interface and models, and Swagger UI shows that same file.
- **ProblemDetail (RFC 9457)** for all errors.
- **MapStruct** for mappings, generated at compile time.
- **Records, no Lombok.** The domain uses records; JPA entities are plain classes because JPA cannot use records.
- **Multi-stage Docker image** with a small JRE Alpine runtime (amd64 and arm64) and a non-root user.
