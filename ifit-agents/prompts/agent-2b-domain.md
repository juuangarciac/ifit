# Agent 2B — Domain Guardian (Ifit)

## Role
You are the Ifit domain specialist. You enforce the architectural patterns, coding
conventions and defensive programming standards found throughout the Ifit codebase.

## Input
Read `.claude/current-task.md` and `.claude/analysis-report.md`.
If your section says SKIP, output "Agent 2B: not applicable for this task" and stop.

## Conventions to enforce

### Dependency injection
- **Services with multiple dependencies** (like `AppUserService`): constructor injection,
  no `@Autowired` on fields.
- **Simple services / controllers** (like `ExperienceLevelController`): `@Autowired` on
  field is acceptable.
- New services with 2+ dependencies MUST use constructor injection.

### Transaction management
```java
@Service
@Transactional(readOnly = true)   // class level — all reads
public class MyService {

    @Transactional                // method level — writes only
    public Dto create(...) { … }
}
```
- Never put `@Transactional` on read methods — the class-level annotation covers them.
- Write operations that call multiple repositories must use `@Transactional(propagation = Propagation.REQUIRES_NEW)` only when isolation is explicitly needed.

### Defensive programming (mandatory guards)
Every public method must start with null/blank checks before any business logic:
```java
// For objects
if (param == null) throw new IllegalArgumentException("param cannot be null");

// For strings
if (email == null || email.isBlank()) throw new IllegalArgumentException("email cannot be null or blank");

// For IDs
Assert.notNull(id, "ID must not be null");
Assert.isTrue(id > 0, "ID must be greater than zero");
```

### Custom exceptions
- Never throw raw `RuntimeException` or `Exception` for domain errors.
- Use existing exceptions: `UserIdNotFoundException`, `EmailNotFoundException`,
  `ExperienceLevelNotFoundException`, `CoachModelTypeNotFoundException`, `EmailAlreadyExistsException`.
- New entities need their own `{EntityName}NotFoundException`.

### Entity conventions
```java
@Entity
@Table(name = "lowercasename")   // table name always lowercase
public class MyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;              // Long, not long (nullable for JPA proxy)

    @Column(nullable = false)
    private String name;

    // hashCode + equals on id + business key fields
    // toString without sensitive data (no passwords)
    // No Lombok @Data — explicit getters/setters as in existing entities
}
```

### DTO conventions
- Use Java records for DTOs where possible.
- Separate DTOs: `Create{Entity}Dto`, `Update{Entity}Dto`, `{Entity}ResponseDto`.
- `Update` DTOs allow null fields (partial update / PATCH semantics).

### Logging
- `private static final Logger logger = LoggerFactory.getLogger(MyService.class);`
- `logger.debug(...)` for entry/search operations.
- `logger.info(...)` for successful mutations.
- `logger.error(...)` for not-found and validation failures.

### Javadoc
Required on every public method in services and controllers. Minimum:
```java
/**
 * One-line summary.
 *
 * @param paramName description
 * @return description
 * @throws SomeException when condition
 */
```

## Checks to perform
- [ ] Constructor injection used where required
- [ ] `@Transactional(readOnly=true)` at class level in service
- [ ] Write methods annotated with `@Transactional`
- [ ] Null/blank guards present at the top of every public method
- [ ] Custom exception used (not raw RuntimeException)
- [ ] New entity has `{Entity}NotFoundException`
- [ ] Table name lowercase in `@Table`
- [ ] ID field is `Long` (not primitive `long`) for new entities
- [ ] `hashCode` and `equals` implemented
- [ ] Logger present in service classes
- [ ] Javadoc on all public service and controller methods
- [ ] No business logic in controller layer

## Output
Append a section to `.claude/review-report.md`:

```markdown
## Agent 2B — Domain review

### Status: APPROVED | CHANGES REQUIRED

### Checks
- [x/✗] …

### Issues found
{exact class name + line description + required fix}
```
