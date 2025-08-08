package Persistance.dao;

import Persistance.models.AutoEcole;
import Persistance.utils.ConxDB;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import java.sql.*;

public class AutoEcoleDAO {
    private static Connection conn = ConxDB.getInstance();

    public static AutoEcole find() throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AutoEcole autoEcole = null;
        String sql = "SELECT * FROM informations";

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                autoEcole = new AutoEcole(
                        rs.getInt("num_tel"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("mail"),
                        rs.getString("password")
                );
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        return autoEcole;
    }

    public static boolean save(AutoEcole autoEcole) throws SQLException {
        PreparedStatement deleteStmt = null;
        PreparedStatement insertStmt = null;
        try {
            // Supprimer les données existantes
            String deleteSql = "DELETE FROM informations";
            deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.executeUpdate();

            // Insérer les nouvelles données
            String insertSql = "INSERT INTO informations (num_tel, nom, adresse, mail, password) VALUES (?, ?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertSql);

            // Vérification de la validité du mot de passe
            String password = autoEcole.getPassword();
            if (password == null) {
                throw new SQLException("Le mot de passe ne peut pas être null.");
            }

            insertStmt.setInt(1, autoEcole.getNumTel());
            insertStmt.setString(2, autoEcole.getNom());
            insertStmt.setString(3, autoEcole.getAdresse());
            insertStmt.setString(4, autoEcole.getEmail());
            insertStmt.setString(5, password);

            int rowsAffected = insertStmt.executeUpdate();
            return rowsAffected > 0;

        } finally {
            if (deleteStmt != null) deleteStmt.close();
            if (insertStmt != null) insertStmt.close();
        }
    }

    public static int getRowCount() {
        int rowCount = 0;
        String query = "SELECT COUNT(*) AS row_count FROM informations";

        try (Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                rowCount = resultSet.getInt("row_count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du nombre de lignes", e);
        }

        return rowCount;
    }

    public static void sendPasswordResetEmail(String recipientEmail, String password) throws MessagingException {
        final String SMTP_HOST = "smtp.gmail.com";
        final String SMTP_PORT = "587";
        final String EMAIL_FROM = "yonkobaha@gmail.com";
        final String EMAIL_PASSWORD = "axixqinzdncprcdn";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Récupération de mot de passe - Auto-École");

            String emailBody = "Bonjour,\n\n" +
                    "Vous avez demandé à récupérer votre mot de passe.\n" +
                    "Votre mot de passe est : " + password + "\n\n" +
                    "Si vous n'avez pas fait cette demande, veuillez ignorer cet email.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe Auto-École";
            message.setText(emailBody);

            Transport.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }
}