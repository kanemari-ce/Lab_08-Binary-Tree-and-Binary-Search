# Lab_08-Binary-Tree-and-Binary-Search

A generic **binary search tree (BST)** implementation in Java, developed **test-first**: the student-authored unit tests were written and proven to fail against TODO stubs *before* the tree logic was implemented, using a small dependency-free test runner instead of JUnit.

The tree supports insertion, lookup, an inclusive **range search** with branch pruning, and deletion covering all three structural cases (leaf, one child, two children via in-order successor). A full step-by-step insertion/deletion trace and the test documentation are in [`Lab8.pdf`](Lab8.pdf).

## Features

- **`put(key, value)`** — recursive insert; rejects null keys/values; replaces the value of an existing key without growing the tree; returns the old value.
- **`get(key)`** — recursive lookup; returns `null` when the key is absent.
- **`rangeSearch(min, max)`** — inclusive on both ends, values returned in ascending key order; inverted bounds (`min > max`) return an empty collection; subtrees that cannot intersect the range are pruned, never visited.
- **`delete(key)`** — three cases: no children, one child, two children. The two-child case copies the in-order successor (leftmost node of the right subtree) into the node, then splices the successor out by reattaching its right child to its parent.
- **`keysInOrder()`** — in-order key dump used by the tests to prove the ordering invariant.

## Project Structure

```
Lab8/
├── README.md
├── Lab8.pdf                       # Trace + unit-test documentation
├── AI-Use-Declaration.pdf         # AI-use declaration (model, prompts, verification)
├── docs/
│   ├── Lab8.html                  # HTML source of the lab report PDF
│   └── AI-Use-Declaration.html    # HTML source of the declaration PDF
├── src/
│   ├── LabTestRunner.java         # Dependency-free test runner (no JUnit)
│   ├── BSTNode.java               # Generic node: comparable key, value, left, right
│   ├── BinarySearchTree.java      # put / get / rangeSearch / delete
│   └── BSTEdgeCaseTest.java       # 7 student-authored tests (edge cases + contracts)
└── out/                           # Compiled classes (gitignored)
```

## Installation / Usage

Requires a JDK (developed and verified on Java 25 LTS). From the repository root:

```powershell
# 1. Compile all sources into out/
javac -d out src\*.java

# 2. Run the unit-test suite
java -cp out LabTestRunner BSTEdgeCaseTest
```

`LabTestRunner` discovers every public no-arg `test*` method, runs each on a fresh instance, prints a PASS/FAIL line per test, and exits with code `0` when everything passes (`1` on any failure, `2` on usage error). Expected output for the current state of the tree code:

```
== BSTEdgeCaseTest ==
  [PASS] testDeleteLeaf                                 (8.1 ms)
  [PASS] testDeleteOneChild                             (0.3 ms)
  [PASS] testDeleteTwoChildrenSuccessorHasRightChild    (0.2 ms)
  [PASS] testEmptyTree                                  (0.4 ms)
  [PASS] testPutRejectsNullKeyAndValue                  (3.2 ms)
  [PASS] testPutReplaceKeepsSize                        (0.3 ms)
  [PASS] testRangeSearchInvertedBounds                  (0.4 ms)
  7/7 passed
ALL GREEN
```

The tests are written test-first: while any tree method is still a TODO stub (`UnsupportedOperationException`), the suite reports those tests as failures with the stub message.

### Regenerating the PDFs (optional)

The two PDFs are rendered from the HTML sources in `docs/` with Microsoft Edge in headless mode:

```powershell
& "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --headless=new --disable-gpu --no-pdf-header-footer --print-to-pdf="Lab8.pdf" "file:///<repo>/docs/Lab8.html"
& "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --headless=new --disable-gpu --no-pdf-header-footer --print-to-pdf="AI-Use-Declaration.pdf" "file:///<repo>/docs/AI-Use-Declaration.html"
```

## Tech Stack

| Component | Choice |
|---|---|
| Language | Java 25.0.2 LTS |
| Dependencies | **None** — pure JDK; `LabTestRunner` (reflection-based) replaces JUnit/Maven/Gradle |
| Build | `javac` / `java` directly, output to `out/` |
| PDF generation | Microsoft Edge headless (`--print-to-pdf`) from HTML sources |
| Version control | Git |

## Documentation

- [`Lab8.pdf`](Lab8.pdf) — insertion trace of `[40, 20, 70, 10, 30, 55, 85, 60]` (per-step key, comparison path, link, and tree), the delete-root-40 trace (successor search, copy, splice), the unit-test table, and the test-first runner evidence.
- [`AI-Use-Declaration.pdf`](AI-Use-Declaration.pdf) — AI model used, verbatim prompts, suggestions adopted/rejected, and verification.
