# Ande Serenada

Aplikasi pembukuan penjualan makanan — versi web dan Android.

## Isi repositori

- `index.html` — halaman utama yang langsung kompatibel dengan GitHub Pages.
- `android/` — proyek Android berbasis WebView.
- `.github/workflows/build-apk.yml` — otomatis membangun APK melalui GitHub Actions.

## Menayangkan aplikasi web dengan GitHub Pages

1. Buka **Settings → Pages** pada repositori GitHub.
2. Pada **Build and deployment**, pilih **Deploy from a branch**.
3. Pilih branch `main` dan folder `/(root)`, lalu tekan **Save**.
4. Tunggu proses deployment selesai. GitHub akan menyediakan alamat Pages untuk repositori ini.

File `index.html` sudah berada di akar repositori sehingga menjadi halaman awal GitHub Pages. Aplikasi tidak memakai pustaka eksternal; logo sudah tertanam di dalam HTML.

## Membuat APK Android

Setiap kali `index.html` atau folder `android/` berubah dan di-push ke branch `main`, GitHub Actions akan menjalankan build APK.

1. Buka tab **Actions**.
2. Pilih workflow **Build Ande Serenada APK**.
3. Tunggu hingga status **Success**.
4. Buka hasil workflow dan unduh artifact **Ande-Serenada-APK**.
5. Ekstrak artifact, lalu pasang `app-debug.apk` pada HP Android.

Workflow menyalin `index.html` terbaru ke aset Android sebelum proses kompilasi, sehingga versi web dan APK menggunakan sumber HTML yang sama.

## Data dan privasi

Data pembukuan disimpan secara lokal di browser/perangkat melalui `localStorage`. Data tidak otomatis tersinkron antar-HP dan tidak disimpan di GitHub. Gunakan fitur cadangan JSON untuk memindahkan data antarperangkat.

## Catatan

- APK debug ditujukan untuk pemasangan langsung dan pengujian.
- Untuk menerbitkan aplikasi di Play Store, siapkan konfigurasi signing rilis dan keystore milik pemilik aplikasi.
- Mengaktifkan GitHub Pages tidak membuat data pembukuan pengguna menjadi publik; yang dipublikasikan hanya file aplikasi, bukan isi `localStorage` pada perangkat.
