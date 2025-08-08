package Service;



import Persistance.dao.MoniteurDAO;
import Persistance.dao.PasserExamenDAO;
import Persistance.models.Candidat;
import Persistance.models.ExamenInfo;
import Persistance.models.Moniteur;
import Persistance.models.PasserExamen;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PasserExamenService {
    public static void save(PasserExamen passerExamen) throws SQLException {
        PasserExamenDAO.save(passerExamen);
    }
    public static ExamenInfo getExamenInfoById(int id) throws SQLException {
       return PasserExamenDAO.getExamenInfoById(id);
    }
    public static List<PasserExamen> findAll() throws SQLException {
        return PasserExamenDAO.findAll();
    }
    public static boolean delete(int cin, int id, LocalDateTime dateTime) throws SQLException {
        return PasserExamenDAO.delete(cin,id,dateTime);
    }
    public static boolean update(PasserExamen passerExamen,LocalDateTime oldDateExam) throws SQLException {
        return PasserExamenDAO.update(passerExamen,oldDateExam);
    }
    public  static boolean  MoniteurExists (int cin){
       return MoniteurService.MoniteurExists(cin);

    }
    public static Boolean examenExists(int cinCondidat) throws SQLException {
        LocalDateTime examenDateTime = PasserExamenDAO.getLatestExamenDateTime(cinCondidat);
        return examenDateTime != null && !examenDateTime.isBefore(LocalDateTime.now());
    }

    public static List<PasserExamen> filterByCode() throws SQLException {
        return findAll().stream()
                .filter(examen -> examen.getIdExamen() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens de type A (idExamen == 2 pour Conduite Type A ou idExamen == 3 pour Parking Type A).
     */
    public static List<PasserExamen> filterByTypeA() throws SQLException {
        return findAll().stream()
                .filter(examen -> examen.getIdExamen() == 2 || examen.getIdExamen() == 3)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens de type B (idExamen == 4 pour Conduite Type B ou idExamen == 5 pour Parking Type B).
     */
    public static List<PasserExamen> filterByTypeB() throws SQLException {
        return findAll().stream()
                .filter(examen -> examen.getIdExamen() == 4 || examen.getIdExamen() == 5)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens de type C (idExamen == 6 pour Conduite Type C ou idExamen == 7 pour Parking Type C).
     */
    public static List<PasserExamen> filterByTypeC() throws SQLException {
        return findAll().stream()
                .filter(examen -> examen.getIdExamen() == 6 || examen.getIdExamen() == 7)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens avec le résultat "En attente".
     */
    public static List<PasserExamen> filterByPending() throws SQLException {
        return findAll().stream()
                .filter(examen -> "En attente".equalsIgnoreCase(examen.getResultatExamen()))
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens avec le résultat "Échoué".
     */
    public static List<PasserExamen> filterByFailed() throws SQLException {
        return findAll().stream()
                .filter(examen -> "Échoué".equalsIgnoreCase(examen.getResultatExamen()))
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens avec le résultat "Réussi".
     */
    public static List<PasserExamen> filterByPassed() throws SQLException {
        return findAll().stream()
                .filter(examen -> "Réussi".equalsIgnoreCase(examen.getResultatExamen()))
                .collect(Collectors.toList());
    }

    /**
     * Filtre les examens de la semaine en cours.
     */
    public static List<PasserExamen> filterByThisWeek() throws SQLException {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
        int currentYear = today.getYear();

        return findAll().stream()
                .filter(examen -> {
                    LocalDateTime examDate = examen.getDateExamen();
                    LocalDate examLocalDate = examDate.toLocalDate();
                    int examWeek = examLocalDate.get(weekFields.weekOfWeekBasedYear());
                    int examYear = examLocalDate.getYear();
                    return examWeek == currentWeek && examYear == currentYear;
                })
                .collect(Collectors.toList());
    }
    public static List<Moniteur> findAllMoniteurs() throws SQLException {
        return MoniteurService.findAll();
    }
    public static String getCandidatCategorie(int cin) throws SQLException {
        return CandidatService.getCandidatCategorie(cin);
    }

    public static List<Moniteur> findMoniteursByCategorieA(){
       return  MoniteurService.getMoniteursTypeA();
    }
    public static List<Moniteur> findMoniteursByCategorieB(){
        return  MoniteurService.getMoniteursTypeB();
    }
    public static List<Moniteur> findMoniteursByCategorieC(){
        return  MoniteurService.getMoniteursTypeC();
    }

    public static boolean examenReussiExists(int cin, int idExamen) throws SQLException {
        List<PasserExamen> examens = PasserExamenDAO.findAll();
        return examens.stream()
                .anyMatch(examen ->
                        examen.getCinCondidat() == cin &&
                                examen.getIdExamen() == idExamen &&
                                examen.getResultatExamen() != null &&
                                examen.getResultatExamen().equalsIgnoreCase("Réussi")
                );
    }

    public static boolean tousExamensTermines(int cin) throws SQLException {
        List<PasserExamen> examens = PasserExamenDAO.findAll();
        return examens.stream()
                .filter(examen -> examen.getCinCondidat() == cin) // Filtrer les examens du candidat
                .allMatch(examen ->
                        examen.getResultatExamen() != null &&
                                !examen.getResultatExamen().equalsIgnoreCase("en attente")
                );
    }
    public static boolean verifierExamenPrecedent(int cin, int idExamen) throws SQLException {
        List<PasserExamen> examens = PasserExamenDAO.findAll();

        // Cas pour idExamen = 2, 4, 6 (examens de conduite)
        if (idExamen == 2 || idExamen == 4 || idExamen == 6) {
            int idPrecedent = 1; // Vérifie l'examen de code pour tous ces cas

            boolean precedentExisteEtReussi = examens.stream()
                    .filter(examen -> examen.getCinCondidat() == cin && examen.getIdExamen() == idPrecedent)
                    .anyMatch(examen ->
                            examen.getResultatExamen() != null &&
                                    examen.getResultatExamen().equalsIgnoreCase("Réussi")
                    );

            // Retourne true si l'examen 1 n'existe pas ou s'il existe et est réussi
            return !examens.stream().anyMatch(examen -> examen.getCinCondidat() == cin && examen.getIdExamen() == idPrecedent) ||
                    precedentExisteEtReussi;
        }
        // Cas pour idExamen = 3, 5, 7 (examens de parking)
        else if (idExamen == 3 || idExamen == 5 || idExamen == 7) {
            int idPrecedent;
            if (idExamen == 3) {
                idPrecedent = 2; // Vérifie l'examen de conduite type A
            } else if (idExamen == 5) {
                idPrecedent = 4; // Vérifie l'examen de conduite type B
            } else { // idExamen == 7
                idPrecedent = 6; // Vérifie l'examen de conduite type C
            }

            boolean precedentExisteEtReussi = examens.stream()
                    .filter(examen -> examen.getCinCondidat() == cin && examen.getIdExamen() == idPrecedent)
                    .anyMatch(examen ->
                            examen.getResultatExamen() != null &&
                                    examen.getResultatExamen().equalsIgnoreCase("Réussi")
                    );

            // Retourne true si l'examen précédent n'existe pas ou s'il existe et est réussi
            return !examens.stream().anyMatch(examen -> examen.getCinCondidat() == cin && examen.getIdExamen() == idPrecedent) ||
                    precedentExisteEtReussi;
        }

        // Si idExamen est autre (par exemple 1), pas de précédent à vérifier
        return true;
    }


    public static int genererIdExamen(String categoriePermis, String typeExamen) {
        if (categoriePermis.toUpperCase().equals("A")) {
            return typeExamen.equals("Conduite") ? 2 : 3;
        } else if (categoriePermis.toUpperCase().equals("B")) {
            return typeExamen.equals("Conduite") ? 4 : 5;
        } else if (categoriePermis.toUpperCase().equals("C")) {
            return typeExamen.equals("Conduite") ? 6 : 7;
        }
        return 0;
    }


}
