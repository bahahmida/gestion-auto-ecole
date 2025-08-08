package Service;

import Persistance.dao.MoniteurDAO;
import Persistance.models.Moniteur;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoniteurService {
    public static List<Moniteur> getMoniteursTypeA() {
        List<Moniteur> moniteurs = new ArrayList<>();
        moniteurs= MoniteurDAO.findAll();
        return moniteurs.stream().filter(moniteur -> moniteur.getCategorie().contains('A')).collect(Collectors.toList());
    }

    public static double getTotalSalaryForMonth(int year, int month){
        try {
            return MoniteurDAO.getTotalSalaryForMonth(year, month);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Moniteur> getMoniteursTypeB() {
        List<Moniteur> moniteurs = new ArrayList<>();
        moniteurs= MoniteurDAO.findAll();
        return moniteurs.stream().filter(moniteur -> moniteur.getCategorie().contains('B')).collect(Collectors.toList());
    }
    public static List<Moniteur> getMoniteursTypeC() {
        List<Moniteur> moniteurs = new ArrayList<>();
        moniteurs= MoniteurDAO.findAll();
        return moniteurs.stream().filter(moniteur -> moniteur.getCategorie().contains("C")).collect(Collectors.toList());
    }

    public static Moniteur getMoniteur(int cin) {
        List<Moniteur> moniteurs = new ArrayList<>();
        moniteurs= MoniteurDAO.findAll();
        if(MoniteurExists(cin)){


        return moniteurs.stream().filter(moniteur -> moniteur.getCin()==cin).collect(Collectors.toList()).get(0);}
        else{
            return null;
        }
    }
    public static void save(Moniteur moniteur) {
        MoniteurDAO.save(moniteur);
    }
    public static boolean MoniteurExists(int cin) {
        List<Moniteur> moniteurs = new ArrayList<>();
        moniteurs= MoniteurDAO.findAll();
        return moniteurs.stream().filter(moniteur -> moniteur.getCin()==cin).collect(Collectors.toList()).size()>0;
    }
    public static int getAnyMoniteur(){
        return MoniteurDAO.findAll().get(0).getCin();
    }
    public static List<Moniteur> findAll() {
        return MoniteurDAO.findAll();
    }

    public static void delete(int cin){
        try {
            MoniteurDAO.delete(cin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void update(Moniteur moniteur){
        try {
            MoniteurDAO.update(moniteur);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    public static List<Moniteur> filterMoniteurs(String filter) throws SQLException {
        List<Moniteur> moniteurs = findAll();
        if (filter.equals("ALL")) {
            return moniteurs;
        } else {
            return moniteurs.stream()
                    .filter(moniteur -> moniteur.getCategorie() != null &&
                            moniteur.getCategorie().stream()
                                    .map(String::valueOf)
                                    .anyMatch(cat -> cat.equals(filter)))
                    .collect(Collectors.toList());
        }
    }


}
