package Persistance.dao;

import Persistance.models.Candidat;
import Persistance.utils.ConxDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CandidatDAO {

    // Connexion statique initialisée avec ConxDB.getInstance()
    private static Connection conn = ConxDB.getInstance();

    // Méthode pour ajouter un candidat
    public static void ajouterCandidat(Candidat candidat) throws SQLException {
        String query = "INSERT INTO candidat (cin, nom, prenom, telephone, date_naissance, categorie, etat, seances_effectuees, seances_totales, montant_paye, montant_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, candidat.getCin());
            statement.setString(2, candidat.getNom());
            statement.setString(3, candidat.getPrenom());
            statement.setString(4, candidat.getTelephone());
            statement.setDate(5, candidat.getDate_naissance() != null ? Date.valueOf(candidat.getDate_naissance()) : null);
            statement.setString(6, candidat.getCategorie());
            statement.setString(7, candidat.getEtat());
            statement.setInt(8, candidat.getSeances_effectuees());
            statement.setInt(9, candidat.getSeances_totales());
            statement.setDouble(10, candidat.getMontant_paye());
            statement.setDouble(11, candidat.getMontant_total());
            statement.executeUpdate();
        }
    }

    // Méthode pour récupérer un candidat par CIN
    public static Candidat getCandidat(int cin) throws SQLException {
        String query = "SELECT * FROM candidat WHERE cin = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, cin);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCandidatFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    // Méthode pour récupérer tous les candidats
    public static List<Candidat> getAllCandidats() throws SQLException {
        List<Candidat> candidats = new ArrayList<>();
        String query = "SELECT * FROM candidat";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                candidats.add(extractCandidatFromResultSet(resultSet));
            }
        }
        return candidats;
    }

    // Méthode pour mettre à jour un candidat
    public static void updateCandidat(Candidat candidat) throws SQLException {
        String query = "UPDATE candidat SET nom = ?, prenom = ?, telephone = ?, date_naissance = ?, categorie = ?, etat = ?, seances_effectuees = ?, seances_totales = ?, montant_paye = ?, montant_total = ? WHERE cin = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, candidat.getNom());
            statement.setString(2, candidat.getPrenom());
            statement.setString(3, candidat.getTelephone());
            statement.setDate(4, candidat.getDate_naissance() != null ? Date.valueOf(candidat.getDate_naissance()) : null);
            statement.setString(5, candidat.getCategorie());
            statement.setString(6, candidat.getEtat());
            statement.setInt(7, candidat.getSeances_effectuees());
            statement.setInt(8, candidat.getSeances_totales());
            statement.setDouble(9, candidat.getMontant_paye());
            statement.setDouble(10, candidat.getMontant_total());
            statement.setInt(11, candidat.getCin());
            statement.executeUpdate();
        }
    }

    // Méthode pour supprimer un candidat
    public static void deleteCandidat(int cin) throws SQLException {
        String query = "DELETE FROM candidat WHERE cin = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, cin);
            statement.executeUpdate();
        }
    }

    // Méthode pour extraire un candidat à partir d'un ResultSet
    private static Candidat extractCandidatFromResultSet(ResultSet resultSet) throws SQLException {
        return new Candidat(
                resultSet.getInt("cin"),
                resultSet.getString("nom"),
                resultSet.getString("prenom"),
                resultSet.getString("telephone"),
                resultSet.getDate("date_naissance") != null ? resultSet.getDate("date_naissance").toLocalDate() : null,
                resultSet.getString("categorie"),
                resultSet.getString("etat"),
                resultSet.getInt("seances_effectuees"),
                resultSet.getInt("seances_totales"),
                resultSet.getDouble("montant_paye"),
                resultSet.getDouble("montant_total")
        );
    }
}