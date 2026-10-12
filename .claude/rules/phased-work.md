# Phased work: phases → tasks → commits → validation

No `paths` on purpose: this is how every piece of work is done, not only the numbered plans.

## Flow

1. **Phase.** Work is split into phases with a testable outcome. One phase per session; the next one starts after `/clear` or in a new session. What must be remembered goes into the plan docs, not the chat.
2. **Tasks.** Before touching anything, read the plan and propose the numbered task list, each task with how it is validated. Wait for approval. Missing design or open decisions are asked, never invented.
3. **Commits.** One small commit per task, made only after that task is validated. The approval of the proposal covers those commits and nothing else. If a validation fails, do not commit.
4. **Validation.** Each task is validated by Claude when possible (unit tests, `ktlintCheck`, screenshots, emulator); human validation only for what cannot be done alone.
5. **Close the phase.** Present what was done, how it was validated and what is pending. The phase is closed, and the plan updated (`- [x]` with commit hash), only when Bernat approves it.
6. **Push** only with the phase validated, or when Bernat asks for it explicitly.

For work that is not part of a numbered plan (a single backlog item, a bugfix), apply the same loop at small scale: short task list, approval, validated commits, then ask for validation.
