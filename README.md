# AOC CLI

A Java CLI to interact with Advent of Code (AoC) programmatically. The CLI handles authentication, input downloading, answer submission, and stats checking.

```bash
./mvnw clean verify
```

### Setup

- Go to https://adventofcode.com and log in
- Open browser developer tools (F12)
- Go to Application/Storage → Cookies → https://adventofcode.com
- Copy the value of the `session` cookie
- Store the value in `.env` with the key `AOC_API_KEY=xxx` or set environment variable `AOC_API_KEY`

### Usage

```bash
./mvnw clean verify
java -jar target/churrera-aoc-client-0.1.0.jar --help
java -jar target/churrera-aoc-client-0.1.0.jar test
java -jar target/churrera-aoc-client-0.1.0.jar pending
java -jar target/churrera-aoc-client-0.1.0.jar pending 2025
java -jar target/churrera-aoc-client-0.1.0.jar problem 2025 1_1
java -jar target/churrera-aoc-client-0.1.0.jar input 2025 1
java -jar target/churrera-aoc-client-0.1.0.jar input submit 1 1 123
java -jar target/churrera-aoc-client-0.1.0.jar stats
```
