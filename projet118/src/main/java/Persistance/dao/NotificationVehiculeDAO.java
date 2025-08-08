package Persistance.dao;

import Persistance.models.NotificationVehicule;
import Persistance.utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificationVehiculeDAO {

    private static Connection conn = ConxDB.getInstance();

    // Save a new notification
    public static int save(NotificationVehicule notification) throws SQLException {
        String sql = "INSERT INTO Notification_Vehicule (id_vehicule, type, date_notification, messaage, etat) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, notification.getIdVehicule());
            pstmt.setString(2, notification.getType());
            pstmt.setDate(3, Date.valueOf(notification.getDateNotification()));
            pstmt.setString(4, notification.getMessage());
            pstmt.setBoolean(5, notification.isEtat());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Échec de l'insertion de la notification : aucune ligne affectée.");
            }

            // Retrieve the generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    notification.setIdNotification(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Échec de la récupération de l'ID généré.");
                }
            }
        }
    }

    // Retrieve all notifications
    public static List<NotificationVehicule> findAll() {
        List<NotificationVehicule> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification_Vehicule";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idNotification = rs.getInt("id_notification");
                int idVehicule = rs.getInt("id_vehicule");
                String type = rs.getString("type");
                LocalDate dateNotification = rs.getDate("date_notification").toLocalDate();
                String message = rs.getString("messaage");
                boolean etat = rs.getBoolean("etat");

                NotificationVehicule notification = new NotificationVehicule(idNotification, idVehicule, type, dateNotification, message, etat);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    // Update the etat of a notification (mark as read)
    public static void markAsRead(int idNotification) throws SQLException {
        String sql = "UPDATE Notification_Vehicule SET etat = TRUE WHERE id_notification = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idNotification);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("La mise à jour de l'état de la notification a échoué, aucune notification trouvée avec l'ID: " + idNotification);
            }
        }
    }

    // Delete a notification
    public static void delete(int idNotification) throws SQLException {
        String sql = "DELETE FROM Notification_Vehicule WHERE id_notification = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idNotification);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("La suppression de la notification a échoué, aucune notification trouvée avec l'ID: " + idNotification);
            }
        }
    }
}