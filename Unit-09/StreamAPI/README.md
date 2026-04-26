# Java Stream API — Graduated Examples

Five self-contained Maven projects covering the full Stream pipeline from creation through
advanced collectors and Optional.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-StreamCreation` | of, iterate, generate, Arrays.stream, Files.lines, IntStream.range, builder |
| 2 | `02-IntermediateOps` | filter, map, flatMap, distinct, sorted, peek, limit, skip, takeWhile, dropWhile |
| 3 | `03-TerminalOps` | collect, reduce, forEach, count, min/max, findFirst/Any, anyMatch/allMatch |
| 4 | `04-Collectors` | groupingBy, partitioningBy, joining, toMap, counting, teeing, nested collectors |
| 5 | `05-OptionalAndPrimitives` | Optional, IntStream/LongStream/DoubleStream, summaryStatistics, boxed |

## Running any example

```
cd 01-StreamCreation
mvn compile exec:java
```

## Mental model

```
01  Source        lazy sequence — nothing executes until a terminal op runs
02  Intermediate  each call returns a new Stream; forms a lazy pipeline
03  Terminal      drives the whole pipeline; stream is consumed and cannot be reused
04  Collectors    fold a stream into a Map, grouped structure, or summary statistic
05  Optional      a one-element stream that models presence/absence without null
```
