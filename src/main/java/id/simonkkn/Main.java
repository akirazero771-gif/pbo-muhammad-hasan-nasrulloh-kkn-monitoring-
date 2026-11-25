package id.simonkkn;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;

public class Main extends JFrame {
    private final Color navy = new Color(22, 57, 99);
    private final Color blue = new Color(35, 105, 190);
    private final Color bg = new Color(244, 247, 252);
    private final Database.User user;
    private JTable mahasiswa;
    private JTable kegiatan;
    private JTable nilai;
    private JTextField cari;
    private JLabel total;
    private JLabel rata;
    private JLabel jumlahKegiatan;
    private JLabel lulus;

    public Main(Database.User user) {
        this.user = user;
        setTitle("SIMON-KKN | Sistem Monitoring Nilai dan Kegiatan KKN Mahasiswa");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboard());
        if (user.isLecturer()) {
            tabs.addTab("Data Mahasiswa", mahasiswaPanel());
        }
        tabs.addTab("Monitoring Kegiatan", kegiatanPanel());
        tabs.addTab("Penilaian KKN", nilaiPanel());
        tabs.addTab("Petunjuk Penggunaan", petunjukPanel());
        add(tabs);
        refresh();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(navy);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel title = new JLabel("<html><font color='#D8E8FF' size='4'>UNIVERSITAS HASYIM ASY'ARI</font>"
                + "<br><font color='white' size='6'><b>SIMON-KKN</b></font>"
                + "<br><font color='white'>Sistem Monitoring Nilai dan Kegiatan KKN Mahasiswa</font></html>");
        JLabel role = new JLabel("<html>" + (user.isLecturer() ? "DOSEN" : "MAHASISWA")
                + "<br>" + user.username() + "</html>");
        role.setForeground(Color.WHITE);
        role.setFont(new Font("SansSerif", Font.BOLD, 15));
        panel.add(title);
        panel.add(role, BorderLayout.EAST);
        return panel;
    }

    private JPanel base() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        return panel;
    }

    private JPanel card(String title, JComponent body) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 243)),
                BorderFactory.createEmptyBorder(14, 15, 14, 15)));
        JLabel label = new JLabel(title);
        label.setForeground(navy);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        panel.add(label, BorderLayout.NORTH);
        panel.add(body);
        return panel;
    }

    private JLabel metric() {
        JLabel label = new JLabel("0");
        label.setForeground(navy);
        label.setFont(new Font("SansSerif", Font.BOLD, 27));
        return label;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void style(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(226, 237, 251));
    }

    private JPanel dashboard() {
        JPanel panel = base();
        JPanel metrics = new JPanel(new GridLayout(1, 4, 10, 10));
        metrics.setOpaque(false);
        total = metric();
        rata = metric();
        jumlahKegiatan = metric();
        lulus = metric();
        if (user.isLecturer()) {
            metrics.add(card("TOTAL MAHASISWA", total));
            metrics.add(card("RATA-RATA NILAI", rata));
            metrics.add(card("CATATAN KEGIATAN", jumlahKegiatan));
            metrics.add(card("MAHASISWA LULUS", lulus));
        } else {
            metrics.add(card("NIM", total));
            metrics.add(card("KELOMPOK", rata));
            metrics.add(card("KEGIATAN SAYA", jumlahKegiatan));
            metrics.add(card("STATUS NILAI", lulus));
        }

        String text = user.isLecturer()
                ? "Selamat datang di SIMON-KKN. Anda dapat mengelola data mahasiswa, memantau kegiatan, dan menginput nilai KKN."
                : "Selamat datang di SIMON-KKN. Anda dapat melihat kegiatan dan nilai milik Anda sendiri serta mencatat kegiatan KKN.";
        JTextArea info = new JTextArea(text);
        info.setWrapStyleWord(true);
        info.setLineWrap(true);
        info.setEditable(false);
        info.setFont(new Font("SansSerif", Font.PLAIN, 15));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(metrics, BorderLayout.NORTH);
        panel.add(card("Ringkasan Sistem", info));
        return panel;
    }

    private JPanel mahasiswaPanel() {
        JPanel panel = base();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        cari = new JTextField(20);
        JButton find = button("Cari", blue);
        JButton add = button("Tambah", blue);
        JButton edit = button("Edit", new Color(70, 125, 190));
        JButton delete = button("Hapus", new Color(190, 65, 65));
        top.add(new JLabel("Cari nama / NIM"));
        top.add(cari);
        top.add(find);
        top.add(add);
        top.add(edit);
        top.add(delete);
        mahasiswa = new JTable();
        style(mahasiswa);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(mahasiswa));
        find.addActionListener(event -> loadMahasiswa());
        cari.addActionListener(event -> loadMahasiswa());
        add.addActionListener(event -> dialogMahasiswa(null));
        edit.addActionListener(event -> {
            int row = mahasiswa.getSelectedRow();
            if (row < 0) {
                msg("Pilih data terlebih dahulu.");
            } else {
                dialogMahasiswa(mahasiswa.getValueAt(row, 0).toString());
            }
        });
        delete.addActionListener(event -> hapusMahasiswa());
        return panel;
    }

    private JPanel kegiatanPanel() {
        JPanel panel = base();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton add = button(user.isLecturer() ? "Tambah Kegiatan" : "Catat Kegiatan", blue);
        JButton reload = button("Muat Ulang", new Color(80, 120, 160));
        top.add(add);
        if (user.isLecturer()) {
            JButton delete = button("Hapus Catatan", new Color(190, 65, 65));
            top.add(delete);
            delete.addActionListener(event -> hapusKegiatan());
        }
        top.add(reload);
        kegiatan = new JTable();
        style(kegiatan);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(kegiatan));
        add.addActionListener(event -> dialogKegiatan());
        reload.addActionListener(event -> loadKegiatan());
        return panel;
    }

    private JPanel nilaiPanel() {
        JPanel panel = base();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton reload = button("Muat Ulang", new Color(80, 120, 160));
        if (user.isLecturer()) {
            JButton input = button("Input / Ubah Nilai", blue);
            top.add(input);
            input.addActionListener(event -> dialogNilai());
        }
        top.add(reload);
        nilai = new JTable();
        style(nilai);
        JLabel bobot = new JLabel("Bobot: Kehadiran 15% • Disiplin 15% • Program Kerja 25% • "
                + "Laporan 20% • Kerja Sama 15% • Tanggung Jawab 10%");
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JScrollPane(nilai));
        center.add(bobot, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(center);
        reload.addActionListener(event -> loadNilai());
        return panel;
    }

    private JPanel petunjukPanel() {
        JPanel panel = base();
        Object[][] rows = {
                {"1", "Dashboard", user.isLecturer()
                        ? "Lihat ringkasan seluruh peserta, kegiatan, dan nilai."
                        : "Lihat ringkasan data akun, kegiatan, dan status nilai Anda."},
                {"2", "Data Mahasiswa", "Khusus dosen: tambah, cari, edit, atau hapus data mahasiswa."},
                {"3", "Monitoring Kegiatan", user.isLecturer()
                        ? "Dosen dapat mencatat dan menghapus kegiatan untuk mahasiswa."
                        : "Lihat kegiatan sendiri dan catat kegiatan KKN Anda."},
                {"4", "Penilaian KKN", user.isLecturer()
                        ? "Dosen dapat memasukkan atau mengubah nilai mahasiswa."
                        : "Mahasiswa hanya dapat melihat nilai miliknya sendiri."},
                {"5", "Penyimpanan", "Data tersimpan otomatis di database MySQL simon_kkn."},
                {"6", "Backup", "Gunakan mysqldump untuk mencadangkan database MySQL."},
                {"7", "Menjalankan", "Buka terminal VS Code dan jalankan: mvn clean compile exec:java."}
        };
        JTable table = new JTable(rows, new String[]{"No.", "Menu", "Petunjuk langkah"});
        style(table);
        table.setRowHeight(55);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private DefaultTableModel query(String sql, String... args) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                ResultSetMetaData metadata = result.getMetaData();
                Vector<String> columns = new Vector<>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    columns.add(metadata.getColumnLabel(i));
                }
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                while (result.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= metadata.getColumnCount(); i++) {
                        row.add(result.getObject(i));
                    }
                    model.addRow(row);
                }
                return model;
            }
        }
    }

    private void loadMahasiswa() {
        try {
            String search = "%" + cari.getText().trim() + "%";
            mahasiswa.setModel(query("SELECT m.nim AS NIM,m.nama AS Nama,k.nama AS Kelompok,"
                    + "k.lokasi AS Lokasi,m.prodi AS Program_Studi FROM mahasiswa m "
                    + "LEFT JOIN kelompok k ON k.id=m.kelompok_id "
                    + "WHERE m.nim LIKE ? OR m.nama LIKE ? ORDER BY m.nim", search, search));
        } catch (Exception e) {
            error(e);
        }
    }

    private void loadKegiatan() {
        try {
            if (user.isLecturer()) {
                kegiatan.setModel(query("SELECT id AS ID,tanggal AS Tanggal,nim AS NIM,"
                        + "nama_kegiatan AS Kegiatan,deskripsi AS Deskripsi,status AS Status "
                        + "FROM kegiatan ORDER BY tanggal DESC"));
            } else {
                kegiatan.setModel(query("SELECT tanggal AS Tanggal,nama_kegiatan AS Kegiatan,"
                        + "deskripsi AS Deskripsi,status AS Status FROM kegiatan "
                        + "WHERE nim=? ORDER BY tanggal DESC", user.nim()));
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void loadNilai() {
        String sql = "SELECT m.nim AS NIM,m.nama AS Mahasiswa,"
                + "COALESCE(p.kehadiran,0) AS Kehadiran,COALESCE(p.disiplin,0) AS Disiplin,"
                + "COALESCE(p.program_kerja,0) AS Program_Kerja,COALESCE(p.laporan,0) AS Laporan,"
                + "COALESCE(p.kerja_sama,0) AS Kerja_Sama,COALESCE(p.tanggung_jawab,0) AS Tanggung_Jawab,"
                + "ROUND(COALESCE(p.kehadiran,0)*.15+COALESCE(p.disiplin,0)*.15"
                + "+COALESCE(p.program_kerja,0)*.25+COALESCE(p.laporan,0)*.20"
                + "+COALESCE(p.kerja_sama,0)*.15+COALESCE(p.tanggung_jawab,0)*.10,2) AS Nilai_Akhir,"
                + "CASE WHEN (COALESCE(p.kehadiran,0)*.15+COALESCE(p.disiplin,0)*.15"
                + "+COALESCE(p.program_kerja,0)*.25+COALESCE(p.laporan,0)*.20"
                + "+COALESCE(p.kerja_sama,0)*.15+COALESCE(p.tanggung_jawab,0)*.10)>=85 THEN 'A' "
                + "WHEN (COALESCE(p.kehadiran,0)*.15+COALESCE(p.disiplin,0)*.15"
                + "+COALESCE(p.program_kerja,0)*.25+COALESCE(p.laporan,0)*.20"
                + "+COALESCE(p.kerja_sama,0)*.15+COALESCE(p.tanggung_jawab,0)*.10)>=75 THEN 'B' "
                + "WHEN (COALESCE(p.kehadiran,0)*.15+COALESCE(p.disiplin,0)*.15"
                + "+COALESCE(p.program_kerja,0)*.25+COALESCE(p.laporan,0)*.20"
                + "+COALESCE(p.kerja_sama,0)*.15+COALESCE(p.tanggung_jawab,0)*.10)>=60 THEN 'C' "
                + "ELSE 'D' END AS Grade,CASE WHEN (COALESCE(p.kehadiran,0)*.15"
                + "+COALESCE(p.disiplin,0)*.15+COALESCE(p.program_kerja,0)*.25"
                + "+COALESCE(p.laporan,0)*.20+COALESCE(p.kerja_sama,0)*.15"
                + "+COALESCE(p.tanggung_jawab,0)*.10)>=60 THEN 'Lulus' ELSE 'Evaluasi' END AS Status "
                + "FROM mahasiswa m LEFT JOIN penilaian p ON p.nim=m.nim";
        try {
            if (user.isLecturer()) {
                nilai.setModel(query(sql + " ORDER BY m.nim"));
            } else {
                nilai.setModel(query(sql + " WHERE m.nim=? ORDER BY m.nim", user.nim()));
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void refresh() {
        if (mahasiswa != null) {
            loadMahasiswa();
        }
        if (kegiatan != null) {
            loadKegiatan();
        }
        if (nilai != null) {
            loadNilai();
        }
        try (Connection connection = Database.connect()) {
            if (user.isLecturer()) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM mahasiswa");
                    total.setText(result.next() ? Integer.toString(result.getInt(1)) : "0");
                    result = statement.executeQuery("SELECT AVG(kehadiran*.15+disiplin*.15"
                            + "+program_kerja*.25+laporan*.20+kerja_sama*.15+tanggung_jawab*.10) "
                            + "FROM penilaian");
                    rata.setText(String.format("%.1f", result.next() ? result.getDouble(1) : 0));
                    result = statement.executeQuery("SELECT COUNT(*) FROM kegiatan");
                    jumlahKegiatan.setText(result.next() ? Integer.toString(result.getInt(1)) : "0");
                    result = statement.executeQuery("SELECT COUNT(*) FROM penilaian WHERE "
                            + "kehadiran*.15+disiplin*.15+program_kerja*.25+laporan*.20"
                            + "+kerja_sama*.15+tanggung_jawab*.10>=60");
                    lulus.setText(result.next() ? Integer.toString(result.getInt(1)) : "0");
                }
            } else {
                String sql = "SELECT m.nim,COALESCE(k.nama,'Belum ditentukan'),"
                        + "(SELECT COUNT(*) FROM kegiatan g WHERE g.nim=m.nim),"
                        + "COALESCE((SELECT CASE WHEN kehadiran*.15+disiplin*.15+program_kerja*.25"
                        + "+laporan*.20+kerja_sama*.15+tanggung_jawab*.10>=60 THEN 'Lulus' "
                        + "ELSE 'Evaluasi' END FROM penilaian WHERE nim=m.nim),'Belum dinilai') "
                        + "FROM mahasiswa m LEFT JOIN kelompok k ON k.id=m.kelompok_id WHERE m.nim=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, user.nim());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            total.setText(result.getString(1));
                            rata.setText(result.getString(2));
                            jumlahKegiatan.setText(result.getString(3));
                            lulus.setText(result.getString(4));
                        }
                    }
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private String[] daftarMahasiswa() {
        Vector<String> students = new Vector<>();
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT CONCAT(nim,' - ',nama) FROM mahasiswa ORDER BY nim")) {
            while (result.next()) {
                students.add(result.getString(1));
            }
        } catch (Exception e) {
            error(e);
        }
        return students.toArray(new String[0]);
    }

    private String[] daftarKelompok() {
        Vector<String> groups = new Vector<>();
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT CONCAT(id,' - ',nama) FROM kelompok ORDER BY id")) {
            while (result.next()) {
                groups.add(result.getString(1));
            }
        } catch (Exception e) {
            error(e);
        }
        return groups.toArray(new String[0]);
    }

    private void dialogMahasiswa(String old) {
        if (!user.isLecturer()) {
            return;
        }
        JTextField nim = new JTextField();
        JTextField nama = new JTextField();
        JTextField prodi = new JTextField();
        JComboBox<String> kelompok = new JComboBox<>(daftarKelompok());
        if (old != null) {
            try (Connection connection = Database.connect();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM mahasiswa WHERE nim=?")) {
                statement.setString(1, old);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        nim.setText(result.getString("nim"));
                        nim.setEditable(false);
                        nama.setText(result.getString("nama"));
                        prodi.setText(result.getString("prodi"));
                    }
                }
            } catch (Exception e) {
                error(e);
            }
        }
        JPanel fields = new JPanel(new GridLayout(0, 2, 7, 7));
        fields.add(new JLabel("NIM"));
        fields.add(nim);
        fields.add(new JLabel("Nama"));
        fields.add(nama);
        fields.add(new JLabel("Kelompok"));
        fields.add(kelompok);
        fields.add(new JLabel("Program Studi"));
        fields.add(prodi);
        String title = old == null ? "Tambah Mahasiswa" : "Edit Mahasiswa";
        if (JOptionPane.showConfirmDialog(this, fields, title, JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) {
            return;
        }
        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);
            try {
                String group = (String) kelompok.getSelectedItem();
                String groupId = group == null ? null : group.split(" - ")[0];
                String sql = old == null
                        ? "INSERT INTO mahasiswa(nim,nama,kelompok_id,prodi) VALUES(?,?,?,?)"
                        : "UPDATE mahasiswa SET nama=?,kelompok_id=?,prodi=? WHERE nim=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    if (old == null) {
                        statement.setString(1, nim.getText().trim());
                        statement.setString(2, nama.getText().trim());
                        if (groupId == null) statement.setNull(3, Types.INTEGER);
                        else statement.setInt(3, Integer.parseInt(groupId));
                        statement.setString(4, prodi.getText().trim());
                    } else {
                        statement.setString(1, nama.getText().trim());
                        if (groupId == null) statement.setNull(2, Types.INTEGER);
                        else statement.setInt(2, Integer.parseInt(groupId));
                        statement.setString(3, prodi.getText().trim());
                        statement.setString(4, old);
                    }
                    statement.executeUpdate();
                }
                if (old == null) {
                    Database.ensureStudentAccount(connection, nim.getText().trim());
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
            refresh();
        } catch (Exception e) {
            error(e);
        }
    }

    private void hapusMahasiswa() {
        if (!user.isLecturer()) {
            return;
        }
        int row = mahasiswa.getSelectedRow();
        if (row < 0) {
            msg("Pilih mahasiswa terlebih dahulu.");
            return;
        }
        String nim = mahasiswa.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Hapus " + nim + " beserta catatan dan nilainya?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection connection = Database.connect();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM mahasiswa WHERE nim=?")) {
                statement.setString(1, nim);
                statement.executeUpdate();
                refresh();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void dialogKegiatan() {
        String[] students = user.isLecturer() ? daftarMahasiswa() : new String[0];
        if (user.isLecturer() && students.length == 0) {
            msg("Tambahkan mahasiswa dahulu.");
            return;
        }
        JComboBox<String> selectedStudent = user.isLecturer() ? new JComboBox<>(students) : null;
        JComboBox<String> status = new JComboBox<>(new String[]{"Terlaksana", "Dalam Proses", "Menunggu"});
        JTextField tanggal = new JTextField(java.time.LocalDate.now().toString());
        JTextField nama = new JTextField();
        JTextField deskripsi = new JTextField();
        JPanel fields = new JPanel(new GridLayout(0, 2, 7, 7));
        if (user.isLecturer()) {
            fields.add(new JLabel("Mahasiswa"));
            fields.add(selectedStudent);
        }
        fields.add(new JLabel("Tanggal YYYY-MM-DD"));
        fields.add(tanggal);
        fields.add(new JLabel("Nama Kegiatan"));
        fields.add(nama);
        fields.add(new JLabel("Deskripsi"));
        fields.add(deskripsi);
        fields.add(new JLabel("Status"));
        fields.add(status);
        if (JOptionPane.showConfirmDialog(this, fields, "Tambah Kegiatan", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) {
            return;
        }
        String nim = user.isLecturer()
                ? selectedStudent.getSelectedItem().toString().split(" - ")[0] : user.nim();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO kegiatan(tanggal,nim,nama_kegiatan,deskripsi,status) VALUES(?,?,?,?,?)")) {
            statement.setString(1, tanggal.getText().trim());
            statement.setString(2, nim);
            statement.setString(3, nama.getText().trim());
            statement.setString(4, deskripsi.getText().trim());
            statement.setString(5, status.getSelectedItem().toString());
            statement.executeUpdate();
            refresh();
        } catch (Exception e) {
            error(e);
        }
    }

    private void hapusKegiatan() {
        if (!user.isLecturer()) {
            return;
        }
        int row = kegiatan.getSelectedRow();
        if (row < 0) {
            msg("Pilih catatan kegiatan.");
            return;
        }
        int id = Integer.parseInt(kegiatan.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Hapus catatan ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection connection = Database.connect();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM kegiatan WHERE id=?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
                refresh();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void dialogNilai() {
        if (!user.isLecturer()) {
            return;
        }
        String[] students = daftarMahasiswa();
        if (students.length == 0) {
            msg("Tambahkan mahasiswa dahulu.");
            return;
        }
        JComboBox<String> selectedStudent = new JComboBox<>(students);
        String[] labels = {"Kehadiran 15%", "Disiplin 15%", "Program Kerja 25%",
                "Laporan 20%", "Kerja Sama 15%", "Tanggung Jawab 10%"};
        JTextField[] fields = new JTextField[6];
        JPanel panel = new JPanel(new GridLayout(0, 2, 7, 7));
        panel.add(new JLabel("Mahasiswa"));
        panel.add(selectedStudent);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new JTextField("80");
            panel.add(new JLabel(labels[i]));
            panel.add(fields[i]);
        }
        selectedStudent.addActionListener(event -> isiNilai(selectedStudent, fields));
        isiNilai(selectedStudent, fields);
        if (JOptionPane.showConfirmDialog(this, panel, "Input / Ubah Nilai",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO penilaian(nim,kehadiran,disiplin,program_kerja,laporan,kerja_sama,tanggung_jawab) "
                             + "VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
                             + "kehadiran=VALUES(kehadiran),disiplin=VALUES(disiplin),"
                             + "program_kerja=VALUES(program_kerja),laporan=VALUES(laporan),"
                             + "kerja_sama=VALUES(kerja_sama),tanggung_jawab=VALUES(tanggung_jawab)")) {
            statement.setString(1, selectedStudent.getSelectedItem().toString().split(" - ")[0]);
            for (int i = 0; i < fields.length; i++) {
                double score = Double.parseDouble(fields[i].getText().trim());
                if (score < 0 || score > 100) {
                    throw new IllegalArgumentException("Nilai harus antara 0 sampai 100.");
                }
                statement.setDouble(i + 2, score);
            }
            statement.executeUpdate();
            refresh();
        } catch (Exception e) {
            error(e);
        }
    }

    private void isiNilai(JComboBox<String> student, JTextField[] fields) {
        String nim = student.getSelectedItem().toString().split(" - ")[0];
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM penilaian WHERE nim=?")) {
            statement.setString(1, nim);
            try (ResultSet result = statement.executeQuery()) {
                boolean found = result.next();
                String[] keys = {"kehadiran", "disiplin", "program_kerja", "laporan", "kerja_sama", "tanggung_jawab"};
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setText(found ? result.getString(keys[i]) : "80");
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void msg(String text) {
        JOptionPane.showMessageDialog(this, text, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    private static Database.User login() {
        while (true) {
            JTextField username = new JTextField(18);
            JPasswordField password = new JPasswordField(18);
            JPanel fields = new JPanel(new GridLayout(0, 2, 7, 7));
            fields.add(new JLabel("Username"));
            fields.add(username);
            fields.add(new JLabel("Kata sandi"));
            fields.add(password);
            int result = JOptionPane.showConfirmDialog(null, fields, "Masuk SIMON-KKN",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            char[] passwordChars = password.getPassword();
            try {
                Database.User user = Database.authenticate(username.getText(), passwordChars);
                if (user != null) {
                    return user;
                }
                JOptionPane.showMessageDialog(null, "Username atau kata sandi salah.",
                        "Login gagal", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Kesalahan database",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        }
    }

    private static boolean setupFirstLecturer() {
        while (true) {
            JTextField nidn = new JTextField(18);
            JTextField name = new JTextField(18);
            JTextField email = new JTextField(18);
            JTextField username = new JTextField(18);
            JPasswordField password = new JPasswordField(18);
            JPasswordField confirmPassword = new JPasswordField(18);
            JPanel fields = new JPanel(new GridLayout(0, 2, 7, 7));
            fields.add(new JLabel("NIDN (opsional)"));
            fields.add(nidn);
            fields.add(new JLabel("Nama dosen"));
            fields.add(name);
            fields.add(new JLabel("Email (opsional)"));
            fields.add(email);
            fields.add(new JLabel("Username"));
            fields.add(username);
            fields.add(new JLabel("Kata sandi (minimal 8 karakter)"));
            fields.add(password);
            fields.add(new JLabel("Ulangi kata sandi"));
            fields.add(confirmPassword);
            int choice = JOptionPane.showConfirmDialog(null, fields, "Buat Akun Dosen Pertama",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return false;
            }

            char[] passwordChars = password.getPassword();
            char[] confirmation = confirmPassword.getPassword();
            try {
                if (name.getText().isBlank() || username.getText().isBlank()) {
                    JOptionPane.showMessageDialog(null, "Nama dosen dan username wajib diisi.",
                            "Data belum lengkap", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                if (passwordChars.length < 8) {
                    JOptionPane.showMessageDialog(null, "Kata sandi harus minimal 8 karakter.",
                            "Kata sandi tidak valid", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                if (!Arrays.equals(passwordChars, confirmation)) {
                    JOptionPane.showMessageDialog(null, "Konfirmasi kata sandi tidak cocok.",
                            "Kata sandi tidak cocok", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                Database.createFirstLecturer(nidn.getText(), name.getText(), email.getText(),
                        username.getText(), passwordChars);
                return true;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Gagal membuat akun dosen",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(passwordChars, '\0');
                Arrays.fill(confirmation, '\0');
            }
        }
    }

    public static void main(String[] args) {
        try {
            Database.init();
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                message += "\n\n" + e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(null, message, "Koneksi MySQL gagal",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                if (!Database.hasLecturerAccount() && !setupFirstLecturer()) {
                    return;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Gagal membaca akun dosen",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Database.User user = login();
            if (user != null) {
                new Main(user).setVisible(true);
            }
        });
    }
}
