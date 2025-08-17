# Distributed Systems – gRPC & REST Projects

This repository contains two independent Java console applications.

---

## 1. gRPC Book Service Integration
- **Architecture:** gRPC Client, Local Server, external TUB Server
- **Communication:** `clientapi.proto` (Client ↔ Server), `books.proto` (Server ↔ TUB)
- **Auth:** `AuthEncoder.java`
- **Stack:** Java, Gradle, protobuf-gradle-plugin, gRPC

---

## 2. SimpleApiCall – REST Countries & Open-Meteo
- **Purpose:** Retrieves a country’s capital and its coordinates (REST Countries API),  
  then fetches live weather data (temperature, wind speed) for the capital (Open-Meteo API).
- **Implementation:** Java console application with manual JSON parsing (no external libs)
- **APIs:** [REST Countries](https://restcountries.com/), [Open-Meteo](https://open-meteo.com/en/docs)

---

## Documentation
📄 Full documentation: [docs/documentation.pdf](docs/TarasLevankouASHA2.pdf)
EOF