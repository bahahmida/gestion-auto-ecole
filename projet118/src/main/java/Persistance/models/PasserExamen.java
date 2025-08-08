package Persistance.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PasserExamen {
    private int cinCondidat;
    private int idExamen;
    private LocalDateTime dateExamen;
    private String resultatExamen;
    private String nomExamen;
    private float prix;


    public PasserExamen(int cinCondidat, int idExamen, LocalDateTime dateExamen, String resultatExamen, String nomExamen, float prix) {
        this.cinCondidat = cinCondidat;
        this.idExamen = idExamen;
        this.dateExamen = dateExamen;
        this.resultatExamen = resultatExamen;
        this.nomExamen = nomExamen;
        this.prix = prix;
    }

    public String getResultatExamen() {
        return resultatExamen;
    }

    public void setResultatExamen(String resultatExamen) {
        this.resultatExamen = resultatExamen;
    }


    public LocalDateTime getDateExamen() {
        return dateExamen;
    }

    public void setDateExamen(LocalDateTime dateExamen) {
        this.dateExamen = dateExamen;
    }

    public int getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(int idExamen) {
        this.idExamen = idExamen;
    }

    public String getNomExamen() {

        return nomExamen;
    }

    public void setNomExamen(String nomExamen) {
        this.nomExamen = nomExamen;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    @Override
    public String toString() {
        return "PasserExamen{" +
                "cinCondidat=" + cinCondidat +
                ", idExamen=" + idExamen +
                ", dateExamen=" + dateExamen +
                ", resultatExamen='" + resultatExamen + '\'' +
                '}';
    }

    public int getCinCondidat() {
        return cinCondidat;
    }

    public void setCinCondidat(int cinCondidat) {
        this.cinCondidat = cinCondidat;
    }
}
