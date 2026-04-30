# 13 — Chain of Responsibility: The Expense Approval Workflow

## The Story

Alice submits a $45 expense for a team lunch. Her manager glances at it and
approves it in two seconds — well within the $100 self-approval limit.

Bob submits a $750 hotel bill for a conference. Alice's limit is exceeded, so the
system automatically escalates it to the manager (limit: $1,000). Approved.

Carol submits an $8,500 server purchase. Beyond the manager's authority. Escalated
to the director (limit: $10,000). Approved.

Dave submits a $35,000 office renovation quote. Beyond everyone's authority except
the CFO. Escalated all the way up. The CFO reviews it and approves.

Each step in this process is a **handler** in the chain. Each handler either
resolves the request or passes it to the next one in line.

---

## The Problem It Solves

When multiple objects might handle a request, and the handler isn't known in
advance, the chain lets you decouple senders from receivers. The submitter just
says "please approve this" — they don't know (and don't care) which approver
actually signs off.

You can change the chain — reorder approvers, change limits, insert new levels —
without touching the submitter code.

---

## Structure

```
Approver         ← Abstract Handler (setNext, approve)
  ├── EmployeeApprover  ← limit: $100
  ├── ManagerApprover   ← limit: $1,000
  ├── DirectorApprover  ← limit: $10,000
  └── CfoApprover       ← limit: unlimited

ExpenseReport    ← Request
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Multiple handlers may process a request | Support ticket escalation |
| Handler is determined at runtime | Middleware pipeline in a web framework |
| The sender shouldn't be coupled to a specific handler | Event propagation in GUI frameworks |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
