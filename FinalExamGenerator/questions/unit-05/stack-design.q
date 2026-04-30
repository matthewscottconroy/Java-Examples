# Example external question — Unit 05: Collections / Design

TYPE:       DESIGN
TOPIC:      Stack Design
UNIT:       05
DIFFICULTY: MEDIUM

---PROMPT---
Design a generic Stack<T> class backed by an ArrayList. Your design
should support push(T item), T pop(), T peek(), boolean isEmpty(),
and int size(). Discuss what happens when pop() or peek() is called
on an empty stack.
---KEY---
Core fields: ArrayList<T> items = new ArrayList<>();

push   → items.add(item)
pop    → check isEmpty(), throw NoSuchElementException if empty,
         else items.remove(items.size() - 1)
peek   → same empty check, return items.get(items.size() - 1)
isEmpty → items.isEmpty()
size   → items.size()

Empty-stack handling: throw a checked or unchecked exception
(NoSuchElementException is idiomatic). Document the contract in
a Javadoc @throws clause. An alternative is to return Optional<T>.
---END---
