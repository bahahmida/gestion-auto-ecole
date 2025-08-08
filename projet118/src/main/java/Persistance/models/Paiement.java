package Persistance.models;

import java.time.LocalDate;

public class Paiement {
    private int id;
    private int cinCandidat;
    private double montant;
    private LocalDate datePaiement;
    private String description;

    public Paiement(int id, int cinCandidat, double montant, LocalDate datePaiement, String description) {
        this.id = id;
        this.cinCandidat = cinCandidat;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCinCandidat() {
        return cinCandidat;
    }

    public void setCinCandidat(int cinCandidat) {
        this.cinCandidat = cinCandidat;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Paiment{" +
                "id=" + id +
                ", cinCandidat=" + cinCandidat +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", description='" + description + '\'' +
                '}';
    }
}
