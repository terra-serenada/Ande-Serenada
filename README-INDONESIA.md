# Ande Serenada — Paket GitHub Pages + Android APK

Paket ini berisi aplikasi web Ande Serenada dan proyek Android WebView. File `index.html` adalah sumber aplikasi utama. Workflow GitHub Actions membangun APK Android secara otomatis.

## Isi paket

- `index.html` — aplikasi Ande Serenada versi terbaru (tema gelap, Daftar Menu, panel struk, penyelamatan data).
- `.nojekyll` — menonaktifkan pemrosesan Jekyll pada GitHub Pages.
- `android/` — proyek Android WebView lengkap.
- `.github/workflows/build-apk.yml` — workflow untuk membangun APK.

## Cara reset repository GitHub

1. Pastikan cadangan data aplikasi dari setiap HP sudah disimpan. Mengganti file repository tidak otomatis mencadangkan data lokal pada HP.
2. Unduh dan ekstrak ZIP paket ini di komputer.
3. Buka repository `terra-serenada/Ande-Serenada` di GitHub.
4. Unggah **isi folder hasil ekstrak** ke root repository, bukan folder pembungkus `Ande-Serenada-Reset-GitHub`.
5. Jika ingin benar-benar mengosongkan repository terlebih dahulu, lakukan itu hanya setelah memastikan salinan ZIP ini tersimpan. Cara yang lebih aman adalah mengganti/menimpa file repository dengan seluruh isi paket ini.
6. Pastikan branch tujuan adalah `main` dan commit perubahan.

## Mengaktifkan GitHub Pages

Di Settings → Pages:

- Source: `Deploy from a branch`
- Branch: `main`
- Folder: `/(root)`
- Save

Alamat halaman proyek: `https://terra-serenada.github.io/Ande-Serenada/`

## Membangun APK

1. Setelah paket masuk ke branch `main`, buka tab **Actions**.
2. Pilih workflow **Build Ande Serenada APK**.
3. Jalankan **Run workflow** jika workflow belum berjalan otomatis.
4. Tunggu sampai semua langkah berstatus hijau.
5. Buka hasil run dan unduh artifact **Ande-Serenada-Android-APK**.
6. Ekstrak artifact ZIP untuk mendapatkan `app-debug.apk`, lalu kirim APK ke HP Android lain dan instal.

APK debug dapat dipasang untuk pengujian dan distribusi internal. Android mungkin meminta izin **Install unknown apps** untuk aplikasi yang digunakan membuka APK.

## Catatan data

Aplikasi web menyimpan data secara lokal pada perangkat/browser. Data di HP lama tidak otomatis ikut ke APK atau HP baru. Ekspor JSON dari perangkat lama dan pulihkan melalui menu **Data & Cadangan** pada perangkat tujuan. Buat cadangan sebelum pemulihan karena proses pulihkan mengganti data yang sedang aktif.

Pada APK Android, ekspor file menggunakan pemilih dokumen Android (Save As), sedangkan pemulihan JSON menggunakan pemilih dokumen (Open). Ini menghindari ketergantungan unduhan `blob:` pada browser.

## Identitas aplikasi Android

- Application ID: `id.terraserenada.andeserenada`
- Minimum Android: Android 6.0 (API 23)
- Target SDK: Android 15 (API 35)
- Versi awal: `1.0.0` (debug APK)

Jangan mengubah Application ID jika ingin mempertahankan identitas paket aplikasi pada pembaruan berikutnya.
