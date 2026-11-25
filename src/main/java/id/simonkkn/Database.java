package id.simonkkn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public record User(String username, String role, String nim) {
        public boolean isLecturer() {
            return "DOSEN".equals(role);
        }
    }

    public static Connection connect() throws SQLException {
        return MySqlConnection.getConnection();
    }

    public static void init() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS kelompok ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "nama VARCHAR(100) NOT NULL UNIQUE,"
                    + "lokasi VARCHAR(255) NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dosen ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "nidn VARCHAR(32) NULL UNIQUE,"
                    + "nama VARCHAR(150) NOT NULL,"
                    + "email VARCHAR(150) NULL UNIQUE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS mahasiswa ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "nim VARCHAR(64) NOT NULL UNIQUE,"
                    + "nama VARCHAR(150) NOT NULL,"
                    + "kelompok_id INT NULL,"
                    + "prodi VARCHAR(150) NULL,"
                    + "CONSTRAINT fk_mahasiswa_kelompok FOREIGN KEY (kelompok_id) "
                    + "REFERENCES kelompok(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS kegiatan ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "tanggal DATE NOT NULL,"
                    + "nim VARCHAR(64) NOT NULL,"
                    + "nama_kegiatan VARCHAR(200) NOT NULL,"
                    + "deskripsi TEXT NULL,"
                    + "status VARCHAR(40) NOT NULL,"
                    + "INDEX idx_kegiatan_tanggal (tanggal),"
                    + "CONSTRAINT fk_kegiatan_mahasiswa FOREIGN KEY (nim) "
                    + "REFERENCES mahasiswa(nim) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS penilaian ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "nim VARCHAR(64) NOT NULL UNIQUE,"
                    + "kehadiran DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "disiplin DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "program_kerja DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "laporan DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "kerja_sama DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "tanggung_jawab DECIMAL(5,2) NOT NULL DEFAULT 0,"
                    + "CONSTRAINT chk_penilaian_rentang CHECK ("
                    + "kehadiran BETWEEN 0 AND 100 AND disiplin BETWEEN 0 AND 100 AND "
                    + "program_kerja BETWEEN 0 AND 100 AND laporan BETWEEN 0 AND 100 AND "
                    + "kerja_sama BETWEEN 0 AND 100 AND tanggung_jawab BETWEEN 0 AND 100),"
                    + "CONSTRAINT fk_penilaian_mahasiswa FOREIGN KEY (nim) "
                    + "REFERENCES mahasiswa(nim) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS akun ("
                    + "username VARCHAR(100) NOT NULL PRIMARY KEY,"
                    + "password_hash VARCHAR(255) NOT NULL,"
                    + "role ENUM('DOSEN','MAHASISWA') NOT NULL,"
                    + "dosen_id INT NULL UNIQUE,"
                    + "nim VARCHAR(64) NULL UNIQUE,"
                    + "CONSTRAINT chk_akun_role_relasi CHECK "
                    + "((role='DOSEN' AND dosen_id IS NOT NULL AND nim IS NULL) OR "
                    + "(role='MAHASISWA' AND dosen_id IS NULL AND nim IS NOT NULL)),"
                    + "CONSTRAINT fk_akun_dosen FOREIGN KEY (dosen_id) "
                    + "REFERENCES dosen(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_akun_mahasiswa FOREIGN KEY (nim) "
                    + "REFERENCES mahasiswa(nim) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyiapkan database MySQL. "
                    + "Pastikan server, database, dan kredensial sudah dikonfigurasi.", e);
        }
    }

    public static boolean hasLecturerAccount() throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM akun WHERE role='DOSEN' LIMIT 1");
             ResultSet result = statement.executeQuery()) {
            return result.next();
        }
    }

    public static void createFirstLecturer(String nidn, String name, String email,
                                           String username, char[] password) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                if (hasLecturerAccount(connection)) {
                    throw new SQLException("Akun dosen sudah tersedia.");
                }
                int lecturerId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO dosen(nidn,nama,email) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, emptyToNull(nidn));
                    insert.setString(2, name.trim());
                    insert.setString(3, emptyToNull(email));
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("ID dosen baru tidak berhasil dibuat.");
                        }
                        lecturerId = keys.getInt(1);
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO akun(username,password_hash,role,dosen_id,nim) "
                                + "VALUES(?,?,'DOSEN',?,NULL)")) {
                    insert.setString(1, username.trim());
                    insert.setString(2, Auth.hash(password));
                    insert.setInt(3, lecturerId);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    static void ensureStudentAccount(Connection connection, String nim) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM akun WHERE nim=?")) {
            query.setString(1, nim);
            try (ResultSet result = query.executeQuery()) {
                if (result.next()) {
                    return;
                }
            }
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM akun WHERE username=?")) {
            query.setString(1, nim);
            try (ResultSet result = query.executeQuery()) {
                if (result.next()) {
                    throw new SQLException("Username mahasiswa sudah digunakan: " + nim);
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO akun(username,password_hash,role,dosen_id,nim) "
                        + "VALUES(?,?,'MAHASISWA',NULL,?)")) {
            insert.setString(1, nim);
            insert.setString(2, Auth.hash(nim.toCharArray()));
            insert.setString(3, nim);
            insert.executeUpdate();
        }
    }

    public static User authenticate(String username, char[] password) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT username,password_hash,role,nim FROM akun WHERE username=?")) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && Auth.verify(password, result.getString("password_hash"))) {
                    return new User(result.getString("username"),
                            result.getString("role"), result.getString("nim"));
                }
            }
        }
        return null;
    }

    private static boolean hasLecturerAccount(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM akun WHERE role='DOSEN' LIMIT 1");
             ResultSet result = statement.executeQuery()) {
            return result.next();
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
