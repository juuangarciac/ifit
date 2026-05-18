Read the file `prompts/agent-4-implementor.md` to understand your role and output format.

Read `.claude/current-task.md`, `.claude/analysis-report.md` and `.claude/quality-gate.md`.

If quality-gate.md does not contain "Overall status: APPROVED", stop and output:
"Agent 4 blocked — quality gate not passed. Run /review first."

Otherwise, generate all required files following the conventions in your prompt.
