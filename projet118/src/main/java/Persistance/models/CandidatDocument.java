package Persistance.models;

import java.time.LocalDateTime;

public class CandidatDocument {
    private int id;
    private String document_type;
    private String description;
    private byte[] file_content; // Remplace file_path
    private int candidat_cin;
    private LocalDateTime upload_date;

    public CandidatDocument() {}

    public CandidatDocument(String document_type, String description, byte[] file_content, int candidat_cin) {
        this.document_type = document_type;
        this.description = description;
        this.file_content = file_content; // Remplace file_path
        this.candidat_cin = candidat_cin;
        this.upload_date = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocument_type() {
        return document_type;
    }

    public void setDocument_type(String document_type) {
        this.document_type = document_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getFile_content() {
        return file_content;
    }

    public void setFile_content(byte[] file_content) {
        this.file_content = file_content;
    }

    public LocalDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDateTime upload_date) {
        this.upload_date = upload_date;
    }

    public int getCandidat_cin() {
        return candidat_cin;
    }

    public void setCandidat_cin(int candidat_cin) {
        this.candidat_cin = candidat_cin;
    }



}