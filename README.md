# 🏆 Quiz Leaderboard System

## 📌 Problem Overview

This project simulates a real-world backend integration scenario where data is fetched from an external API, processed, and transformed into a final leaderboard.

The challenge focuses on handling **duplicate API responses**, ensuring **data consistency**, and building a reliable aggregation pipeline.

---

## 🎯 Objective

The application performs the following:

* Polls a validator API **exactly 10 times**
* Maintains a **5-second delay** between each request
* Deduplicates events using `(roundId + participant)`
* Aggregates total scores per participant
* Generates a leaderboard sorted by `totalScore`
* Submits the final result **only once**

---

*

---

## 🚀 How to Run

### ▶️ Default Execution

```bash
# Windows
.\mvnw.cmd clean compile exec:java

# Mac/Linux
./mvnw clean compile exec:java
```

### ▶️ Run with Custom Registration Number

```bash
# Windows
.\mvnw.cmd clean compile exec:java -Dexec.args="YOUR_REG_NO"

# Mac/Linux
./mvnw clean compile exec:java -Dexec.args="YOUR_REG_NO"
```

---

## 🧠 How It Works

### 🔹 1. Polling Phase

* API is called **10 times (poll = 0 to 9)**
* A **5-second delay** ensures compliance with API constraints

---

### 🔹 2. Deduplication Logic

To prevent double counting, a `HashSet` is used:

```java
String key = roundId + "_" + participant;
```

* If key exists → duplicate → ignored
* Else → processed

---

### 🔹 3. Aggregation

A `HashMap` is used to accumulate scores:

```java
Map<String, Integer> scores;
```

---

### 🔹 4. Leaderboard Generation

* Convert map → list
* Sort in descending order of scores

---

### 🔹 5. Submission

* Final leaderboard is converted to JSON using **Jackson**
* Submitted to `/quiz/submit` endpoint

---

## 📊 Final Output

| Participant | Total Score |
| ----------- | ----------- |
| Bob         | 295         |
| Alice       | 280         |
| Charlie     | 260         |

**Total Score = 835**

---

## ⚠️ Key Challenge Solved

### Handling Duplicate API Data

Example:

* Poll 1 → Alice +10
* Poll 3 → Alice +10 (duplicate)

✔ Counted once
❌ Duplicate ignored

---

## 🧪 Features

* Reliable API polling
* Idempotent data processing
* Duplicate-safe aggregation
* Clean leaderboard generation
* Structured logging

---

## 📌 Notes

* Designed to handle real-world distributed system issues
* Ensures **data consistency** and **correctness**
* Submission is performed **only once** to maintain integrity

---

## 👨‍💻 Author

**Rohit Choudhary**
