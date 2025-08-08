package Persistance.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Candidat {
    private int cin;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate date_naissance;
    private String categorie;
    private String etat;
    private int seances_effectuees;
    private int seances_totales;
    private double montant_paye;
    private double montant_total;

    public Candidat() {
        // Initialisation des valeurs par défaut
        this.cin = 0;
        this.nom = "";
        this.prenom = "";
        this.telephone = "";
        this.date_naissance = LocalDate.now();
        this.categorie = "";
        this.etat = "En cours";
        this.seances_effectuees = 0;
        this.seances_totales = 0;
        this.montant_paye = 0.0;
        this.montant_total = 0.0;
    }

    public Candidat(int cin, String nom, String prenom, String telephone, LocalDate date_naissance,
                    String categorie, String etat, int seances_effectuees,
                    int seances_totales, double montant_paye, double montant_total) {
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.date_naissance = date_naissance;
        this.categorie = categorie;
        this.etat = etat;
        this.seances_effectuees = seances_effectuees;
        this.seances_totales = seances_totales;
        this.montant_paye = montant_paye;
        this.montant_total = montant_total;
    }

    public int getCin() {
        return cin;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDate_naissance() {
        return date_naissance;
    }

    public void setDate_naissance(LocalDate date_naissance) {
        this.date_naissance = date_naissance;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }



    public int getSeances_effectuees() {
        return seances_effectuees;
    }

    public void setSeances_effectuees(int seances_effectuees) {
        this.seances_effectuees = seances_effectuees;
    }

    public int getSeances_totales() {
        return seances_totales;
    }

    public void setSeances_totales(int seances_totales) {
        this.seances_totales = seances_totales;
    }

    public double getMontant_paye() {
        return montant_paye;
    }

    public void setMontant_paye(double montant_paye) {
        this.montant_paye = montant_paye;
    }

    public double getMontant_total() {
        return montant_total;
    }

    public void setMontant_total(double montant_total) {
        this.montant_total = montant_total;
    }

    @Override
    public String toString() {
        return "Candidat{" +
                "cin=" + cin +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", date_naissance=" + date_naissance +
                ", categorie='" + categorie + '\'' +
                ", etat='" + etat + '\'' +
                ", seances_effectuees=" + seances_effectuees +
                ", seances_totales=" + seances_totales +
                ", montant_paye=" + montant_paye +
                ", montant_total=" + montant_total +
                '}';
    }
}
