package Persistance.models;

import java.time.LocalDate;

public class NotificationVehicule {
    private int idNotification;
    private int idVehicule;
    private String type; // ENUM: 'assurance', 'visite_technique', 'vidange', 'vignette'
    private LocalDate dateNotification;
    private String message;
    private boolean etat; // FALSE (unread) or TRUE (read)

    // Constructor
    public NotificationVehicule(int idNotification, int idVehicule, String type, LocalDate dateNotification, String message, boolean etat) {
        this.idNotification = idNotification;
        this.idVehicule = idVehicule;
        this.type = type;
        this.dateNotification = dateNotification;
        this.message = message;
        this.etat = etat;
    }
    public NotificationVehicule() {
    }

    // Getters and Setters
    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDateNotification() {
        return dateNotification;
    }

    public void setDateNotification(LocalDate dateNotification) {
        this.dateNotification = dateNotification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isEtat() {
        return etat;
    }

    public void setEtat(boolean etat) {
        this.etat = etat;
    }

    @Override
    public String toString() {
        return "NotificationVehicule{" +
                "idNotification=" + idNotification +
                ", idVehicule=" + idVehicule +
                ", type='" + type + '\'' +
                ", dateNotification=" + dateNotification +
                ", message='" + message + '\'' +
                ", etat=" + etat +
                '}';
    }
}