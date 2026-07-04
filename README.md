# 🌊 KELELEP: Sistem Manajemen Banjir & Drainase Perkotaan
# KALKULASI EVALUASI LIMPAHAN EKSTRIM LAJU EMPASAN PIPA

![KELELEP Version](https://img.shields.io/badge/version-1.0-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-23-brightgreen.svg)

## 📖 Pengenalan
**KELELEP** adalah aplikasi simulasi tata kelola infrastruktur drainase perkotaan (seperti gorong-gorong beton dan area resapan air). Aplikasi ini dirancang menggunakan arsitektur MVC (Model-View-Controller) dengan antarmuka **JavaFX** yang modern dan elegan. Salah satu keunggulan utama KELELEP adalah integrasinya dengan **Google Gemini AI (2.5 Flash)** yang bertindak sebagai Asisten Cerdas (Advisor) untuk memberikan rekomendasi mitigasi bencana banjir secara interaktif dan *real-time*.

## 🎯 Tujuan
Tujuan utama aplikasi ini adalah untuk membantu insinyur sipil, perencana tata kota, atau mahasiswa dalam:
- Menguji ketahanan kapasitas infrastruktur yang ada terhadap berbagai tingkat curah hujan.
- Memvisualisasikan potensi luapan air (banjir) secara langsung (*real-time*).
- Mendapatkan panduan/tindakan preventif darurat langsung dari AI saat sistem mendeteksi titik luapan.

## ✨ Fitur Utama
1. **Secure Login:** Sistem akses masuk sederhana untuk mengamankan dasbor simulasi.
2. **Manajemen Aset Dinamis (CRUD):** Tambah, lihat, dan hapus data saluran air (*Concrete Pipe*) dan taman resapan (*Absorption Area*).
3. **Impor Database CSV:** Mendukung pembacaan basis data eksternal melalui file format `.csv` dengan parser yang aman dan kebal *crash*.
4. **Live Simulation & Hydraulic Chart:** 
   - Slider pengatur intensitas curah hujan (mm/jam).
   - Animasi BarChart yang membandingkan volume air aktual terhadap kapasitas maksimal masing-masing aset.
   - Peringatan visual instan jika terjadi kelebihan beban (*overflow*).
5. **Smart AI Advisor (WhatsApp Style):** Panel obrolan kekinian di sisi kanan untuk berkonsultasi seputar infrastruktur. Dilengkapi fitur **Konfirmasi Mitigasi**, di mana saat simulasi dihentikan, sistem bisa secara cerdas merangkum data banjir dan memintakan solusi ke Gemini AI.
6. **Ekspor Laporan (PDF):** Cetak laporan lengkap berisi status sistem saat ini, daftar aset yang meluap, dan riwayat log kejadian ke dalam format `.pdf`.

## 📂 Struktur Aplikasi & Arsitektur
Proyek ini mengadopsi pola MVC (Model-View-Controller) untuk memastikan kode tetap rapi dan *scalable*. Berikut adalah gambaran struktur folder utamanya:

```text
📦 KELELEP-Banjir
 ┣ 📂 src/main/java/com/kelelep
 ┃ ┣ 📂 controller            # Mengatur aksi antarmuka (Dashboard, Landing, Login)
 ┃ ┣ 📂 exception             # Custom error handling (KelelepOverflowException)
 ┃ ┣ 📂 model                 # Polimorfisme & entitas aset (ConcretePipe, dll.)
 ┃ ┣ 📂 service               # Mesin logika (SimulationEngine, GeminiService)
 ┃ ┣ 📜 App.java              # Entry point utama (tanpa batasan modul)
 ┃ ┗ 📜 Main.java             # Kelas utama JavaFX Application
 ┣ 📂 src/main/resources
 ┃ ┣ 📂 css                   # Desain estetika (styles.css)
 ┃ ┣ 📂 views                 # Layout antarmuka FXML
 ┃ ┗ 📜 config.properties     # Konfigurasi rahasia (API Key Gemini)
 ┣ 📜 network_template.csv    # Salinan template/database CSV di root
 ┗ 📜 pom.xml                 # Konfigurasi dependensi Maven
```

## 🛠️ Teknologi & Dependensi
- **Bahasa Pemrograman:** Java 25
- **Framework UI:** JavaFX 23.0.2
- **Build Tool:** Apache Maven
- **Library Tambahan:**
  - `com.google.code.gson` (Gson): Menguraikan dan merakit format JSON untuk request/response API Gemini.
  - `com.github.librepdf` (OpenPDF): Digunakan untuk *generate* laporan berbentuk PDF secara native.

## 🚀 Cara Menjalankan Program

### 1. Persiapan Maven & Dependensi
Aplikasi ini dikelola dengan **Apache Maven**, sehingga Anda tidak perlu mengunduh file `.jar` secara manual.
- Pastikan Anda sudah menginstal Java (JDK 25) dan Maven. (Cek dengan perintah `mvn -v` di terminal).
- Maven akan secara otomatis mengunduh library JavaFX, Gson, dan OpenPDF yang terdaftar di dalam `pom.xml` ketika Anda menjalankan perintah *compile*.

### 2. Persiapan API Key
Agar fitur AI berfungsi, Anda memerlukan API Key gratis dari Google AI Studio.
- Buka file `src/main/resources/config.properties`.
- Ganti teks bawaan dengan API key Anda:
  ```properties
  GEMINI_API_KEY=AIzaSy... (paste key Anda di sini)
  ```

### 3. Kompilasi dan Jalankan
Buka terminal/Command Prompt di folder utama proyek (tempat file `pom.xml` berada) dan jalankan:
```bash
# Untuk membersihkan dan mengompilasi ulang proyek
mvn clean compile

# Untuk menjalankan aplikasi JavaFX
mvn javafx:run
```

### 4. Login Akses
Saat program terbuka, gunakan kredensial berikut untuk masuk:
- **Username:** `admin`
- **Password:** `admin123`

### 5. Menyiapkan Data Simulasi (Opsional)
Anda bisa membuat data kustom dengan membuat file CSV. 
- Anda dapat menyalin data template langsung dari [Template Google Sheets Resmi KELELEP](https://docs.google.com/spreadsheets/d/1luL_PyscM4aXokFK8J9nyoIOY8wcgf_lqvYze1f3Uho/edit?usp=sharing) (*Make a Copy* ke Google Drive Anda).
- Atau Anda dapat mengedit file lokal **`network_template.csv`** yang sudah disediakan di folder root aplikasi ini sebagai referensi format tabel yang disyaratkan.
