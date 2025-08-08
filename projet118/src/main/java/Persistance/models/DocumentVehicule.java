package Persistance.models;

import java.time.LocalDate;

public class DocumentVehicule {
    private int idDocument;
    private int idVehicule;
    private int idTypeDocument;
    private LocalDate dateEcheance;
    private Integer kilometrageEcheance;

    public DocumentVehicule(int idDocument, int idVehicule, int idTypeDocument) {
        this.idDocument = idDocument;
        this.idVehicule = idVehicule;
        this.idTypeDocument = idTypeDocument;
    }
    public DocumentVehicule() {}

    // Getters et setters
    public int getIdDocument() { return idDocument; }
    public void setIdDocument(int idDocument) { this.idDocument = idDocument; }
    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }
    public int getIdTypeDocument() { return idTypeDocument; }
    public void setIdTypeDocument(int idTypeDocument) { this.idTypeDocument = idTypeDocument; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }
    public Integer getKilometrageEcheance() { return kilometrageEcheance; }
    public void setKilometrageEcheance(Integer kilometrageEcheance) { this.kilometrageEcheance = kilometrageEcheance; }

    @Override
    public String toString() {
        return "DocumentVehicule{" +
                "idDocument=" + idDocument +
                ", idVehicule=" + idVehicule +
                ", idTypeDocument=" + idTypeDocument +
                ", dateEcheance=" + dateEcheance +
                ", kilometrageEcheance=" + kilometrageEcheance +
                '}';
    }
}