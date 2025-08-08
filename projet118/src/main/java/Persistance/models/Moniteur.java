package Persistance.models;

import java.util.ArrayList;
import java.util.List;

public class Moniteur {
    private int cin;
    private String nom;
    private String prenom;
    private int tel;
    private float salaire;
    private List<Character> categorie;

    public Moniteur(int cin, String nom, String prenom, int tel, float salaire) {
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.tel = tel;
        this.salaire = salaire;
        this.categorie = new ArrayList<Character>();
    }
    public Moniteur() {}

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

    public int getTel() {
        return tel;
    }

    public void setTel(int tel) {
        this.tel = tel;
    }

    public float getSalaire() {
        return salaire;
    }

    public void setSalaire(float salaire) {
        this.salaire = salaire;
    }

    public List<Character> getCategorie() {
        return categorie;
    }

    public void setCategorie(List<Character> categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Moniteur{" +
                "cin=" + cin +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", tel=" + tel +
                ", salaire=" + salaire +
                ", categorie=" + categorie +
                '}';
    }
}
