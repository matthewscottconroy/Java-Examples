# Java Concurrency — Graduated Examples

Ten self-contained Maven projects that build from raw threads to modern virtual-thread concurrency.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-ThreadBasics` | Thread lifecycle, Thread vs Runnable, sleep/join/interrupt, daemon threads |
| 2 | `02-RunnableCallableFuture` | Runnable vs Callable, Future, FutureTask, get/cancel/isDone |
| 3 | `03-ExecutorService` | Thread pools, ScheduledExecutorService, ThreadPoolExecutor internals |
| 4 | `04-CompletableFuture` | Async pipelines, chaining, combining, error handling |
| 5 | `05-Synchronization` | Race conditions, synchronized, volatile, wait/notify, JMM |
| 6 | `06-ExplicitLocks` | ReentrantLock, ReadWriteLock, StampedLock, Condition |
| 7 | `07-AtomicsAndCollections` | Atomic types, LongAdder, ConcurrentHashMap, blocking queues |
| 8 | `08-Synchronizers` | CountDownLatch, CyclicBarrier, Semaphore, Exchanger |
| 9 | `09-ForkJoin` | RecursiveTask, RecursiveAction, work-stealing, parallel streams |
| 10 | `10-VirtualThreads` | Virtual threads (Java 21), cancellation, thread-per-request |

## Running any example

```
cd 01-ThreadBasics
mvn compile exec:java
```

## Concept progression

```
01  Thread + Runnable          raw thread creation, lifecycle states
02  Callable + Future          tasks that return values and throw exceptions
03  ExecutorService            pools, scheduling, rejection, thread factories
04  CompletableFuture          non-blocking composition without callbacks
05  synchronized / volatile    core synchronization primitives and the JMM
06  ReentrantLock / Condition  explicit locks, fairness, optimistic reads
07  Atomic + concurrent colls  lock-free counters, concurrent maps, blocking queues
08  Synchronizers              latches, barriers, semaphores, exchangers
09  ForkJoin + streams         divide-and-conquer, work-stealing, parallel streams
10  Virtual threads            Project Loom, thread-per-request, cancellation
```
