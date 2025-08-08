package Persistance.dao;

import Persistance.models.ExamenInfo;
import Persistance.models.PasserExamen;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PasserExamenDAO {

    private static final Connection conn = ConxDB.getInstance();

    // Méthode pour récupérer tous les enregistrements
    public static List<PasserExamen> findAll() throws SQLException {
        List<PasserExamen> passerExamens = new ArrayList<>();
        String sql = "SELECT pe.*, e.nomExamen, e.prix " +
                "FROM passerExamen pe " +
                "JOIN examen e ON pe.idExamen = e.idExamen";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PasserExamen examen = new PasserExamen(
                        rs.getInt("cinCondidat"),
                        rs.getInt("idExamen"),
                        rs.getTimestamp("dateHeureExamen").toLocalDateTime(),
                        rs.getString("resultat"),
                        rs.getString("nomExamen"),
                        rs.getInt("prix")
                );

                passerExamens.add(examen);
            }
        }
        return passerExamens;
    }

    // Méthode pour insérer un nouvel enregistrement
    public static boolean save(PasserExamen passerExamen) throws SQLException {
        if (passerExamen == null) {
            throw new IllegalArgumentException("L'objet passerExamen ne peut pas être null");
        }

        String sql = "INSERT INTO passerExamen (cinCondidat, idExamen, dateHeureExamen, resultat) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, passerExamen.getCinCondidat());
            pstmt.setInt(2, passerExamen.getIdExamen());
            pstmt.setTimestamp(3, Timestamp.valueOf(passerExamen.getDateExamen()));
            pstmt.setString(4, passerExamen.getResultatExamen());

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        }
    }

    // Méthode pour mettre à jour un enregistrement
    public static boolean update(PasserExamen passerExamen, LocalDateTime oldDateExamen) throws SQLException {
        if (passerExamen == null) {
            throw new IllegalArgumentException("L'objet passerExamen ne peut pas être null");
        }
        if (oldDateExamen == null) {
            throw new IllegalArgumentException("La date actuelle de l'examen ne peut pas être null");
        }

        String sql = "UPDATE passerExamen SET dateHeureExamen = ?, resultat = ? " +
                "WHERE cinCondidat = ? AND idExamen = ? AND dateHeureExamen = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(passerExamen.getDateExamen())); // Nouvelle date/heure
            pstmt.setString(2, passerExamen.getResultatExamen());
            pstmt.setInt(3, passerExamen.getCinCondidat());
            pstmt.setInt(4, passerExamen.getIdExamen());
            pstmt.setTimestamp(5, Timestamp.valueOf(oldDateExamen)); // Ancienne date/heure pour le WHERE

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Méthode pour supprimer un enregistrement
    public static boolean delete(int cinCondidat, int idExamen, LocalDateTime dateExamen) throws SQLException {
        String sql = "DELETE FROM passerExamen WHERE cinCondidat = ? AND idExamen = ? AND dateHeureExamen = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cinCondidat);
            pstmt.setInt(2, idExamen);
            pstmt.setObject(3, dateExamen); // Utilisation de setObject pour LocalDateTime

            return pstmt.executeUpdate() > 0;
        }
    }

    public static ExamenInfo getExamenInfoById(int idExamen) throws SQLException {
        String sql = "SELECT idExamen, nomExamen, prix FROM examen WHERE idExamen = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idExamen);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ExamenInfo(
                            rs.getInt("idExamen"),
                            rs.getString("nomExamen"),
                            rs.getInt("prix")
                    );
                }
            }
        }
        return null;
    }

    public static LocalDateTime getLatestExamenDateTime(int cinCondidat) throws SQLException {
        String sql = "SELECT dateHeureExamen FROM passerExamen " +
                "WHERE cinCondidat = ? " +
                "ORDER BY dateHeureExamen DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cinCondidat);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("dateHeureExamen");
                    return timestamp != null ? timestamp.toLocalDateTime() : null;
                }
            }
        }
        return null;
    }
}