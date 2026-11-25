# Petunjuk Penggunaan SIMON-KKN

| No. | Menu | Langkah penggunaan |
|---|---|---|
| 1 | Login | Masukkan username dan kata sandi akun dosen atau mahasiswa. Tutup jendela login untuk keluar sebelum masuk. |
| 2 | Dashboard | Dosen melihat ringkasan seluruh data; mahasiswa hanya melihat ringkasan miliknya. |
| 3 | Data Mahasiswa (dosen) | Tambah, cari, edit, dan hapus data mahasiswa. Akun mahasiswa baru dibuat otomatis. |
| 4 | Monitoring Kegiatan | Dosen dapat mencatat/menghapus kegiatan semua mahasiswa. Mahasiswa hanya melihat dan mencatat kegiatannya sendiri. |
| 5 | Penilaian KKN | Dosen menginput/mengubah nilai. Mahasiswa hanya melihat nilai miliknya sendiri. |
| 6 | Rekap Nilai | Tabel menampilkan komponen, nilai akhir, grade, dan status kelulusan sesuai akses akun. |
| 7 | Penyimpanan dan backup | Data tersimpan di MySQL database `simon_kkn`. Gunakan `mysqldump` untuk backup. |
| 8 | Menjalankan | Setelah MySQL siap dan kredensial diatur, jalankan `mvn clean compile exec:java`. |

## Akun

- Akun dosen pertama dibuat lewat formulir saat aplikasi pertama kali dibuka.
- Akun mahasiswa yang ditambahkan dosen menggunakan NIM sebagai username dan kata sandi awal.
- Tidak ada akun atau data dummy bawaan. Aplikasi belum menyediakan fitur ubah kata sandi.

## Perbedaan akses

| Fitur | Dosen | Mahasiswa |
|---|---|---|
| Dashboard | Semua data | Data sendiri |
| Data mahasiswa | Tambah, cari, ubah, hapus | Tidak tersedia |
| Kegiatan | Lihat dan kelola semua mahasiswa | Lihat dan catat kegiatan sendiri |
| Nilai | Lihat dan input/ubah seluruh nilai | Hanya melihat nilai sendiri |

Database mulai kosong. Dosen dapat mengisi data mahasiswa baru setelah login.

## Rumus nilai akhir
Kehadiran×15% + Disiplin×15% + Program Kerja×25% + Laporan×20% + Kerja Sama×15% + Tanggung Jawab×10%.

| Grade | Rentang |
|---|---:|
| A | 85–100 |
| B | 75–84,99 |
| C | 60–74,99 |
| D | Di bawah 60 |

Bobot dan batas grade merupakan contoh, bukan ketentuan resmi kampus. Minta persetujuan dosen sebelum digunakan untuk penilaian sebenarnya.

## Menyiapkan MySQL

1. Jalankan MySQL Server.
2. Jalankan file `database/simon_kkn_mysql.sql` di MySQL Workbench atau klien MySQL. **Skrip ini menghapus database `simon_kkn` yang sudah ada**, lalu membuat database dan tabel entitas dalam keadaan kosong.
3. Atur kredensial lewat environment variable, bukan di source code. Di PowerShell:
   ```powershell
   $env:SIMON_MYSQL_URL = "jdbc:mysql://localhost:3306/simon_kkn?serverTimezone=UTC"
   $env:SIMON_MYSQL_USER = "root"
   $env:SIMON_MYSQL_PASSWORD = "kata-sandi-MySQL-Anda"
   ```
4. Jalankan aplikasi:
   ```powershell
   mvn clean compile exec:java
   ```

Connector/J sudah terdaftar sebagai dependensi Maven. Seluruh fitur SIMON-KKN menggunakan MySQL. Aplikasi dapat membuat tabel yang belum ada setelah database dibuat; akun dosen pertama harus dibuat melalui formulir. File SQLite lama tidak digunakan atau dimigrasikan.

## Entitas

| Tabel | Isi dan relasi |
|---|---|
| `kelompok` | Nama kelompok dan lokasi; satu kelompok dapat memiliki banyak mahasiswa. |
| `dosen` | Profil dosen: NIDN, nama, dan email. |
| `mahasiswa` | NIM, nama, program studi, dan kelompok. |
| `kegiatan` | Tanggal, mahasiswa pemilik, nama kegiatan, deskripsi, status. |
| `penilaian` | Satu rekap nilai per mahasiswa dan enam komponen nilai. |
| `akun` | Akun login dan peran `DOSEN`/`MAHASISWA`; masing-masing terhubung ke profil dosen atau mahasiswa. |

Untuk backup database:

```powershell
mysqldump -u root -p simon_kkn > simon_kkn_backup.sql
```
