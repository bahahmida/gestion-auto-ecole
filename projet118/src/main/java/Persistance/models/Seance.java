package Persistance.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Seance {
    private Long id;
    private String type; // "CODE" or "CONDUITE"
    private Timestamp dateTime;
    private String location; // Location description
    private double latitude; // For map integration
    private double longitude;
    private String localisation;// For map integration
    private Long moniteurId; // Reference to the instructor
    private Long candidatId; // Reference to the student
    private String status; // e.g., "SCHEDULED", "COMPLETED", "CANCELLED"

    // Constructors
    public Seance() {}

    public Seance(String type, Timestamp dateTime, String location,
                  double latitude, double longitude, String localisation, Long moniteurId,
                  Long candidatId, String status) {
        this.type = type;
        this.dateTime = dateTime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.localisation = localisation;
        this.moniteurId = moniteurId;
        this.candidatId = candidatId;
        this.status = status;
    }
    public Seance(Timestamp dateTime, String location,
                  double latitude, double longitude,String localisation,long moniteurId,long candidatId) {
        this.dateTime = dateTime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.localisation = localisation;
        this.moniteurId = moniteurId;
        this.candidatId = candidatId;

    }
    public Seance(Timestamp dateTime,long moniteurId,long candidatId){
        this.dateTime = dateTime;
        this.moniteurId = moniteurId;
        this.candidatId = candidatId;
    }

    // Getters and Setters
    public int getId() { return Math.toIntExact(id); }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getDateTime() { return dateTime; }
    public void setDateTime(Timestamp dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getMoniteurId() { return Math.toIntExact(moniteurId); }
    public void setMoniteurId(Long moniteurId) { this.moniteurId = moniteurId; }

    public int getCandidatId() { return Math.toIntExact(candidatId); }
    public void setCandidatId(Long candidatId) { this.candidatId = candidatId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocalisation() {
        return localisation;
    }
    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
}