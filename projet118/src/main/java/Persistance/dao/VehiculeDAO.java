package Persistance.dao;

import Persistance.models.Vehicule;
import Persistance.models.DocumentVehicule;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {

    private static Connection conn = ConxDB.getInstance();

    public static int save(Vehicule vehicule, List<DocumentVehicule> documents) throws SQLException {
        String sqlVehicule = "INSERT INTO vehicules (marque, modele, immatriculation, annee_fabrication, km_actuel, categorie) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDocument = "INSERT INTO documentsVehicules (id_vehicule, id_type_document, date_echeance, kilometrage_echeance) VALUES (?, ?, ?, ?)";

        int generatedId = -1;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtVehicule = conn.prepareStatement(sqlVehicule, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVehicule.setString(1, vehicule.getMarque());
                pstmtVehicule.setString(2, vehicule.getModele());
                pstmtVehicule.setString(3, vehicule.getImmatriculation());
                pstmtVehicule.setInt(4, vehicule.getAnneeFabrication());
                pstmtVehicule.setInt(5, vehicule.getKmActuel());
                pstmtVehicule.setString(6, String.valueOf(vehicule.getCategorie()));
                int rowsAffected = pstmtVehicule.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion du véhicule : aucune ligne affectée.");
                }

                try (ResultSet generatedKeys = pstmtVehicule.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                        vehicule.setIdVehicule(generatedId);
                    } else {
                        throw new SQLException("Échec de la récupération de l'ID généré.");
                    }
                }
            }

            for (DocumentVehicule document : documents) {
                try (PreparedStatement pstmtDocument = conn.prepareStatement(sqlDocument)) {
                    pstmtDocument.setInt(1, generatedId);
                    pstmtDocument.setInt(2, document.getIdTypeDocument());
                    if (document.getDateEcheance() != null) {
                        pstmtDocument.setDate(3, Date.valueOf(document.getDateEcheance()));
                    } else {
                        pstmtDocument.setNull(3, Types.DATE);
                    }
                    if (document.getKilometrageEcheance() != null) {
                        pstmtDocument.setInt(4, document.getKilometrageEcheance());
                    } else {
                        pstmtDocument.setNull(4, Types.INTEGER);
                    }
                    pstmtDocument.executeUpdate();
                }
            }

            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors du rollback de l'insertion du véhicule", ex);
            }
            throw new SQLException("Erreur lors de l'insertion du véhicule et de ses documents", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors de la réinitialisation de l'auto-commit", ex);
            }
        }
    }

    public static List<Vehicule> findAllVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicules";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idVehicule = rs.getInt("id_vehicule");
                String marque = rs.getString("marque");
                String modele = rs.getString("modele");
                String immatriculation = rs.getString("immatriculation");
                int anneeFabrication = rs.getInt("annee_fabrication");
                int kmActuel = rs.getInt("km_actuel");
                char categorie = rs.getString("categorie").charAt(0);

                Vehicule vehicule = new Vehicule(idVehicule, marque, modele, immatriculation, anneeFabrication, kmActuel, categorie);
                vehicules.add(vehicule);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des véhicules", e);
        }

        return vehicules;
    }

    public static List<DocumentVehicule> findAllDocuments() {
        List<DocumentVehicule> documents = new ArrayList<>();
        String sql = "SELECT * FROM DocumentsVehicules";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idDocument = rs.getInt("id_document");
                int idVehicule = rs.getInt("id_vehicule");
                int idTypeDocument = rs.getInt("id_type_document");
                Date dateEcheance = rs.getDate("date_echeance");
                Integer kilometrageEcheance = rs.getInt("kilometrage_echeance");
                if (rs.wasNull()) {
                    kilometrageEcheance = null;
                }

                DocumentVehicule document = new DocumentVehicule(idDocument, idVehicule, idTypeDocument);
                document.setDateEcheance(dateEcheance != null ? dateEcheance.toLocalDate() : null);
                document.setKilometrageEcheance(kilometrageEcheance);
                documents.add(document);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des documents des véhicules", e);
        }

        return documents;
    }

    public static void delete(int idVehicule) throws SQLException {
        String deleteDocuments = "DELETE FROM DocumentsVehicules WHERE id_vehicule = ?";
        String deleteVehicule = "DELETE FROM Vehicules WHERE id_vehicule = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(deleteDocuments)) {
                pstmt.setInt(1, idVehicule);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteVehicule)) {
                pstmt.setInt(1, idVehicule);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Le véhicule avec ID " + idVehicule + " n'existe pas.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors du rollback de la suppression du véhicule", ex);
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

    public static Vehicule findVehiculeById(int id) throws SQLException {
        Vehicule vehicule = null;
        String query = "SELECT * FROM vehicules WHERE id_vehicule = ?";

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    vehicule = new Vehicule();
                    vehicule.setMarque(rs.getString("marque"));
                    vehicule.setModele(rs.getString("modele"));
                    vehicule.setImmatriculation(rs.getString("immatriculation"));
                }
            }
        }

        return vehicule;
    }

    public static void updateVehiculeAndDocuments(Vehicule vehicule, List<DocumentVehicule> documents) throws SQLException {
        String updateVehiculeSQL = "UPDATE Vehicules SET marque = ?, modele = ?, immatriculation = ?, annee_fabrication = ?, km_actuel = ?, categorie = ? WHERE id_vehicule = ?";
        String updateDocumentSQL = "UPDATE documentsvehicules SET date_echeance = ?, kilometrage_echeance = ? WHERE id_vehicule = ? AND id_type_document = ?";
        String insertDocumentSQL = "INSERT INTO documentsvehicules (id_vehicule, id_type_document, date_echeance, kilometrage_echeance) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtVehicule = conn.prepareStatement(updateVehiculeSQL)) {
                pstmtVehicule.setString(1, vehicule.getMarque());
                pstmtVehicule.setString(2, vehicule.getModele());
                pstmtVehicule.setString(3, vehicule.getImmatriculation());
                pstmtVehicule.setInt(4, vehicule.getAnneeFabrication());
                pstmtVehicule.setInt(5, vehicule.getKmActuel());
                pstmtVehicule.setString(6, String.valueOf(vehicule.getCategorie()));
                pstmtVehicule.setInt(7, vehicule.getIdVehicule());

                int rowsAffectedVehicule = pstmtVehicule.executeUpdate();
                if (rowsAffectedVehicule == 0) {
                    throw new SQLException("La mise à jour du véhicule a échoué, aucun véhicule trouvé avec l'ID: " + vehicule.getIdVehicule());
                }
            }

            if (documents != null && !documents.isEmpty()) {
                for (DocumentVehicule doc : documents) {
                    try (PreparedStatement pstmtDocument = conn.prepareStatement(updateDocumentSQL)) {
                        pstmtDocument.setObject(1, doc.getDateEcheance() != null ? java.sql.Date.valueOf(doc.getDateEcheance()) : null);
                        pstmtDocument.setObject(2, doc.getKilometrageEcheance() != null && doc.getKilometrageEcheance() > 0 ? doc.getKilometrageEcheance() : null);
                        pstmtDocument.setInt(3, vehicule.getIdVehicule());
                        pstmtDocument.setInt(4, doc.getIdTypeDocument());

                        int rowsAffectedDocument = pstmtDocument.executeUpdate();
                        if (rowsAffectedDocument == 0) {
                            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertDocumentSQL)) {
                                pstmtInsert.setInt(1, vehicule.getIdVehicule());
                                pstmtInsert.setInt(2, doc.getIdTypeDocument());
                                pstmtInsert.setObject(3, doc.getDateEcheance() != null ? java.sql.Date.valueOf(doc.getDateEcheance()) : null);
                                pstmtInsert.setObject(4, doc.getKilometrageEcheance() != null && doc.getKilometrageEcheance() > 0 ? doc.getKilometrageEcheance() : null);
                                pstmtInsert.executeUpdate();
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
                throw new RuntimeException("Erreur lors du rollback de la mise à jour du véhicule", ex);
            }
            throw new SQLException("Erreur lors de la mise à jour", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException("Erreur lors de la réinitialisation de l'auto-commit", ex);
            }
        }
    }
}