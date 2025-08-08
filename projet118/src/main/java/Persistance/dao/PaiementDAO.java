package Persistance.dao;

import Persistance.models.AutoEcole;
import Persistance.models.Paiement;
import Persistance.utils.ConxDB;
import javafx.scene.text.TextAlignment;

import javax.swing.text.Document;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

public class PaiementDAO {

    private static Connection conn = ConxDB.getInstance();

    // Récupérer tous les paiements
    public static List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idPaiement = rs.getInt("id_paiement");
                int cinCandidat = rs.getInt("cin_candidat");
                double montant = rs.getDouble("montant");
                LocalDate datePaiement = rs.getDate("date_paiement").toLocalDate();
                String description = rs.getString("description");

                Paiement paiement = new Paiement(idPaiement, cinCandidat, montant, datePaiement, description);
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paiements;
    }

    // Supprimer un paiement par ID
    public static void delete(int idPaiement) throws SQLException {
        String sql = "DELETE FROM paiement WHERE id_paiement = ?";

        try {
            // Start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idPaiement);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Le paiement avec l'ID " + idPaiement + " n'existe pas.");
                }
            }

            // Commit transaction
            conn.commit();
        } catch (SQLException e) {
            // Rollback in case of error
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            // Reset auto-commit
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Insérer un nouveau paiement
    public static void save(Paiement paiement) {
        String sql = "INSERT INTO paiement (cin_candidat, montant, date_paiement, description) VALUES (?, ?, ?, ?)";

        try {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, paiement.getCinCandidat());
                pstmt.setDouble(2, paiement.getMontant());
                pstmt.setDate(3, Date.valueOf(paiement.getDatePaiement()));
                pstmt.setString(4, paiement.getDescription());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion du paiement : aucune ligne affectée.");
                }

                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        paiement.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Échec de la récupération de l'ID généré pour le paiement.");
                    }
                }
            }


        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du paiement", e);
        }
    }

    // Mettre à jour un paiement existant
    public static void update(Paiement paiement) throws SQLException {
        String sql = "UPDATE paiement SET cin_candidat = ?, montant = ?, date_paiement = ?, description = ? WHERE id_paiement = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, paiement.getCinCandidat());
                pstmt.setDouble(2, paiement.getMontant());
                pstmt.setDate(3, Date.valueOf(paiement.getDatePaiement()));
                pstmt.setString(4, paiement.getDescription());
                pstmt.setInt(5, paiement.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("La mise à jour du paiement a échoué, aucun paiement trouvé avec l'ID: " + paiement.getId());
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Récupérer un paiement par ID
    public static Paiement findById(int idPaiement) {
        String sql = "SELECT * FROM paiement WHERE id_paiement = ?";
        Paiement paiement = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPaiement);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int cinCandidat = rs.getInt("cin_candidat");
                    double montant = rs.getDouble("montant");
                    LocalDate datePaiement = rs.getDate("date_paiement").toLocalDate();
                    String description = rs.getString("description");

                    paiement = new Paiement(idPaiement, cinCandidat, montant, datePaiement, description);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paiement;
    }

    // Récupérer tous les paiements d'un candidat par CIN
    public static List<Paiement> findByCinCandidat(int cinCandidat) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE cin_candidat = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cinCandidat);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idPaiement = rs.getInt("id_paiement");
                    double montant = rs.getDouble("montant");
                    LocalDate datePaiement = rs.getDate("date_paiement").toLocalDate();
                    String description = rs.getString("description");

                    Paiement paiement = new Paiement(idPaiement, cinCandidat, montant, datePaiement, description);
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paiements;
    }


    public static void generateRecuPDF(AutoEcole autoEcole, Paiement paiement, String filePath) {
        try (PDDocument document = new PDDocument()) {
            // Définir une taille de page A5
            PDRectangle pageSize = PDRectangle.A5;
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            // Créer un flux de contenu pour écrire sur la page
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // En-tête centré et amélioré (sans fond coloré)
                float yStart = pageSize.getHeight() - 40;

                // Ligne décorative en haut
                contentStream.setLineWidth(2.0f);
                contentStream.setStrokingColor(0.3f, 0.3f, 0.6f); // Bleu foncé élégant
                contentStream.moveTo(40, yStart + 10);
                contentStream.lineTo(pageSize.getWidth() - 40, yStart + 10);
                contentStream.stroke();

                // Titre centré avec une police plus grande
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
                contentStream.setNonStrokingColor(0.2f, 0.2f, 0.5f); // Bleu foncé pour le texte du titre

                // Calculer la largeur du texte pour un centrage précis
                String title = "Reçu de Paiement";
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 22;
                float centerX = (pageSize.getWidth() - titleWidth) / 2;

                contentStream.beginText();
                contentStream.newLineAtOffset(centerX, yStart - 10);
                contentStream.showText(title);
                contentStream.endText();

                // Ligne décorative en bas de l'en-tête
                contentStream.setLineWidth(1.0f);
                contentStream.setStrokingColor(0.3f, 0.3f, 0.6f);
                contentStream.moveTo(80, yStart - 25);
                contentStream.lineTo(pageSize.getWidth() - 80, yStart - 25);
                contentStream.stroke();

                // Informations de l'Auto-École (on démarre plus bas pour laisser de l'espace à l'en-tête)
                float yPosition = yStart - 60;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.setNonStrokingColor(0, 0, 0); // Noir pour le texte

                // Nom de l'auto-école centré
                String nomAutoEcole = autoEcole.getNom();
                float nomWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(nomAutoEcole) / 1000 * 14;
                float nomX = (pageSize.getWidth() - nomWidth) / 2;

                contentStream.beginText();
                contentStream.newLineAtOffset(nomX, yPosition);
                contentStream.showText(nomAutoEcole);
                contentStream.endText();

                yPosition -= 25;

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                // Adresse
                contentStream.beginText();
                contentStream.newLineAtOffset(40, yPosition);
                contentStream.showText("Adresse: " + autoEcole.getAdresse());
                contentStream.endText();

                yPosition -= 20;
                // Téléphone
                contentStream.beginText();
                contentStream.newLineAtOffset(40, yPosition);
                contentStream.showText("Tél: " + autoEcole.getNumTel());
                contentStream.endText();

                yPosition -= 20;
                // Email
                contentStream.beginText();
                contentStream.newLineAtOffset(40, yPosition);
                contentStream.showText("Email: " + autoEcole.getEmail());
                contentStream.endText();

                yPosition -= 25;
                // Ligne de séparation
                contentStream.setLineWidth(1.5f);
                contentStream.setStrokingColor(0.5f, 0.5f, 0.5f); // Gris
                contentStream.moveTo(40, yPosition);
                contentStream.lineTo(pageSize.getWidth() - 40, yPosition);
                contentStream.stroke();

                // Détails du paiement dans une boîte
                yPosition -= 20;
                float boxHeight = 90;
                contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f); // Fond gris clair
                contentStream.addRect(40, yPosition - boxHeight, pageSize.getWidth() - 80, boxHeight);
                contentStream.fill();

                contentStream.setStrokingColor(0, 0, 0); // Bordure noire
                contentStream.setLineWidth(0.5f);
                contentStream.addRect(40, yPosition - boxHeight, pageSize.getWidth() - 80, boxHeight);
                contentStream.stroke();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setNonStrokingColor(0, 0, 0); // Texte noir
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition - 20);
                contentStream.showText("Détails du Paiement");
                contentStream.endText();

                // Formater la date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dateFormatted = paiement.getDatePaiement().format(formatter);

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition - 40);
                contentStream.showText("Montant : " + String.format("%.2f", paiement.getMontant()) + " TND");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition - 60);
                contentStream.showText("Date de Paiement : " + dateFormatted);
                contentStream.endText();

                yPosition -= boxHeight + 30;
                // Pied de page avec "Merci"
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                contentStream.setNonStrokingColor(0, 0.5f, 0); // Vert foncé

                String footerText = "Merci pour votre paiement !";
                float footerWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footerText) / 1000 * 12;
                float footerX = (pageSize.getWidth() - footerWidth) / 2;

                contentStream.beginText();
                contentStream.newLineAtOffset(footerX, yPosition);
                contentStream.showText(footerText);
                contentStream.endText();
            }

            // Sauvegarder le document
            document.save(filePath);

            // Ouvrir le fichier PDF généré
            File pdfFile = new File(filePath);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'ouverture du PDF: " + e.getMessage());
                }
            } else {
                System.out.println("Impossible d'ouvrir le fichier PDF : Desktop non supporté ou fichier non trouvé.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            throw new RuntimeException("Échec de la génération du reçu PDF", e);
        }
    }
    public static List<Paiement> getPaiementsForMonth(int year, int month) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT id_paiement, cin_candidat, montant, date_paiement, description " +
                "FROM paiement WHERE YEAR(date_paiement) = ? AND MONTH(date_paiement) = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_paiement");
                    int cinCandidat = rs.getInt("cin_candidat");
                    double montant = rs.getDouble("montant");
                    LocalDate datePaiement = rs.getDate("date_paiement").toLocalDate();
                    String description = rs.getString("description");

                    paiements.add(new Paiement(id, cinCandidat, montant, datePaiement, description));
                }
            }
        }
        return paiements;
    }
}