# 📘 Pengajuan KTA Web App

![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Gradle](https://img.shields.io/badge/Gradle-Build%20Tool-blue)

> **Aplikasi Pengajuan KTA** adalah aplikasi web modern berbasis **Java Spring Boot** yang digunakan untuk pelayanan pengajuan *KTA*.  
> Dikembangkan oleh tim IT Developer **Dinas Komunikasi, Informatika, Statistik dan Persandian** untuk memudahkan proses pengajuan *KTA*.

---

## 🚀 Tech Stack

- ☕ **Java 21**
- 🧩 **Spring Boot 3**
- 🐘 **PostgreSQL**
- ⚡ **Redis**
- 🐳 **Docker & Docker Compose**
- ⚙️ **Gradle**
- 📦 **Flyway (Database Migration)**
- 🧠 **AWS SDK (S3 Storage**
---

## 📜 Flyway Migration

src/main/resources/db/migration/


Flyway otomatis berjalan ketika aplikasi start pertama kali.

## 📚 API Documentation

### 👉 Swagger UI Local:

http://localhost:8080/kta/api/swagger-ui.html


👉 OpenAPI JSON:

http://localhost:8080/v3/api-docs

___

## ⚙️ Setup & Run (Local)

### 1️⃣ Clone repository
```bash
git clone https://github.com/bappeda-dev-team/pengajuan_kta.git
cd pengajuan_kta
