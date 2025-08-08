package Service;

import Persistance.dao.AutoEcoleDAO;
import Persistance.dao.PaiementDAO;
import Persistance.models.AutoEcole;
import Persistance.models.Candidat;
import Persistance.models.Paiement;

import java.sql.SQLException;
import java.util.List;

public class PaiementService {
    public static List<Candidat> getCandidatNonPaye(){
        return CandidatService.getCandidatNonPaye();
    }
    public static void deletePaiement(int id){
        try {
            PaiementDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Paiement> getPaiementsByCinCandidat(int cin){
        return PaiementDAO.findByCinCandidat(cin);
    }

    public static void save(Paiement paiement){
        PaiementDAO.save(paiement);
    }
    public static void delete(int id){
        try {
            PaiementDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void update(Paiement paiement){
        try {
            PaiementDAO.update(paiement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void updateCandidat(Candidat candidat){
        try {
            CandidatService.updateCandidat(candidat);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static Candidat getCandidat(int cin){
        try {
            return CandidatService.getCandidat(cin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void generateRecuPDF(AutoEcole autoEcole,Paiement paiement,String path){
        try {
            PaiementDAO.generateRecuPDF(autoEcole,paiement,path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AutoEcole find(){
        return AutoEcoleService.find();
    }


    public static List<Paiement> getPaiementsForMonth(int year, int month){
        try {
            return PaiementDAO.getPaiementsForMonth(year,month);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
