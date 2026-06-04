# iFit Agents — Claude Code Configuration

## Project purpose
Multi-agent workflow for the iFit project. Ensures quality, consistency and defensive
programming across three microservices: ApiGateway, Ifit and Ronnie.

## Agent commands
| Command | Description |
|---|---|
| `/orchestrate "<request>"` | Entry point. Parses the request and generates a task file. |
| `/analyze` | Agent 1 — context analysis. Detects affected service and dependencies. |
| `/guard-gateway` | Agent 2A — ApiGateway contracts (routes, JWT, StripPrefix). |
| `/guard-domain` | Agent 2B — Ifit domain rules (JPA, transactions, exceptions). |
| `/guard-ai` | Agent 2C — Ronnie AI rules (LangChain4j, @AiService, RAG). |
| `/review` | Agent 3 — quality review. Aggregates guardian reports. |
| `/implement` | Agent 4 — code generation following project conventions. |
| `/verify` | Agent 5 — final cross-service regression check. |
| `/run-all "<request>"` | Runs the full pipeline end to end. |

## Conventions enforced
- Java 21, Spring Boot 3.5.7, Spring Cloud 2025.0.0, Maven
- Constructor injection in services (field @Autowired only in simple services/controllers)
- @Transactional(readOnly=true) at class level, @Transactional on write methods
- Null/blank guards at the start of every method (IllegalArgumentException)
- Custom domain exceptions (UserIdNotFoundException, ExperienceLevelNotFoundException…)
- Javadoc required on all controllers and services
- LangChain4j @AiService must declare wiringMode=EXPLICIT with named beans
- ChatContext must always be managed in try/finally
- All routes in ApiGateway follow /ifit/api/v1/{service}/** with StripPrefix=3

## Task file
The orchestrator writes `.claude/current-task.md` before any agent runs.
Each agent reads this file to understand its scope and constraints.
