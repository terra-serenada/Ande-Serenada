# Ande Serenada — paket repository lengkap

Paket ini menyiapkan ulang seluruh repository Ande Serenada untuk GitHub Pages dan APK Android.

## Isi utama

- `index.html`: aplikasi web saat ini (fitur pembukuan dan panel yang sudah ada dipertahankan).
- `favicon*`, `apple-touch-icon.png`, `icon-*.png`, `site.webmanifest`: ikon web/PWA.
- `android/`: proyek Android WebView lengkap dengan ikon launcher adaptif yang diperbaiki.
- `.github/workflows/build-apk.yml`: workflow build APK dengan pemasangan Android SDK yang benar.
- `.nojekyll`: file GitHub Pages.
- `brand-assets/`: salinan logo asli untuk aset sumber.

## Cara upload seluruh paket

1. Simpan ZIP ini sebagai cadangan sebelum mengunggah.
2. Ekstrak ZIP di komputer sehingga terlihat `index.html`, `.nojekyll`, folder `android`, dan folder `.github` pada tingkat paling atas.
3. Di GitHub, buka repository `terra-serenada/Ande-Serenada` dan branch `main`.
4. Pilih **Add file → Upload files**.
5. Unggah seluruh isi folder hasil ekstrak (jangan unggah folder pembungkus ZIP sebagai satu folder).
6. Commit ke branch `main`.
7. Karena GitHub Upload tidak menghapus file lama yang tidak ada dalam paket, setelah upload hapus file duplikat lama berikut secara manual jika masih ada: `.github/workflows/Ande-Serenada-build-apk-FIXED.yml`. Pertahankan hanya `.github/workflows/build-apk.yml` agar build tidak berjalan dua kali.

## Ikon Android

Ikon adaptif menggunakan foreground dengan ruang kosong yang cukup pada tepi, sehingga logo berada di safe zone Android. Layer monochrome sengaja tidak dipakai agar launcher Android/One UI tidak mengubah logo warna menjadi ikon tema satu warna. PNG density fallback juga diberi ruang aman.

## GitHub Pages

Buka **Settings → Pages**, pilih **Deploy from a branch**, branch `main`, folder `/(root)`, lalu Save. URL project: `https://terra-serenada.github.io/Ande-Serenada/`.

Untuk menyegarkan shortcut, setelah deployment berhasil hapus shortcut Ande Serenada lama saja, lalu buka URL HTTPS di Samsung Internet dan tambahkan ke layar utama kembali. Jangan hapus data situs/browser.

## APK

Setelah commit, buka tab **Actions**, pilih `Build Ande Serenada APK`, dan periksa hasil build. APK diunggah sebagai artifact `Ande-Serenada-APK` apabila seluruh langkah berhasil.

## Keamanan data

Mengunggah file repository tidak memindahkan ataupun menghapus data `localStorage` yang sudah tersimpan di HP. Jangan bersihkan data/cache browser dan jangan menghapus shortcut lama sebelum ikon baru berhasil dibuat. Cadangkan data pembukuan dalam format JSON sebelum pemulihan atau penggantian data aplikasi.
