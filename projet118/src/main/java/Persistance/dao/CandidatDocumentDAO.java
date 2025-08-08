package Persistance.dao;

import Persistance.utils.ConxDB;
import Persistance.models.CandidatDocument; // Importer la classe CandidatDocument
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandidatDocumentDAO {

    private static Connection conn = ConxDB.getInstance();

    public static void save(CandidatDocument document) throws SQLException {
        String query = "INSERT INTO candidat_document (candidat_cin, document_type, description, file_content, upload_date) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, document.getCandidat_cin());
            statement.setString(2, document.getDocument_type());
            statement.setString(3, document.getDescription());
            statement.setBytes(4, document.getFile_content()); // Utilise setBytes pour le BLOB
            statement.setTimestamp(5, Timestamp.valueOf(document.getUpload_date() != null ? document.getUpload_date() : LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating document failed, no rows affected.");
            }
        }
    }

    public static void update(CandidatDocument document) throws SQLException {
        String query = "UPDATE candidat_document SET document_type = ?, description = ?, file_content = ? WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, document.getDocument_type());
            statement.setString(2, document.getDescription());
            statement.setBytes(3, document.getFile_content()); // Utilise setBytes pour le BLOB
            statement.setInt(4, document.getId());

            statement.executeUpdate();
        }
    }

    public static void delete(int documentId) throws SQLException {
        String query = "DELETE FROM candidat_document WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, documentId);
            statement.executeUpdate();
        }
    }

    public static void deleteAllForCandidat(int candidatCin) throws SQLException {
        String query = "DELETE FROM candidat_document WHERE candidat_cin = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, candidatCin);
            statement.executeUpdate();
        }
    }

    public static List<CandidatDocument> findByCandidatCin(int candidatCin) throws SQLException {
        String query = "SELECT * FROM candidat_document WHERE candidat_cin = ?";
        List<CandidatDocument> documents = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, candidatCin);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                documents.add(mapResultSetToDocument(resultSet));
            }

            return documents;
        }
    }



    private static CandidatDocument mapResultSetToDocument(ResultSet resultSet) throws SQLException {
        CandidatDocument document = new CandidatDocument();
        document.setId(resultSet.getInt("id"));
        document.setCandidat_cin(resultSet.getInt("candidat_cin"));
        document.setDocument_type(resultSet.getString("document_type"));
        document.setDescription(resultSet.getString("description"));
        document.setFile_content(resultSet.getBytes("file_content")); // Utilise getBytes pour le BLOB

        Timestamp uploadTimestamp = resultSet.getTimestamp("upload_date");
        LocalDateTime uploadDate = uploadTimestamp != null ? uploadTimestamp.toLocalDateTime() : null;
        document.setUpload_date(uploadDate);

        return document;
    }
}