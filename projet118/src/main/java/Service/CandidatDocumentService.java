package Service;

import Persistance.dao.CandidatDocumentDAO;
import Persistance.models.CandidatDocument; // Importer la classe CandidatDocument
import java.sql.SQLException;
import java.util.List;


public class CandidatDocumentService {


    public static void save(CandidatDocument document) {
        try {
            CandidatDocumentDAO.save(document);
        } catch (SQLException e) {
            System.err.println("Error saving document: " + e.getMessage());

        }
    }


    public static boolean deleteDocument(int documentId) {
        try {
            CandidatDocumentDAO.delete(documentId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting document: " + e.getMessage());
            return false;
        }
    }


    public static List<CandidatDocument> findByCandidatCin(int candidatCin){
        try {
            return CandidatDocumentDAO.findByCandidatCin(candidatCin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}