package Persistance.dao;

import Persistance.models.Seance;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceDAO {
    private static Connection conn = ConxDB.getInstance();

    public static void saveSeance(Seance seance) {
        String sql = "INSERT INTO seance_conduite (date_time, location, latitude, longitude, localisation, moniteur_id, candidat_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, seance.getDateTime());
                pstmt.setString(2, seance.getLocation());
                pstmt.setDouble(3, seance.getLatitude());
                pstmt.setDouble(4, seance.getLongitude());
                pstmt.setString(5, seance.getLocalisation());
                pstmt.setLong(6, seance.getMoniteurId());
                pstmt.setLong(7, seance.getCandidatId());

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Échec de l'enregistrement de la séance de conduite.");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la séance de conduite", e);
        }
    }

    public static void saveSeanceCode(Seance seance) {
        String sql = "INSERT INTO seance_code (date_time, moniteur_id, candidat_id) VALUES (?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, seance.getDateTime());
                pstmt.setLong(2, seance.getMoniteurId());
                pstmt.setLong(3, seance.getCandidatId());

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Échec de l'enregistrement de la séance de code.");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la séance de code", e);
        }
    }

    public static void updateSeance(Seance seance, Timestamp originalDateTime) throws SQLException {
        String sql = "UPDATE seance_conduite SET date_time = ?, location = ?, latitude = ?, longitude = ?, localisation = ?, moniteur_id = ? WHERE candidat_id = ? AND date_time = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, seance.getDateTime());
                pstmt.setString(2, seance.getLocation());
                pstmt.setDouble(3, seance.getLatitude());
                pstmt.setDouble(4, seance.getLongitude());
                pstmt.setString(5, seance.getLocalisation());
                pstmt.setLong(6, seance.getMoniteurId());
                pstmt.setLong(7, seance.getCandidatId());
                pstmt.setTimestamp(8, originalDateTime);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Aucune séance de conduite trouvée pour candidat_id " + seance.getCandidatId() + " et date_time " + originalDateTime);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la mise à jour de la séance de conduite", e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public static void updateSeanceCode(Seance seance, Timestamp originalDateTime) throws SQLException {
        String sql = "UPDATE seance_code SET date_time = ?, moniteur_id = ? WHERE candidat_id = ? AND date_time = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, seance.getDateTime());
                pstmt.setLong(2, seance.getMoniteurId());
                pstmt.setLong(3, seance.getCandidatId());
                pstmt.setTimestamp(4, originalDateTime);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Aucune séance de code trouvée pour candidat_id " + seance.getCandidatId() + " et date_time " + originalDateTime);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la mise à jour de la séance de code", e);
        }
    }

    public static void deleteSeanceConduite(Long candidatId, Timestamp dateTime) throws SQLException {
        String sql = "DELETE FROM seance_conduite WHERE candidat_id = ? AND date_time = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, candidatId);
                pstmt.setTimestamp(2, dateTime);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Aucune séance de conduite trouvée pour candidat_id " + candidatId + " et date_time " + dateTime);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la suppression de la séance de conduite", e);
        }
    }

    public static void deleteSeanceCode(Long candidatId, Timestamp dateTime) throws SQLException {
        String sql = "DELETE FROM seance_code WHERE candidat_id = ? AND date_time = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, candidatId);
                pstmt.setTimestamp(2, dateTime);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    throw new SQLException("Aucune séance de code trouvée pour candidat_id " + candidatId + " et date_time " + dateTime);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la suppression de la séance de code", e);
        }
    }

    public static List<Seance> getSeancesByDateAndVehicule(Timestamp dateTime, String vehicule) {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance_conduite WHERE date_time = ? AND location = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, dateTime);
            stmt.setString(2, vehicule);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp date = rs.getTimestamp("date_time");
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    String localisation = rs.getString("localisation");
                    long moniteurId = rs.getLong("moniteur_id");
                    long candidatId = rs.getLong("candidat_id");

                    Seance seance = new Seance(date, location, latitude, longitude, localisation, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des séances par date et véhicule", e);
        }
        return seances;
    }

    public static List<Seance> getSeancesByDate(Timestamp dateTime) {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance_code WHERE date_time = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, dateTime);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp date = rs.getTimestamp("date_time");
                    long moniteurId = rs.getLong("moniteur_id");
                    long candidatId = rs.getLong("candidat_id");

                    Seance seance = new Seance(date, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des séances par date", e);
        }
        return seances;
    }

    public static List<Seance> findAll() {
        String query = "SELECT * FROM seance_conduite";
        List<Seance> seances = new ArrayList<>();

        try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Timestamp dateTime = rs.getTimestamp("date_time");
                String location = rs.getString("location");
                double latitude = rs.getDouble("latitude");
                double longitude = rs.getDouble("longitude");
                String localisation = rs.getString("localisation");
                long moniteurId = rs.getLong("moniteur_id");
                long candidatId = rs.getLong("candidat_id");

                Seance seance = new Seance(dateTime, location, latitude, longitude, localisation, moniteurId, candidatId);
                seances.add(seance);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les séances de conduite", e);
        }
        return seances;
    }

    public static List<Seance> findAllCode() {
        String query = "SELECT * FROM seance_code";
        List<Seance> seances = new ArrayList<>();

        try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp dateTime = rs.getTimestamp("date_time");
                long moniteurId = rs.getLong("moniteur_id");
                long candidatId = rs.getLong("candidat_id");

                Seance seance = new Seance(dateTime, moniteurId, candidatId);
                seances.add(seance);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les séances de code", e);
        }
        return seances;
    }

    public static Seance getSeanceByCandidatId(long candidatId) throws SQLException {
        String query = "SELECT * FROM seance_conduite WHERE candidat_id = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, candidatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    String localisation = rs.getString("localisation");
                    long moniteurId = rs.getLong("moniteur_id");

                    return new Seance(dateTime, location, latitude, longitude, localisation, moniteurId, candidatId);
                }
            }
        }
        return null;
    }

    public static Seance getSeanceCodeByCandidatId(long candidatId) throws SQLException {
        String query = "SELECT * FROM seance_code WHERE candidat_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, candidatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    long moniteurId = rs.getLong("moniteur_id");

                    return new Seance(dateTime, moniteurId, candidatId);
                }
            }
        }
        return null;
    }

    public static List<Seance> getSeancesByMoniteurId(long moniteurId) {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance_conduite WHERE moniteur_id = ?";

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, moniteurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    String localisation = rs.getString("localisation");
                    long candidatId = rs.getLong("candidat_id");

                    Seance seance = new Seance(dateTime, location, latitude, longitude, localisation, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des séances par moniteur", e);
        }
        return seances;
    }

    public static List<Seance> getSeancesCodeByMoniteurId(long moniteurId) {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance_code WHERE moniteur_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, moniteurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    long candidatId = rs.getLong("candidat_id");

                    Seance seance = new Seance(dateTime, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des séances de code par moniteur", e);
        }
        return seances;
    }

    public List<Seance> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance_conduite WHERE date_time BETWEEN ? AND ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    String localisation = rs.getString("localisation");
                    long moniteurId = rs.getLong("moniteur_id");
                    long candidatId = rs.getLong("candidat_id");

                    Seance seance = new Seance(dateTime, location, latitude, longitude, localisation, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des séances par plage de dates", e);
        }
        return seances;
    }

    public static boolean rechercherSeanceEffectue(Timestamp horaire, long candidatId) {
        String sql = "SELECT COUNT(*) FROM seance_effectue WHERE date_time = ? AND candidat_id = ?";
        boolean existe = false;

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, horaire);
            pstmt.setLong(2, candidatId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    existe = (count > 0);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la séance effectuée", e);
        }

        return existe;
    }

    public static void insererSeanceEffectue(Timestamp horaire, long candidatId) {
        String sql = "INSERT INTO seance_effectue (date_time, candidat_id) VALUES (?, ?)";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, horaire);
            pstmt.setLong(2, candidatId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de la séance effectuée", e);
        }
    }

    public static List<Seance> getAllSeancesCodeByCandidatId(long candidatId) throws SQLException {
        String query = "SELECT * FROM seance_code WHERE candidat_id = ?";
        List<Seance> seances = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, candidatId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    long moniteurId = rs.getLong("moniteur_id");

                    Seance seance = new Seance(dateTime, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        }
        return seances;
    }

    public static List<Seance> getAllSeancesByCandidatId(long candidatId) throws SQLException {
        String query = "SELECT * FROM seance_conduite WHERE candidat_id = ?";
        List<Seance> seances = new ArrayList<>();

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, candidatId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp dateTime = rs.getTimestamp("date_time");
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    String localisation = rs.getString("localisation");
                    long moniteurId = rs.getLong("moniteur_id");

                    Seance seance = new Seance(dateTime, location, latitude, longitude, localisation, moniteurId, candidatId);
                    seances.add(seance);
                }
            }
        }
        return seances;
    }

    public static List<Seance> filterConduiteByThisWeek() throws SQLException {
        String query = "SELECT * FROM seance_conduite WHERE date_time BETWEEN ? AND ?";
        List<Seance> seances = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDate today = now.toLocalDate();
        LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate sunday = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = monday.atStartOfDay();
        if (startDateTime.isBefore(now)) {
            startDateTime = now;
        }
        Timestamp startOfWeek = Timestamp.valueOf(startDateTime);
        Timestamp endOfWeek = Timestamp.valueOf(sunday.atTime(23, 59, 59));

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, startOfWeek);
            stmt.setTimestamp(2, endOfWeek);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seance seance = new Seance(
                            rs.getTimestamp("date_time"),
                            rs.getString("location"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude"),
                            rs.getString("localisation"),
                            rs.getLong("moniteur_id"),
                            rs.getLong("candidat_id")
                    );
                    seance.setType("CONDUITE");
                    seances.add(seance);
                }
            }
        }
        return seances;
    }

    public static List<Seance> filterCodeByThisWeek() throws SQLException {
        String query = "SELECT * FROM seance_code WHERE date_time BETWEEN ? AND ?";
        List<Seance> seances = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDate today = now.toLocalDate();
        LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate sunday = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = monday.atStartOfDay();
        if (startDateTime.isBefore(now)) {
            startDateTime = now;
        }
        Timestamp startOfWeek = Timestamp.valueOf(startDateTime);
        Timestamp endOfWeek = Timestamp.valueOf(sunday.atTime(23, 59, 59));

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, startOfWeek);
            stmt.setTimestamp(2, endOfWeek);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seance seance = new Seance(
                            rs.getTimestamp("date_time"),
                            rs.getLong("moniteur_id"),
                            rs.getLong("candidat_id")
                    );
                    seance.setType("CODE");
                    seances.add(seance);
                }
            }
        }
        return seances;
    }

    public Optional<Seance> findById(Long id) {
        return Optional.empty();
    }
}