package Service;

import Persistance.dao.CandidatDAO;
import Persistance.models.Candidat;
import Persistance.models.Seance;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class MenuService {


    public static List<Candidat> getAllCandidats()  {
        try {
            return CandidatService.getAllCandidats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Seance> getAllSeancesByCandidatId(long candidatId){
        return SeanceService.getAllSeancesByCandidatId(candidatId);
    }

    public static List<Seance> getAllSeancesCodeByCandidatId (long candidatId){
        return SeanceService.getAllSeancesCodeByCandidatId(candidatId);
    }

    public static boolean rechercherSeanceEffectue(Timestamp horaire, long candidatId){
        return SeanceService.rechercherSeanceEffectue(horaire, candidatId);
    }
    public static void insererSeanceEffectue(Timestamp horaire, long candidatId){
        SeanceService.insererSeanceEffectue(horaire, candidatId);
    }
}
