# Bajaj Finserv Health - SRM Quiz Task

This repository contains the solution for the Bajaj Finserv Health JVM Qualifier assignment.

## 1. Problem Statement
The goal of this assignment is to simulate a real-world backend integration problem. We need to build an application that:
- Polls a validator API exactly 10 times to fetch events (scores of participants across rounds).
- Maintains a mandatory 5-second delay between continuous requests.
- Deduplicates the events using `(roundId + participant)`.
- Aggregates the results to compute the final total score per participant.
- Generates a leaderboard ranked by the `totalScore` of users.
- Submits the final leaderboard payload to the `POST /quiz/submit` endpoint exactly once.

## 2. Requirements & Setup
- **Java**: JDK 11 or higher (Tested with Java 17).
- **Maven**: To build the project and manage dependencies.

## 3. How to Run

You can run the task directly via Maven command.

1. Open a terminal in the root directory of this project where `pom.xml` is located.
2. If you need to change your `regNo` (default is `2024CS101`), you can pass it as a program argument or modify `com.bajaj.quiz.QuizTask.java`.
3. Execute the standard Maven process using the `exec:java` plugin:
   ```bash
   # On Windows:
   .\mvnw.cmd clean compile exec:java

   # On Mac/Linux:
   ./mvnw clean compile exec:java
   ```
   **OR** if you wish to pass a specific `regNo`:
   ```bash
   # On Windows:
   .\mvnw.cmd clean compile exec:java -Dexec.args="YOUR_REG_NO"

   # On Mac/Linux:
   ./mvnw clean compile exec:java -Dexec.args="YOUR_REG_NO"
   ```

## 4. How the Code Works
- **Polling Phase**: The `main` method triggers a `for` loop `0` up to `9`. Within the loop, a standard `java.net.http.HttpClient` performs a GET request to the quiz API to fetch real-time batch events.
- **Handling Duplicates**: In distributed systems, records can duplicate. The deduplication handles this organically by employing a Java `HashSet` holding combinations of `roundId_participant`. If the current iteration encounters an existing key, the specific score increment is bypassed, preserving accuracy.
- **Aggregation phase**: We compute total scores dynamically per user in a `HashMap`. 
- **Sorting Phase**: Maps don't hold order, so the results are transposed into an `ArrayList` and sorted in descending order based on `totalScore`.
- **Submission Phase**: `Jackson` is leveraged for serialization, turning the in-memory array/objects into standard json structure. The final artifact is published to `/quiz/submit`.
