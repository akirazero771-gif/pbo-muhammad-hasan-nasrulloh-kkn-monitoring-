-- PERHATIAN: skrip ini menghapus database simon_kkn beserta seluruh isinya.
-- Jalankan hanya jika memang ingin mulai dari database kosong.
DROP DATABASE IF EXISTS simon_kkn;

CREATE DATABASE simon_kkn
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE simon_kkn;

CREATE TABLE kelompok (
    id INT NOT NULL AUTO_INCREMENT,
    nama VARCHAR(100) NOT NULL,
    lokasi VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_kelompok_nama (nama)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dosen (
    id INT NOT NULL AUTO_INCREMENT,
    nidn VARCHAR(32) NULL,
    nama VARCHAR(150) NOT NULL,
    email VARCHAR(150) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_dosen_nidn (nidn),
    UNIQUE KEY uq_dosen_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mahasiswa (
    id INT NOT NULL AUTO_INCREMENT,
    nim VARCHAR(64) NOT NULL,
    nama VARCHAR(150) NOT NULL,
    kelompok_id INT NULL,
    prodi VARCHAR(150) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_mahasiswa_nim (nim),
    CONSTRAINT fk_mahasiswa_kelompok
        FOREIGN KEY (kelompok_id) REFERENCES kelompok(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE kegiatan (
    id INT NOT NULL AUTO_INCREMENT,
    tanggal DATE NOT NULL,
    nim VARCHAR(64) NOT NULL,
    nama_kegiatan VARCHAR(200) NOT NULL,
    deskripsi TEXT NULL,
    status VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_kegiatan_tanggal (tanggal),
    CONSTRAINT fk_kegiatan_mahasiswa
        FOREIGN KEY (nim) REFERENCES mahasiswa(nim)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE penilaian (
    id INT NOT NULL AUTO_INCREMENT,
    nim VARCHAR(64) NOT NULL,
    kehadiran DECIMAL(5,2) NOT NULL DEFAULT 0,
    disiplin DECIMAL(5,2) NOT NULL DEFAULT 0,
    program_kerja DECIMAL(5,2) NOT NULL DEFAULT 0,
    laporan DECIMAL(5,2) NOT NULL DEFAULT 0,
    kerja_sama DECIMAL(5,2) NOT NULL DEFAULT 0,
    tanggung_jawab DECIMAL(5,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_penilaian_nim (nim),
    CONSTRAINT chk_penilaian_rentang CHECK (
        kehadiran BETWEEN 0 AND 100
        AND disiplin BETWEEN 0 AND 100
        AND program_kerja BETWEEN 0 AND 100
        AND laporan BETWEEN 0 AND 100
        AND kerja_sama BETWEEN 0 AND 100
        AND tanggung_jawab BETWEEN 0 AND 100
    ),
    CONSTRAINT fk_penilaian_mahasiswa
        FOREIGN KEY (nim) REFERENCES mahasiswa(nim)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE akun (
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('DOSEN', 'MAHASISWA') NOT NULL,
    dosen_id INT NULL,
    nim VARCHAR(64) NULL,
    PRIMARY KEY (username),
    UNIQUE KEY uq_akun_dosen (dosen_id),
    UNIQUE KEY uq_akun_nim (nim),
    CONSTRAINT chk_akun_role_relasi CHECK (
        (role = 'DOSEN' AND dosen_id IS NOT NULL AND nim IS NULL)
        OR (role = 'MAHASISWA' AND dosen_id IS NULL AND nim IS NOT NULL)
    ),
    CONSTRAINT fk_akun_dosen
        FOREIGN KEY (dosen_id) REFERENCES dosen(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_akun_mahasiswa
        FOREIGN KEY (nim) REFERENCES mahasiswa(nim)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabel sengaja kosong. Buat akun dosen pertama melalui aplikasi;
-- data mahasiswa, kelompok, kegiatan, dan nilai dapat dimasukkan setelah login.
