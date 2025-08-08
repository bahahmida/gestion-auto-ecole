package Service;

import Persistance.dao.CandidatDAO;
import Persistance.dao.MoniteurDAO;
import Persistance.dao.VehiculeDAO;
import Persistance.models.Candidat;
import Persistance.models.Moniteur;
import Persistance.models.Seance;
import Persistance.dao.SeanceDAO;
import Persistance.models.Vehicule;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SeanceService {
    private static SeanceDAO seanceDAO = null;

    public SeanceService() {
        this.seanceDAO = new SeanceDAO();
    }

    // Create a new session
    public void createSeance(Seance seance) {
        seanceDAO.saveSeance(seance);
    }

    // Get a session by ID
    public Optional<Seance> getSeanceById(Long id) {
        return seanceDAO.findById(id);
    }

    public static List<Seance> getAllSeancesByCandidatId(long candidatId){
        try {
            return SeanceDAO.getAllSeancesByCandidatId(candidatId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Seance> filterCodeByThisWeek(){
        try {
            return SeanceDAO.filterCodeByThisWeek();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Seance> filterConduiteByThisWeek(){
        try {
            return SeanceDAO.filterConduiteByThisWeek();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean rechercherSeanceEffectue(Timestamp horaire, long candidatId){
        return SeanceDAO.rechercherSeanceEffectue(horaire, candidatId);
    }

    public static List<Vehicule> findAllVehicules(){
        return VehiculeService.findAllVehicules();
    }
    public static List<Candidat> getAllCandidats() throws SQLException {
        return CandidatService.getAllCandidats();
    }

    public static void saveSeanceCode(Seance seance){
        seanceDAO.saveSeanceCode(seance);
    }
    public static void saveSeance(Seance seance){
        seanceDAO.saveSeance(seance);
    }

    public static void insererSeanceEffectue(Timestamp horaire, long candidatId){
        SeanceDAO.insererSeanceEffectue(horaire, candidatId);
    }

    public static void deleteSeanceConduite(Long candidatId, Timestamp dateTime){
        try {
            SeanceDAO.deleteSeanceConduite(candidatId, dateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteSeanceCode(Long candidatId, Timestamp dateTime){
        try {
            SeanceDAO.deleteSeanceCode(candidatId,dateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static Moniteur getMoniteur(int cin){
        return MoniteurService.getMoniteur(cin);
    }

    public static List<Seance> getAllSeancesCodeByCandidatId(long candidatId){
        try {
            return SeanceDAO.getAllSeancesCodeByCandidatId(candidatId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Delete a session
    /*public void deleteSeance(Long id) throws SQLException {
        seanceDAO.delete(id);
    }*/

    // Get all sessions
    public static List<Seance> getAllSeances() {
        return seanceDAO.findAll();
    }

    // Get sessions for a specific candidate

    public static List<Seance> getSeancesCodeByMoniteurId(long moniteurId){
        return seanceDAO.getSeancesCodeByMoniteurId(moniteurId);
    }

    // Get sessions within a date range
    public List<Seance> getSeancesByDateRange(LocalDateTime start, LocalDateTime end) {
        return seanceDAO.findByDateRange(start, end);
    }
    public static List<Seance> getAllSeancesCode() {
        return seanceDAO.findAllCode();
    }

    // Get code sessions by candidate





    public static Candidat getCandidat(int cin) throws SQLException {
        return CandidatService.getCandidat(cin);
    }

    public static List<Character> getCategoriesByCin(String cin){
        try {
            return MoniteurDAO.getCategoriesByCin(cin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Seance getSeanceByCandidatId(long candidatId){
        try {
            return SeanceDAO.getSeanceByCandidatId(candidatId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Seance> getSeancesByDateAndVehicule(Timestamp dateTime, String vehicule){
        return SeanceDAO.getSeancesByDateAndVehicule(dateTime, vehicule);
    }

    public static List<Seance> getSeancesByMoniteurId(long moniteurId){
        return SeanceDAO.getSeancesByMoniteurId(moniteurId);
    }

    public static void updateSeanceCode(Seance seance, Timestamp originalDateTime){
        try {
            SeanceDAO.updateSeanceCode(seance,originalDateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateSeance(Seance seance, Timestamp originalDateTime){
        try {
            SeanceDAO.updateSeance(seance,originalDateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}