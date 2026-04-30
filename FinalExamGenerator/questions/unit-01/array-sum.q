# Example external question — Unit 01: Arrays
# Instructors can add questions here without recompiling.

TYPE:       WRITE
TOPIC:      Arrays
UNIT:       01
DIFFICULTY: EASY

---PROMPT---
Write a static method int sum(int[] arr) that returns the sum of all
elements in the array. Return 0 for an empty array.
---CODE---
public static int sum(int[] arr) {
    // TODO: implement
}
---KEY---
Iterate with a for-each loop accumulating into a running total:
  int total = 0;
  for (int x : arr) total += x;
  return total;
Edge case: arr.length == 0 → returns 0 naturally.
---END---
