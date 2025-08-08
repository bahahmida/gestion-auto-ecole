package Service;

import Persistance.dao.MaintenanceVehiculeDAO;
import Persistance.models.MaintenanceVehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MaintenanceVehiculeService {

    // Sauvegarder une nouvelle maintenance
    public static void save(MaintenanceVehicule maintenance) throws SQLException {
        MaintenanceVehiculeDAO.save(maintenance);
    }

    public static List<MaintenanceVehicule> getMaintenancesForMonth(int year, int month){
        return MaintenanceVehiculeDAO.getMaintenancesForMonth(year, month);
    }

    // Vérifier si une maintenance existe
    public static boolean maintenanceExists(int idVehicule, LocalDate dateDebut) throws SQLException {
        List<MaintenanceVehicule> maintenances = new ArrayList<>();
        maintenances = MaintenanceVehiculeDAO.findAllByVehiculeId(idVehicule); // Récupère toutes les maintenances du véhicule

        // Vérifie si la dateDebut chevauche une période de maintenance existante
        return maintenances.stream().anyMatch(maintenance ->
                !dateDebut.isBefore(maintenance.getDateDebut()) && !dateDebut.isAfter(maintenance.getDateFin())
        );
    }

    // Supprimer une maintenance
    public static void delete(int idMaintenance) throws SQLException {
        MaintenanceVehiculeDAO.delete(idMaintenance);
    }

    // Mettre à jour une maintenance
    public static void update(MaintenanceVehicule maintenance) throws SQLException {
        MaintenanceVehiculeDAO.update(maintenance);
    }

    public static List<MaintenanceVehicule> getCurrentMaintenances() {
        LocalDate today = LocalDate.now();
        return MaintenanceVehiculeDAO.findAll().stream()
                .filter(maintenance -> maintenance.getDateFin().isAfter(today))
                .collect(Collectors.toList());
    }
    public static List<MaintenanceVehicule> getHistoricalMaintenancesByIdVehiculeId(int idVehicule) {
        LocalDate today = LocalDate.now();
        return MaintenanceVehiculeDAO.findAll().stream()
                .filter(maintenance -> (maintenance.getDateFin().isBefore(today) ||
                        maintenance.getDateFin().isEqual(today))
                        && maintenance.getIdVehicule() == idVehicule)
                .collect(Collectors.toList());
    }

}