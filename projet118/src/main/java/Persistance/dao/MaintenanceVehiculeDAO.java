package Persistance.dao;

import Persistance.models.MaintenanceVehicule;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceVehiculeDAO {

    private static Connection conn = ConxDB.getInstance();

    // Récupérer toutes les maintenances d'un véhicule spécifique
    public static List<MaintenanceVehicule> findAllByVehiculeId(int idVehicule) {
        List<MaintenanceVehicule> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM MaintenanceVehicule WHERE idVehicule = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVehicule);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MaintenanceVehicule maintenance = new MaintenanceVehicule();
                    maintenance.setIdMaintenance(rs.getInt("idMaintenance"));
                    maintenance.setIdVehicule(rs.getInt("idVehicule"));
                    maintenance.setTypeMaintenance(rs.getString("typeMaintenance"));
                    maintenance.setDateDebut(rs.getDate("dateDebut") != null ? rs.getDate("dateDebut").toLocalDate() : null);
                    maintenance.setDateFin(rs.getDate("dateFin") != null ? rs.getDate("dateFin").toLocalDate() : null);
                    maintenance.setCout(rs.getDouble("cout"));
                    maintenance.setFacture(rs.getBlob("facture"));
                    maintenance.setDescription(rs.getString("description")); // Ajout de description
                    maintenances.add(maintenance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maintenances;
    }

    public static List<MaintenanceVehicule> findAll() {
        List<MaintenanceVehicule> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM MaintenanceVehicule";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                MaintenanceVehicule maintenance = new MaintenanceVehicule();
                maintenance.setIdMaintenance(rs.getInt("idMaintenance"));
                maintenance.setIdVehicule(rs.getInt("idVehicule"));
                maintenance.setTypeMaintenance(rs.getString("typeMaintenance"));
                maintenance.setDateDebut(rs.getDate("dateDebut") != null ? rs.getDate("dateDebut").toLocalDate() : null);
                maintenance.setDateFin(rs.getDate("dateFin") != null ? rs.getDate("dateFin").toLocalDate() : null);
                maintenance.setCout(rs.getDouble("cout"));
                maintenance.setFacture(rs.getBlob("facture"));
                maintenance.setDescription(rs.getString("description"));
                maintenances.add(maintenance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maintenances;
    }

    // Supprimer une maintenance
    public static void delete(int idMaintenance) throws SQLException {
        String sql = "DELETE FROM MaintenanceVehicule WHERE idMaintenance = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idMaintenance);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("La maintenance avec l'ID " + idMaintenance + " n'existe pas.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Insérer une nouvelle maintenance
    public static void save(MaintenanceVehicule maintenance) throws SQLException {
        String sql = "INSERT INTO MaintenanceVehicule (idVehicule, typeMaintenance, dateDebut, dateFin, cout, facture, description) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, maintenance.getIdVehicule());
                pstmt.setString(2, maintenance.getTypeMaintenance());
                pstmt.setDate(3, maintenance.getDateDebut() != null ? Date.valueOf(maintenance.getDateDebut()) : null);
                pstmt.setDate(4, maintenance.getDateFin() != null ? Date.valueOf(maintenance.getDateFin()) : null);
                pstmt.setDouble(5, maintenance.getCout());
                pstmt.setBlob(6, maintenance.getFacture());
                pstmt.setString(7, maintenance.getDescription()); // Ajout de description

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion de la maintenance : aucune ligne affectée.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Mettre à jour une maintenance existante
    public static void update(MaintenanceVehicule maintenance) throws SQLException {
        String sql = "UPDATE MaintenanceVehicule SET idVehicule = ?, typeMaintenance = ?, dateDebut = ?, dateFin = ?, cout = ?, facture = ?, description = ? WHERE idMaintenance = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, maintenance.getIdVehicule());
                pstmt.setString(2, maintenance.getTypeMaintenance());
                pstmt.setDate(3, maintenance.getDateDebut() != null ? Date.valueOf(maintenance.getDateDebut()) : null);
                pstmt.setDate(4, maintenance.getDateFin() != null ? Date.valueOf(maintenance.getDateFin()) : null);
                pstmt.setDouble(5, maintenance.getCout());
                pstmt.setBlob(6, maintenance.getFacture());
                pstmt.setString(7, maintenance.getDescription()); // Ajout de description
                pstmt.setInt(8, maintenance.getIdMaintenance());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("La mise à jour de la maintenance a échoué, aucune maintenance trouvée avec l'ID: " + maintenance.getIdMaintenance());
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static List<MaintenanceVehicule> getMaintenancesForMonth(int year, int month) {
        List<MaintenanceVehicule> maintenances = new ArrayList<>();
        // SQL query to filter by year and month of dateDebut
        String sql = "SELECT dateDebut, cout FROM MaintenanceVehicule WHERE YEAR(dateDebut) = ? AND MONTH(dateDebut) = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate dateDebut = rs.getDate("dateDebut") != null ? rs.getDate("dateDebut").toLocalDate() : null;
                    double cout = rs.getDouble("cout");
                    MaintenanceVehicule maintenance = new MaintenanceVehicule(dateDebut, cout);
                    maintenances.add(maintenance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maintenances;
    }

}