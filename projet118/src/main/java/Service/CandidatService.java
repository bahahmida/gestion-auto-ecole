package Service;

import Persistance.dao.CandidatDAO;
import Persistance.models.Candidat;
import Persistance.models.CandidatDocument;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CandidatService {

    // Plus besoin d'instance de CandidatDAO ni de constructeur

    public static void ajouterCandidat(Candidat candidat) throws SQLException {
        CandidatDAO.ajouterCandidat(candidat);
    }

    public static Candidat getCandidat(int cin) throws SQLException {
        return CandidatDAO.getCandidat(cin);
    }

    public static List<Candidat> getAllCandidats() throws SQLException {
        return CandidatDAO.getAllCandidats();
    }

    public static void updateCandidat(Candidat candidat) throws SQLException {
        CandidatDAO.updateCandidat(candidat);
    }

    public static void deleteCandidat(int cin) throws SQLException {
        CandidatDAO.deleteCandidat(cin);
    }
    public static boolean existCandidat(int cin) throws SQLException {
        return CandidatDAO.getAllCandidats().stream().filter(candidat -> candidat.getCin() == cin).collect(Collectors.toList()).size()>0;
    }

    public static List<Candidat> searchCandidats(String searchText) throws SQLException {
        return CandidatDAO.getAllCandidats().stream()
                .filter(candidat ->
                        (candidat.getNom() != null && candidat.getNom().toLowerCase().contains(searchText.toLowerCase())) ||
                                (candidat.getPrenom() != null && candidat.getPrenom().toLowerCase().contains(searchText.toLowerCase())) ||
                                String.valueOf(candidat.getCin()).contains(searchText))
                .collect(Collectors.toList());
    }

    public static List<Candidat> getCandidatsByEtat(String etat) throws SQLException {
        return CandidatDAO.getAllCandidats().stream()
                .filter(candidat -> candidat.getEtat() != null && candidat.getEtat().equals(etat))
                .collect(Collectors.toList());
    }

    public static String getCandidatCategorie(int cin) throws SQLException {
        System.out.println(cin);
        return CandidatDAO.getAllCandidats().stream().filter(candidat ->candidat.getCin()==cin).collect(Collectors.toList()).get(0).getCategorie();
    }


    public static void saveDocument(CandidatDocument document){
        CandidatDocumentService.save(document);
}


    public static boolean MoniteurExists(int cin){
       return MoniteurService.MoniteurExists(cin);

    }
    public static List<Candidat> getCandidatNonPaye(){
        try {
            return CandidatDAO.getAllCandidats().stream().
                    filter(candidat -> candidat.getMontant_total()> candidat.getMontant_paye()).
                    collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Candidat> filterCandidats(String filter) throws SQLException {
        List<Candidat> candidats = getAllCandidats();
        switch (filter) {
            case "ACTIF":
                return candidats.stream()
                        .filter(candidat -> candidat.getEtat() != null && candidat.getEtat().equals("Actif"))
                        .collect(Collectors.toList());
            case "INACTIF":
                return candidats.stream()
                        .filter(candidat -> candidat.getEtat() != null && candidat.getEtat().equals("Inactif"))
                        .collect(Collectors.toList());
            case "NON_PAYE":
                return candidats.stream()
                        .filter(candidat -> candidat.getMontant_paye() < candidat.getMontant_total())
                        .collect(Collectors.toList());
            case "TOUT":
            default:
                return candidats; // Retourne tous les candidats sans filtre
        }
    }


}

