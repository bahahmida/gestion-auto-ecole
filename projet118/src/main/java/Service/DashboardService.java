package Service;

import Persistance.dao.MoniteurDAO;
import Persistance.dao.PasserExamenDAO;
import Persistance.models.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardService {

    public static List<Candidat> getAllCandidats(){
        try {
            return CandidatService.getAllCandidats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Seance> filterConduiteByThisWeek(){
        return SeanceService.filterConduiteByThisWeek();
    }

    public static List<MaintenanceVehicule> getMaintenancesForMonth(int year, int month){
        return MaintenanceVehiculeService.getMaintenancesForMonth(year, month);
    }

    public static double getTotalSalaryForMonth(int year, int month){
        return MoniteurService.getTotalSalaryForMonth(year, month);
    }

    public static List<Seance> filterCodeByThisWeek(){
        return SeanceService.filterCodeByThisWeek();
    }

    public static List<Moniteur> findAll() {
        return MoniteurService.findAll();
    }

    public static List<Vehicule> findAllVehicules(){
        return VehiculeService.findAllVehicules();
    }

    public static List<PasserExamen> findAllExamen() throws SQLException {
        return PasserExamenService.findAll();
    }

    public static Vehicule findVehiculeById(int id){
        return VehiculeService.findVehiculeById(id);
    }

    public static List<Paiement> getPaiementsForMonth(int year, int month){
        return PaiementService.getPaiementsForMonth(year, month);
    }

    public static long countUpcomingExamsThisWeek() throws SQLException {
        return PasserExamenService.filterByThisWeek().stream().count();
    }

    public static long countUpcomingCodeSessionsThisWeek() throws SQLException {
        return filterCodeByThisWeek().stream().count();
    }

    public static long countUpcomingDrivingSessionsThisWeek() throws SQLException {
        return filterConduiteByThisWeek().stream().count();
    }

    public static long countInactiveCandidates(List<Candidat> candidates) {
        return candidates.stream().filter(c->c.getEtat().equals("Inactif")).toArray().length;
    }

    public static long countPendingExams(List<PasserExamen> exams) {
        return exams.stream()
                .filter(e -> "EN ATTENTE".equalsIgnoreCase(e.getResultatExamen()))
                .count();
    }

    // Méthodes pour les graphiques

    public static Map<String, Object> getRevenueAndExpenseData(int year, int month) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        List<Paiement> paiements = getPaiementsForMonth(year, month);
        Map<Integer, Double> revenueByDay = paiements.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDatePaiement().getDayOfMonth(),
                        Collectors.summingDouble(Paiement::getMontant)
                ));

        List<MaintenanceVehicule> maintenances = getMaintenancesForMonth(year, month);
        Map<Integer, Double> expenseByDay = maintenances.stream()
                .filter(m -> m.getDateDebut() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getDateDebut().getDayOfMonth(),
                        Collectors.summingDouble(MaintenanceVehicule::getCout)
                ));

        double totalSalary = getTotalSalaryForMonth(year, month);
        expenseByDay.merge(2, totalSalary, Double::sum);

        result.put("revenueByDay", revenueByDay);
        result.put("expenseByDay", expenseByDay);
        return result;
    }

    public static Map<String, Long> getExamResultsDistribution() throws SQLException {
        List<PasserExamen> allExamens = findAllExamen();
        Map<String, Long> distribution = new HashMap<>();

        long reussiCount = allExamens.stream()
                .filter(e -> "RÉUSSI".equalsIgnoreCase(e.getResultatExamen()))
                .count();
        long echoueCount = allExamens.stream()
                .filter(e -> "ÉCHOUÉ".equalsIgnoreCase(e.getResultatExamen()))
                .count();
        long enAttenteCount = allExamens.stream()
                .filter(e -> "EN ATTENTE".equalsIgnoreCase(e.getResultatExamen()))
                .count();

        distribution.put("reussi", reussiCount);
        distribution.put("echoue", echoueCount);
        distribution.put("enAttente", enAttenteCount);
        distribution.put("total", (long) allExamens.size());
        return distribution;
    }

    public static Map<String, Long> getPaymentStatusDistribution() throws SQLException {
        List<Candidat> allCandidats = getAllCandidats();
        Map<String, Long> distribution = new HashMap<>();

        long fullyPaidCount = 0;
        long notPaidCount = 0;
        long partiallyPaidCount = 0;
        long totalCandidatss = 0;

        for (Candidat candidat : allCandidats) {
            double totalPaid = candidat.getMontant_paye();
            double totalRequested = candidat.getMontant_total();
            if (totalPaid >= totalRequested && totalRequested > 0.0 && totalPaid > 0.0) {
                fullyPaidCount++;
                totalCandidatss++;
            } else if (totalPaid == 0.0 && totalRequested > 0.0) {
                notPaidCount++;
                totalCandidatss++;
            } else if (totalPaid > 0 && totalPaid < totalRequested) {
                partiallyPaidCount++;
                totalCandidatss++;
            }
        }

        distribution.put("fullyPaid", fullyPaidCount);
        distribution.put("notPaid", notPaidCount);
        distribution.put("partiallyPaid", partiallyPaidCount);
        distribution.put("total", totalCandidatss);
        return distribution;
    }
}
