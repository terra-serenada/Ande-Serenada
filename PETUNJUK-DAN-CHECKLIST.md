# Patch audit dan perbaikan APK — Ande Serenada

Patch ini disiapkan dari pemeriksaan sumber repository `terra-serenada/Ande-Serenada` pada 8 November 2026. Patch tidak mengubah repository secara langsung.

## Berkas yang perlu diganti

Ekstrak ZIP, lalu unggah/ganti empat berkas berikut di repository dengan path yang sama:

1. `android/build.gradle`
2. `android/app/build.gradle`
3. `android/app/src/main/java/id/terraserenada/andeserenada/MainActivity.java`
4. `.github/workflows/build-apk.yml`

File `index.html` utama tidak diubah. Workflow akan menyalin `index.html` terbaru ke `android/app/src/main/assets/index.html` saat build.

## Mengapa file ini diperbaiki

- `android/build.gradle` dikembalikan menjadi deklarasi plugin induk dengan Android Gradle Plugin `8.13.2`, versi yang digunakan pada build APK terakhir yang terkonfirmasi sukses.
- `versionCodeOverride` dan konfigurasi signing dipindahkan ke `android/app/build.gradle`, yaitu modul yang benar-benar menghasilkan `app-debug.apk`.
- Package id debug dipertahankan sebagai `id.terraserenada.andeserenada.debug`.
- `MainActivity.java` ditambah dengan `saveBase64File(...)` untuk JPG/PDF dan `printReceiptHTML(...)` untuk dialog cetak Android. `saveTextFile(...)` untuk JSON/CSV tetap tersedia.
- Workflow memeriksa empat secret signing, memvalidasi keystore dan alias, membangun `:app:assembleDebug`, lalu memeriksa package id, versionCode, tanda tangan APK dan kecocokan sertifikat dengan keystore.

## Keystore permanen — dibuat satu kali saja

Keystore tidak disertakan dalam ZIP. Buat sendiri dan simpan salinan `.jks` di tempat aman. Jangan unggah file `.jks` atau file Base64 ke repository.

Jalankan pada komputer yang memiliki JDK 17 atau lebih baru:

```bash
keytool -genkeypair -v -storetype JKS \
  -keystore ande-serenada-update.jks \
  -alias ande-serenada \
  -keyalg RSA -keysize 2048 -validity 10000
```

Catat dengan aman:

- password keystore (`store password`)
- alias: `ande-serenada`
- password private key (`key password`)

Untuk menghindari salah ketik, password private key boleh dibuat sama dengan password keystore.

Bila abang hanya memakai HP Android dan sudah menggunakan Termux, keystore juga bisa dibuat langsung di HP:

```bash
pkg update
pkg install openjdk-17 coreutils
mkdir -p ~/ande-signing
cd ~/ande-signing
keytool -genkeypair -v -storetype JKS \
  -keystore ande-serenada-update.jks \
  -alias ande-serenada \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 ande-serenada-update.jks > ande-serenada-update-base64.txt
```

Perintah `openjdk-17` tersedia pada repositori paket Termux saat audit ini; paket tersebut menyertakan `keytool`. Jika Termux tidak mengenali nama paketnya, periksa `pkg search openjdk` sebelum melanjutkan.

Buat string Base64 satu baris:

**PowerShell Windows:**

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("ande-serenada-update.jks")) | Set-Content -NoNewline -Encoding ascii ande-serenada-update-base64.txt
```

**Linux:**

```bash
base64 -w 0 ande-serenada-update.jks > ande-serenada-update-base64.txt
```

Buka repository GitHub → **Settings → Secrets and variables → Actions → New repository secret**. Buat empat secret berikut:

| Nama secret | Nilai |
|---|---|
| `ANDROID_SIGNING_KEYSTORE_BASE64` | seluruh isi file `ande-serenada-update-base64.txt` |
| `ANDROID_SIGNING_STORE_PASSWORD` | password keystore |
| `ANDROID_SIGNING_KEY_ALIAS` | `ande-serenada` |
| `ANDROID_SIGNING_KEY_PASSWORD` | password private key |

Simpan file `.jks` dan password di tempat aman. Keystore yang sama harus dipakai untuk semua update berikutnya. Jika keystore hilang, APK mendatang tidak dapat ditandatangani sebagai update dengan identitas yang sama.

## Urutan aman setelah patch diunggah

1. Pastikan keempat secret tersedia.
2. Jalankan workflow **Build Ande Serenada APK** melalui tab Actions.
3. Pastikan job selesai sukses dan artefak bernama `Ande-Serenada-APK-v1.2.0-build...` tersedia.
4. Jangan menghapus aplikasi lama sebelum artefak baru benar-benar selesai dibangun dan diunduh.
5. Karena APK lama berasal dari signing debug runner sebelumnya, kemungkinan besar instalasi pertama dengan keystore permanen memerlukan uninstall aplikasi lama lalu install APK baru. Cadangan JSON yang sudah abang simpan digunakan untuk memulihkan data setelah instalasi.
6. Setelah APK baru terpasang, uji impor JSON, ekspor JPG, ekspor PDF, dan Cetak sebelum menganggap migrasi selesai.

## Batas verifikasi patch

Patch ini diaudit secara statis terhadap sumber repository dan log build sebelumnya. Lingkungan kerja ini tidak memiliki Android SDK/`android.jar`, jadi kompilasi Android end-to-end belum dapat dijalankan di sini. Workflow yang diperbaiki akan melakukan validasi APK sesungguhnya di GitHub Actions. Jangan menganggap APK baru sudah teruji sebelum run Actions menghasilkan artefak sukses.
