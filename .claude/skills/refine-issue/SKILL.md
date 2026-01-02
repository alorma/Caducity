---
name: refine-issue
description: Expert guidance for refining GitHub issues into development tasks
tags: [issues, github, refinement, refine, planning]
---

# Refine GitHub Issue Skill

You are helping to refine a GitHub issue into a well-defined development task. Extract the issue number from the user's request (e.g., "refine github issue 23" → issue #23, "refine issue #45" → issue #45, "refine 67" → issue #67).

**IMPORTANT**: Use the TodoWrite tool to track your refinement process with these tasks:
1. Fetch issue details from GitHub
2. Gather context from codebase
3. Ask clarifying questions (if needed)
4. Create refinement document
5. Update GitHub issue

## Step 1: Fetch Issue Details

Run: `gh issue view ${ISSUE_NUMBER} --json title,body,labels,state,comments,assignees`

Review the complete issue context including:
- Title and description
- Current labels and state
- All comments and discussion
- Assignees (if any)

## Step 2: Gather Codebase Context

Use the Task tool with subagent_type=Explore to:
- Find relevant code files related to the issue topic
- Review CLAUDE.md for architecture patterns and conventions
- Check DEVELOPMENT_PLAN.md (if exists) for related features
- Search for similar implementations or patterns already in use
- Identify affected components and dependencies

**Example searches**:
- Find feature-related files: Use Glob with patterns like `**/*<feature>*.kt`
- Search for related code: Use Grep for keywords from the issue
- Understand architecture: Read relevant screen/ViewModel/UseCase files

## Step 3: Analyze and Clarify

Based on the issue and codebase context:

**If requirements are unclear**, use AskUserQuestion to clarify:
- Specific behavior expectations
- UI/UX preferences
- Technical approach preferences (e.g., which library, pattern)
- Scope boundaries (what's in/out)
- Edge cases and error handling requirements
- Testing expectations

**If requirements are clear**, proceed to refinement.

## Step 4: Create Refinement Document

Structure your refinement as follows:

### User Story
**As a** [user type]
**I want** [goal]
**So that** [benefit]

### Technical Context
- **Affected Components**: List screens, ViewModels, UseCases, data sources
- **Architecture Pattern**: Follow CLAUDE.md conventions (MVI/MVVM, Clean Architecture)
- **Dependencies**: Existing features or models this depends on
- **Integration Points**: Where this connects to existing code

### Acceptance Criteria
Clear, testable criteria in checkbox format:
- [ ] Criterion 1 (specific and measurable)
- [ ] Criterion 2 (specific and measurable)
- [ ] Criterion 3 (specific and measurable)

### Implementation Guidance

**Data Layer** (if applicable):
- Room entities to create/modify
- Data source interfaces
- Repository changes

**Domain Layer** (if applicable):
- Use cases to implement
- Domain models
- Business logic

**UI Layer** (if applicable):
- Screens to create/modify
- ViewModel state and events
- Navigation changes
- Compose components

**Other Considerations**:
- Notification system integration
- Background work (WorkManager)
- Dependency injection updates
- Testing strategy

### Technical Considerations
- Performance implications
- Security concerns
- Backwards compatibility
- Error handling approach
- Logging/debugging needs

### Out of Scope
Explicitly list what this issue does NOT include to prevent scope creep.

## Step 5: Update GitHub Issue

Update the issue using: `gh issue edit ${ISSUE_NUMBER}`

Add this refined content to the issue body:
```markdown
## Refinement

[Include the full refinement document from Step 4]

---
*Refined by Claude Code on [date]*
```

Then add the "Refined" label:
```bash
gh issue edit ${ISSUE_NUMBER} --add-label "Refined"
```

## Tips for Natural Language Usage

This skill understands various phrasings:
- "refine github issue 23"
- "refine issue #45"
- "refine 67"
- "help me refine issue 12"
- "can you refine gh issue 8"

Extract the issue number and follow the 5-step process above.

---

**Now begin**: Fetch issue details and start the refinement process.
