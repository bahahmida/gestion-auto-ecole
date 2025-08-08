package Persistance.models;

import java.sql.Blob;
import java.time.LocalDate;

public class MaintenanceVehicule {
    private int idMaintenance;
    private int idVehicule;
    private String typeMaintenance;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double cout;
    private Blob facture;
    private String description; // Nouveau champ ajouté

    // Constructeurs
    public MaintenanceVehicule() {}
    public MaintenanceVehicule(LocalDate dateDebut,double cout) {
        this.dateDebut = dateDebut;
        this.cout = cout;
    }
    // Getters et Setters
    public int getIdMaintenance() { return idMaintenance; }
    public void setIdMaintenance(int idMaintenance) { this.idMaintenance = idMaintenance; }

    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }

    public String getTypeMaintenance() { return typeMaintenance; }
    public void setTypeMaintenance(String typeMaintenance) { this.typeMaintenance = typeMaintenance; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public double getCout() { return cout; }
    public void setCout(double cout) { this.cout = cout; }

    public Blob getFacture() { return facture; }
    public void setFacture(Blob facture) { this.facture = facture; }

    public String getDescription() { return description; } // Getter pour description
    public void setDescription(String description) { this.description = description; } // Setter pour description
}