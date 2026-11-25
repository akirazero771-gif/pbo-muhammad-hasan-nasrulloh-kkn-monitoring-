# SIMON-KKN

Sistem Monitoring Nilai dan Kegiatan KKN Mahasiswa — aplikasi desktop Java Swing dengan akses dosen dan mahasiswa, terintegrasi dengan MySQL.

## Kebutuhan

- JDK 17 atau lebih baru
- Maven
- MySQL Server 8.0.16 atau lebih baru
- MySQL Workbench atau klien MySQL untuk menjalankan file SQL

## Menyiapkan database

1. Jalankan MySQL Server.
2. Buka dan jalankan [`database/simon_kkn_mysql.sql`](database/simon_kkn_mysql.sql) melalui MySQL Workbench atau klien MySQL. **Skrip ini menghapus database `simon_kkn` yang sudah ada**, membuat ulang tabel kosong, dan tidak memasukkan data contoh.
3. Atur kredensial MySQL di PowerShell sebelum menjalankan aplikasi:
   ```powershell
   $env:SIMON_MYSQL_URL = "jdbc:mysql://localhost:3306/simon_kkn?serverTimezone=UTC"
   $env:SIMON_MYSQL_USER = "root"
   $env:SIMON_MYSQL_PASSWORD = "kata-sandi-MySQL-Anda"
   ```
   Jika memakai URL dan username default, cukup atur `SIMON_MYSQL_PASSWORD`.
4. Dari folder proyek, jalankan:
   ```powershell
   mvn clean compile exec:java
   ```

Connector/J akan diunduh oleh Maven dari dependensi di `pom.xml`. Aplikasi membuat tabel yang belum ada. Jika belum ada akun dosen, aplikasi meminta data dosen dan kredensial untuk membuat akun pertama. Tidak ada akun atau data dummy bawaan.

## Akun

- **Dosen pertama:** dibuat melalui formulir saat aplikasi dibuka pertama kali.
- **Mahasiswa:** akun otomatis dibuat saat dosen menambahkan mahasiswa; username dan kata sandi awal menggunakan NIM.
- Mahasiswa yang ditambahkan dosen otomatis mendapat akun dengan aturan yang sama.

Kata sandi disimpan sebagai hash PBKDF2. Fitur ubah kata sandi belum tersedia.

## Hak akses

- **Dosen:** melihat seluruh dashboard; mencari, menambah, mengubah, dan menghapus data mahasiswa; melihat serta mengelola kegiatan semua mahasiswa; melihat dan mengubah nilai seluruh mahasiswa.
- **Mahasiswa:** melihat ringkasan, kegiatan, dan nilai miliknya sendiri; mencatat kegiatan untuk dirinya. Mahasiswa tidak dapat membuka data mahasiswa lain, menghapus kegiatan, atau mengubah nilai.

## Entitas database

- `kelompok`: kelompok KKN dan lokasi.
- `dosen`: profil dosen (NIDN, nama, email).
- `mahasiswa`: identitas mahasiswa, program studi, dan kelompok.
- `kegiatan`: catatan kegiatan per mahasiswa.
- `penilaian`: enam komponen nilai per mahasiswa.
- `akun`: username, hash kata sandi, peran, dan relasi ke profil dosen atau mahasiswa.

Semua tabel kosong setelah skrip SQL dijalankan. Foreign key menjaga keterkaitan data; menghapus mahasiswa akan menghapus akun, kegiatan, dan nilainya. Menghapus kelompok hanya mengosongkan relasi kelompok mahasiswa.

## Nilai

Kehadiran 15%, Disiplin 15%, Program Kerja 25%, Laporan 20%, Kerja Sama 15%, Tanggung Jawab 10%.
Grade contoh: A >= 85; B >= 75; C >= 60; D < 60. Sesuaikan dengan pedoman kampus.

## Backup

Gunakan `mysqldump` untuk mencadangkan database:

```powershell
mysqldump -u root -p simon_kkn > simon_kkn_backup.sql
```

Skrip SQL menghapus database MySQL `simon_kkn` yang sudah ada. Pastikan database yang ingin dihapus hanya berisi data lama. File SQLite `simon_kkn.db` tidak digunakan atau diimpor.
