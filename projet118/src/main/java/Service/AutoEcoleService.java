package Service;

import Persistance.dao.AutoEcoleDAO;
import Persistance.models.AutoEcole;

import javax.mail.MessagingException;
import java.sql.SQLException;

public class AutoEcoleService {
    public static void save(AutoEcole autoEcole) {
        try {
            AutoEcoleDAO.save(autoEcole);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static AutoEcole find(){
        try {
            return AutoEcoleDAO.find();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getRowCount(){
        return AutoEcoleDAO.getRowCount();
    }

    public static void sendPasswordResetEmail(String recipientEmail, String password){
        try {
            AutoEcoleDAO.sendPasswordResetEmail(recipientEmail, password);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}