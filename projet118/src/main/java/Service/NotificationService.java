package Service;

import Persistance.dao.NotificationVehiculeDAO;
import Persistance.dao.VehiculeDAO;
import Persistance.models.NotificationVehicule;
import Persistance.models.Vehicule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {

    // Retrieve all notifications
    public static List<NotificationVehicule> getNotifications() {
        return NotificationVehiculeDAO.findAll();
    }

    // Retrieve notifications for a specific vehicle
    public static List<NotificationVehicule> getNotificationsForVehicule(int idVehicule) {
        List<NotificationVehicule> notifications = NotificationVehiculeDAO.findAll();
        return notifications.stream()
                .filter(notification -> notification.getIdVehicule() == idVehicule)
                .collect(Collectors.toList());
    }

    // Mark a notification as read
    public static void markNotificationAsRead(int idNotification) throws SQLException {
        NotificationVehiculeDAO.markAsRead(idNotification);
    }

    // Delete a notification
    public static void deleteNotification(int idNotification) throws SQLException {
        NotificationVehiculeDAO.delete(idNotification);
    }
    public static Vehicule getVehicule(int idVehicule) {
        return VehiculeService.getVehicule(idVehicule);
    }

    public static int save(NotificationVehicule notification){
        try {
            return NotificationVehiculeDAO.save(notification);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}