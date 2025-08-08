package Service;


import Persistance.dao.VehiculeDAO;
import Persistance.models.NotificationVehicule;
import Persistance.models.Vehicule;
import Persistance.models.DocumentVehicule;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VehiculeService {


    public static List<Vehicule> findAllVehicules(){
        return VehiculeDAO.findAllVehicules();
    }
    // Récupérer les véhicules de catégorie A
    public static List<Vehicule> getVehiculesByCategorieA() {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules = VehiculeDAO.findAllVehicules();
        return vehicules.stream().filter(vehicule -> vehicule.getCategorie() == 'A').collect(Collectors.toList());
    }

    public static List<Vehicule> getVehicules() {
        return VehiculeDAO.findAllVehicules();
    }

    // Récupérer les véhicules de catégorie B
    public static List<Vehicule> getVehiculesByCategorieB() {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules = VehiculeDAO.findAllVehicules();
        return vehicules.stream().filter(vehicule -> vehicule.getCategorie() == 'B').collect(Collectors.toList());
    }
    public static List<Vehicule> getVehiculesByCategorieC() {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules = VehiculeDAO.findAllVehicules();
        return vehicules.stream().filter(vehicule -> vehicule.getCategorie() == 'C').collect(Collectors.toList());
    }

    // Récupérer un véhicule par son ID
    public static Vehicule getVehicule(int idVehicule) {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules = VehiculeDAO.findAllVehicules();
        return vehicules.stream().filter(vehicule -> vehicule.getIdVehicule() == idVehicule).collect(Collectors.toList()).get(0);
    }

    // Enregistrer un véhicule avec ses documents
    public static int save(Vehicule vehicule, List<DocumentVehicule> documents) throws SQLException {
        return VehiculeDAO.save(vehicule, documents);
    }

    // Vérifier si un véhicule existe déjà (par ID)
    public static boolean VehiculeExists(String immatricula) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules = VehiculeDAO.findAllVehicules();
        return vehicules.stream().filter(vehicule -> vehicule.getImmatriculation().equals(immatricula)).collect(Collectors.toList()).size() > 0;
    }

    // Récupérer tous les documents d'un véhicule spécifique
    public static List<DocumentVehicule> getDocumentsForVehicule(int idVehicule) {
        List<DocumentVehicule> documents = new ArrayList<>();
        documents = VehiculeDAO.findAllDocuments();
        return documents.stream().filter(doc -> doc.getIdVehicule() == idVehicule).collect(Collectors.toList());
    }

    public static LocalDate calculateDateEcheance(String immatriculation) {
        // Extraire le dernier chiffre du numéro d'immatriculation
        String numericPart = immatriculation;// Retirer tout sauf les chiffres
        if (numericPart.isEmpty()) {
            // Si aucun chiffre n'est trouvé, utiliser une date par défaut
            return adjustYearBasedOnCurrentDate(3, 5); // 5 mars
        }

        // Prendre le dernier chiffre
        int lastDigit = Character.getNumericValue(numericPart.charAt(numericPart.length() - 1));

        // Déterminer si le dernier chiffre est pair ou impair
        if (lastDigit % 2 == 0) {
            // Pair : 6 mars
            return adjustYearBasedOnCurrentDate(3, 6);
        } else {
            // Impair : 5 avril
            return adjustYearBasedOnCurrentDate(4, 5);
        }
    }

    // Méthode utilitaire pour ajuster l'année en fonction de la date actuelle
    private static LocalDate adjustYearBasedOnCurrentDate(int month, int day) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();

        // Créer la date d'échéance pour l'année courante
        LocalDate echeanceThisYear = LocalDate.of(currentYear, month, day);

        // Si la date n'est pas encore dépassée (ou est aujourd'hui), retourner cette année
        if (!echeanceThisYear.isBefore(currentDate)) {
            return echeanceThisYear;
        } else {
            // Sinon, retourner la même date l'année prochaine
            return LocalDate.of(currentYear + 1, month, day);
        }
    }

    // Nouvelle méthode pour mettre à jour un véhicule
    public static void updateVehicule(Vehicule vehicule,List<DocumentVehicule> documentVehicules)  {
        // Puisque nous ne mettons à jour que le km_actuel dans ce contexte
        try {
            VehiculeDAO.updateVehiculeAndDocuments(vehicule, documentVehicules);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getImmatriculationById(int idVehicule) {
        Vehicule vehicule = getVehicule(idVehicule); // Méthode supposée dans VehiculeDAO
        return vehicule != null ? vehicule.getImmatriculation() : null;
    }
    public static void delete(int idVehicule) throws SQLException {
        try {
            VehiculeDAO.delete(idVehicule);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static DocumentVehicule updateDateViniette(DocumentVehicule documentVehicule) {
        if (documentVehicule.getDateEcheance() != null) {
            LocalDate currentDate = LocalDate.now();
            LocalDate echeance = documentVehicule.getDateEcheance();

            // Vérifier si la date d'échéance est passée
            if (echeance.isBefore(currentDate)) {
                // Récupérer le jour et le mois de la date d'échéance
                int day = echeance.getDayOfMonth();
                int month = echeance.getMonthValue();
                // Incrémenter l'année de 1
                int newYear = echeance.getYear() + 1;
                // Créer une nouvelle date avec le même jour et mois, mais l'année suivante
                LocalDate newEcheance = LocalDate.of(newYear, month, day);
                documentVehicule.setDateEcheance(newEcheance);
            }
        }
        return documentVehicule;
    }

    public static void UpdateDateVinietteForAllVehicule() {
        getVehicules().stream()
                .forEach(vehicule -> {
                    // Récupérer les documents du véhicule
                    List<DocumentVehicule> documents = getDocumentsForVehicule(vehicule.getIdVehicule());

                    // Trouver et mettre à jour le document vignette (idTypeDocument = 1)
                    documents.stream()
                            .filter(doc -> doc.getIdTypeDocument() == 1)
                            .findFirst()
                            .ifPresent(document -> {
                                // Mettre à jour la date de la vignette si elle est passée
                                updateDateViniette(document);
                            });

                    // Mettre à jour le véhicule avec la liste complète des documents
                    updateVehicule(vehicule, documents);
                });
    }

    
    /**
     * Récupérer tous les documents de tous les véhicules
     * @return Liste de tous les documents
     */
    public static List<DocumentVehicule> getAllDocuments() {
        return VehiculeDAO.findAllDocuments();
    }

    public static Vehicule findVehiculeById(int id){
        try {
            return VehiculeDAO.findVehiculeById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Vehicule> filterVehicules(String filter) throws SQLException {
        List<Vehicule> vehicules = getVehicules();
        if (vehicules == null) {
            return Collections.emptyList();
        }

        if ("ALL".equalsIgnoreCase(filter)) {
            return new ArrayList<>(vehicules);
        }

        return vehicules.stream()
                .filter(vehicule -> {
                    Character categorie = vehicule.getCategorie();
                    return filter.equalsIgnoreCase(categorie.toString());
                })
                .collect(Collectors.toList());
    }




}
