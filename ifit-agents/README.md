# iFit Agents

Multi-agent workflow for the iFit project. Ensures quality, consistency and
defensive programming across ApiGateway, Ifit and Ronnie before any code lands.

## Setup

Copy this folder into the root of your iFit monorepo (or workspace root if
the three services are separate repos in the same Claude Code session).

```
your-workspace/
├── api-gateway/
├── ifit/
├── ronnie/
└── ifit-agents/      ← this folder
    ├── CLAUDE.md
    ├── README.md
    ├── prompts/
    └── .claude/
        └── commands/
```

Claude Code will detect `.claude/commands/` automatically and register the slash commands.

---

## Usage

### Full pipeline (recommended)
```
/run-all "Add a new endpoint to get user routines paginated"
```
Runs all 6 agents in order. Stops at the quality gate if issues are found.

### Step by step
Run agents individually when you want finer control:

```
/orchestrate "Add a new AI coach called Serena specialized in cardio"
/analyze
/guard-gateway
/guard-domain
/guard-ai
/review
/implement
/verify
```

---

## Agents

| # | Command | Role |
|---|---|---|
| 0 | `/orchestrate` | Parses the request and writes `.claude/current-task.md` |
| 1 | `/analyze` | Detects affected services and maps dependency order |
| 2A | `/guard-gateway` | Validates ApiGateway routes, JWT and StripPrefix |
| 2B | `/guard-domain` | Enforces Ifit patterns: JPA, transactions, exceptions |
| 2C | `/guard-ai` | Enforces LangChain4j, @AiService and ChatContext rules |
| 3 | `/review` | Quality gate — blocks or approves implementation |
| 4 | `/implement` | Generates production-ready code (only after gate passes) |
| 5 | `/verify` | Cross-service regression and JWT flow check |

Agents 2A, 2B and 2C run in parallel. Each writes SKIP if its service is not affected.

---

## Generated files

Each pipeline run produces these files under `.claude/`:

| File | Written by |
|---|---|
| `current-task.md` | Agent 0 — task definition and agent assignments |
| `analysis-report.md` | Agent 1 — dependency graph and risk flags |
| `review-report.md` | Agents 2A/2B/2C — guardian checks |
| `quality-gate.md` | Agent 3 — go/no-go decision + Agent 4 file list |
| `verification-report.md` | Agent 5 — final cross-service verdict |

These files are the audit trail of the pipeline. Commit them alongside the code.
