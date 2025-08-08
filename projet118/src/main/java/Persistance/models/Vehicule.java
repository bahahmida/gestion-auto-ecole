package Persistance.models;

public class Vehicule {
    private int idVehicule;
    private String marque;
    private String modele;
    private String immatriculation;
    private int anneeFabrication;
    private int kmActuel;
    private char categorie;

    public Vehicule(int idVehicule, String marque, String modele, String immatriculation, int anneeFabrication, int kmActuel, char categorie) {
        this.idVehicule = idVehicule;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.anneeFabrication = anneeFabrication;
        this.kmActuel = kmActuel;
        this.categorie = categorie;
    }
    public Vehicule() {}

    // Getters et setters
    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public int getAnneeFabrication() { return anneeFabrication; }
    public void setAnneeFabrication(int anneeFabrication) { this.anneeFabrication = anneeFabrication; }
    public int getKmActuel() { return kmActuel; }
    public void setKmActuel(int kmActuel) { this.kmActuel = kmActuel; }
    public char getCategorie() { return categorie; }
    public void setCategorie(char categorie) { this.categorie = categorie; }

    @Override
    public String toString() {
        return "Vehicule{" +
                "idVehicule=" + idVehicule +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", immatriculation='" + immatriculation + '\'' +
                ", anneeFabrication=" + anneeFabrication +
                ", kmActuel=" + kmActuel +
                ", categorie=" + categorie +
                '}';
    }
}