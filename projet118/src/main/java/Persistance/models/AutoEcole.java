package Persistance.models;

public class AutoEcole {
    private int numTel;
    private String nom;
    private String adresse;
    private String email;
    private String password;

    public AutoEcole(int numTel, String nom, String adresse, String email, String password) {
        this.numTel = numTel;
        this.nom = nom;
        this.adresse = adresse;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public int getNumTel() {
        return numTel;
    }

    public void setNumTel(int numTel) {
        this.numTel = numTel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "numéro de Telephone: " + numTel + "\n" +
                "Nom: " + nom + "\n" +
                "Adresse: " + adresse + "\n" +
                "Email: " + email;
    }
}