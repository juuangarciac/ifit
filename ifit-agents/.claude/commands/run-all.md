Run the complete iFit agent pipeline for the following request: $ARGUMENTS

Execute each step in order. After each step, confirm it completed before proceeding.

## Step 1 — Orchestrate
Read `prompts/agent-0-orchestrator.md`.
Write `.claude/current-task.md` based on the request above.
Print the agent summary table.

## Step 2 — Analyze
Read `prompts/agent-1-analyst.md`.
Write `.claude/analysis-report.md`.

## Step 3 — Guardian reviews (parallel)
Read and apply all three guardian prompts:
- `prompts/agent-2a-gateway.md`
- `prompts/agent-2b-domain.md`
- `prompts/agent-2c-ai.md`
Append each result to `.claude/review-report.md`.
Skip agents whose task section says SKIP.

## Step 4 — Quality gate
Read `prompts/agent-3-reviewer.md`.
Write `.claude/quality-gate.md`.
If status is CHANGES REQUIRED: print the full issue list and STOP.
Do not proceed to Step 5.

## Step 5 — Implement
Read `prompts/agent-4-implementor.md`.
Generate all required files.
Update `.claude/quality-gate.md` with the implementation summary.

## Step 6 — Verify
Read `prompts/agent-5-verifier.md`.
Write `.claude/verification-report.md`.
If result is FAIL: print the issue list and indicate corrections needed in Agent 4.
If result is PASS: print "Pipeline complete. Ready to merge."
