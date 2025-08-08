package Persistance.dao;

import Persistance.models.Moniteur;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoniteurDAO {

    private static Connection conn = ConxDB.getInstance();

    public static List<Moniteur> findAll() {
        List<Moniteur> moniteurs = new ArrayList<>();
        String sql = "SELECT * FROM moniteur";
        String sqlCategories = "SELECT nomCategorie FROM " +
                "categoriemoniteur ca INNER JOIN categorie c ON c.idCategorie = ca.idCategorie WHERE cin = ?";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int cin = rs.getInt("cin");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                int tel = rs.getInt("tel");
                float salaire = rs.getFloat("salaire");

                Moniteur moniteur = new Moniteur(cin, nom, prenom, tel, salaire);

                // Récupérer les catégories du moniteur
                try (PreparedStatement pstmtCategories = conn.prepareStatement(sqlCategories)) {
                    pstmtCategories.setInt(1, cin);
                    try (ResultSet rsCategories = pstmtCategories.executeQuery()) {
                        while (rsCategories.next()) {
                            String categorie = rsCategories.getString("nomCategorie");
                            moniteur.getCategorie().add(categorie.charAt(0));
                        }
                    }
                }

                moniteurs.add(moniteur);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des moniteurs", e);
        }

        return moniteurs;
    }

    public static double getTotalSalaryForMonth(int year, int month) throws SQLException {
        double totalSalary = 0.0;
        String sql = "SELECT salaire FROM moniteur";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                totalSalary += rs.getFloat("salaire");
            }
        }
        return totalSalary;
    }

    public static void delete(int cin) throws SQLException {
        String deleteCategories = "DELETE FROM categoriemoniteur WHERE cin = ?";
        String deleteMoniteur = "DELETE FROM moniteur WHERE cin = ?";

        try {
            conn.setAutoCommit(false);

            // Delete categories first
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCategories)) {
                pstmt.setInt(1, cin);
                pstmt.executeUpdate();
            }

            // Then delete the moniteur
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMoniteur)) {
                pstmt.setInt(1, cin);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Le moniteur avec CIN " + cin + " n'existe pas.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors du rollback de la suppression du moniteur", ex);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors de la réinitialisation de l'auto-commit", ex);
            }
        }
    }

    public static void save(Moniteur moniteur) {
        String sqlMoniteur = "INSERT INTO moniteur (cin, nom, prenom, tel, salaire) VALUES (?, ?, ?, ?, ?)";
        String sqlCategorieId = "SELECT idCategorie FROM categorie WHERE nomCategorie = ?";
        String sqlCategorieMoniteur = "INSERT INTO categoriemoniteur (cin, idCategorie) VALUES (?, ?)";

        try {
            // Étape 1 : Insérer le moniteur dans la table `moniteur`
            try (PreparedStatement pstmtMoniteur = conn.prepareStatement(sqlMoniteur)) {
                pstmtMoniteur.setInt(1, moniteur.getCin());
                pstmtMoniteur.setString(2, moniteur.getNom());
                pstmtMoniteur.setString(3, moniteur.getPrenom());
                pstmtMoniteur.setInt(4, moniteur.getTel());
                pstmtMoniteur.setFloat(5, moniteur.getSalaire());

                int rowsAffected = pstmtMoniteur.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion du moniteur : aucune ligne affectée.");
                }
            }

            // Étape 2 : Insérer les associations dans la table `categoriemoniteur`
            for (Character nomCategorie : moniteur.getCategorie()) {
                try (PreparedStatement pstmtCategorieId = conn.prepareStatement(sqlCategorieId)) {
                    pstmtCategorieId.setString(1, String.valueOf(nomCategorie));
                    try (ResultSet rsCategorie = pstmtCategorieId.executeQuery()) {
                        if (rsCategorie.next()) {
                            int categorieId = rsCategorie.getInt("idCategorie");

                            try (PreparedStatement pstmtCategorieMoniteur = conn.prepareStatement(sqlCategorieMoniteur)) {
                                pstmtCategorieMoniteur.setInt(1, moniteur.getCin());
                                pstmtCategorieMoniteur.setInt(2, categorieId);
                                pstmtCategorieMoniteur.executeUpdate();
                            }
                        } else {
                            throw new SQLException("Catégorie non trouvée : " + nomCategorie);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du moniteur", e);
        }
    }

    public static void update(Moniteur moniteur) throws SQLException {
        String updateMoniteur = "UPDATE moniteur SET nom = ?, prenom = ?, tel = ?, salaire = ? WHERE cin = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateMoniteur)) {
                pstmt.setString(1, moniteur.getNom());
                pstmt.setString(2, moniteur.getPrenom());
                pstmt.setInt(3, moniteur.getTel());
                pstmt.setFloat(4, moniteur.getSalaire());
                pstmt.setInt(5, moniteur.getCin());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("La mise à jour du moniteur a échoué, aucun moniteur trouvé avec le CIN: " + moniteur.getCin());
                }
            }

            // Update categories
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM categoriemoniteur WHERE cin = ?")) {
                pstmt.setInt(1, moniteur.getCin());
                pstmt.executeUpdate();
            }

            String sqlCategorieId = "SELECT idCategorie FROM categorie WHERE nomCategorie = ?";
            String sqlCategorieMoniteur = "INSERT INTO categoriemoniteur (cin, idCategorie) VALUES (?, ?)";

            for (Character nomCategorie : moniteur.getCategorie()) {
                try (PreparedStatement pstmtCategorieId = conn.prepareStatement(sqlCategorieId)) {
                    pstmtCategorieId.setString(1, String.valueOf(nomCategorie));
                    try (ResultSet rsCategorie = pstmtCategorieId.executeQuery()) {
                        if (rsCategorie.next()) {
                            int categorieId = rsCategorie.getInt("idCategorie");

                            try (PreparedStatement pstmtCategorieMoniteur = conn.prepareStatement(sqlCategorieMoniteur)) {
                                pstmtCategorieMoniteur.setInt(1, moniteur.getCin());
                                pstmtCategorieMoniteur.setInt(2, categorieId);
                                pstmtCategorieMoniteur.executeUpdate();
                            }
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors du rollback de la mise à jour du moniteur", ex);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors de la réinitialisation de l'auto-commit", ex);
            }
        }
    }

    public static List<Character> getCategoriesByCin(String cin) throws SQLException {
        List<Character> categories = new ArrayList<>();

        String query = "SELECT c.nomCategorie " +
                "FROM categoriemoniteur mc " +
                "JOIN categorie c ON mc.idCategorie = c.idCategorie " +
                "WHERE mc.cin = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("nomCategorie");
                    if (category != null && !category.isEmpty()) {
                        categories.add(category.charAt(0));
                    }
                }
            }
        }

        return categories;
    }




}