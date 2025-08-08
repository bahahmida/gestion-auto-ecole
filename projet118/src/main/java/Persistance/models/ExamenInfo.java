package Persistance.models;

public class ExamenInfo{
    private final int idExamen;
    private final String nomExamen;
    private final int prix;

    public ExamenInfo(int idExamen, String nomExamen, int prix) {
        this.idExamen = idExamen;
        this.nomExamen = nomExamen;
        this.prix = prix;
    }

    public int getIdExamen() {
        return idExamen;
    }

    public String getNomExamen() {
        return nomExamen;
    }

    public int getPrix() {
        return prix;
    }

    @Override
    public String toString() {
        return "Examen{" +
                "id=" + idExamen +
                ", nom='" + nomExamen + '\'' +
                ", prix=" + prix +
                '}';
    }
}
