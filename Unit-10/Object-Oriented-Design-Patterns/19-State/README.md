# 19 — State: The Vending Machine

## The Story

Stand in front of a vending machine and press the dispense button before you've
selected anything. Nothing happens — or you get an error bleep. Now select an
item and press it again. Still nothing — you haven't paid. Insert your money,
press it one more time: your bag of chips drops.

The same button. The same press. Three completely different outcomes depending on
*what state the machine is in*.

Without the State pattern you'd write this as one enormous method stuffed with
`if (selected && moneyInserted && !dispensing)`. Each new state multiplies every
condition check. With State, you create one class per state, each handling only
the events that make sense there.

---

## The Problem It Solves

Behaviour that changes based on an object's mode leads to sprawling conditionals
that become impossible to extend safely. The State pattern extracts each mode
into its own class. The context (vending machine) holds a reference to the
current state object and delegates every action to it. Transitioning to a new
state is just swapping the reference.

---

## States and Transitions

```
IDLE ──(selectItem)──→ ITEM_SELECTED ──(insertMoney)──→ PAYMENT_PENDING
                              │                               │
                           (cancel)                    (dispense, paid)
                              ↓                               ↓
                            IDLE                         DISPENSING
                                                             │
                                                         (dispense)
                                                             ↓
                                                           IDLE
```

---

## Structure

```
VendingMachineState     ← State interface (selectItem, insertMoney, dispense, cancel)
  ├── IdleState
  ├── ItemSelectedState
  ├── PaymentPendingState
  └── DispensingState

VendingMachine          ← Context (holds current state, delegates all actions)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Object behaviour changes with mode | Traffic lights, vending machines, order lifecycle |
| Large conditional blocks branching on internal status | TCP connections (LISTEN/SYN_RCVD/ESTABLISHED/CLOSE_WAIT) |
| Clean transition rules per state | Workflow engines, game AI, UI wizards |

Java's `javax.faces.lifecycle.Lifecycle`, thread state (`NEW/RUNNABLE/BLOCKED/TERMINATED`),
and network protocol implementations all use this pattern.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
