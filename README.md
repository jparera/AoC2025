# Advent of Code 2025 (Java)

This repository contains solutions for the Advent of Code 2025 puzzles written in Java.

## Getting Started

**Prerequisites:**

* **Java 25 or later:** ([https://www.oracle.com/java/technologies/javase-downloads.html](https://www.oracle.com/java/technologies/javase-downloads.html))
* A code editor or IDE (e.g., IntelliJ IDEA, Eclipse)

**Running the Solutions:**

Each day's solution is a separate Java class located in the `src/` directory. The class name follows the format `DayXX.java`, where XX is the day number.

To run a solution:

1. Download the input data for the desired day from Advent of Code ([https://adventofcode.com/2024](https://adventofcode.com/2024)).
2. Place the input data in a file named `inputXX.txt` in the root directory of this repository, where XX is the day number.
3. Open a terminal or command prompt and navigate to the root directory of the repository.
4. Execute the desired solution class:

```bash
java src/DayXX.java
```

## Benchmarking

`src/Measure.java` provides a simple way to measure the performance of a puzzle solution. It offers the following features:

* **Configurable Iterations:** Customize the number of iterations to fine-tune your benchmarks.
* **Warmup Phase:** Ensures accurate measurements by running a warmup phase to eliminate initial overhead.
* **Human-Readable Output:** Displays results in clear units (seconds, milliseconds, microseconds, etc.) for easy interpretation.
* **Modular Design:** Easily add new solutions to be benchmarked by implementing the `Solution` interface.

**Parameters**

* **--loops:** Specifies the number of iterations for the benchmark. Default: 10.

**How to Use**

1. **Implement Your Solution:** Create a class that implements the `Solution` interface and define its `call` method to execute your solution logic.
2. **Add Your Solution to `SOLUTIONS`:** Add your solution class to the `SOLUTIONS` array in the `Measure` class.
3. **Run the Benchmark:** Execute the `Measure` class with the desired day number as an argument, optionally specifying the `--loops` parameter.

**Example:**

```bash
java src/Measure.java --loops 100 1
```

This will benchmark the solution for Day 1 with 100 iterations.
