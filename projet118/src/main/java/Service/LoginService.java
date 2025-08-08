package Service;

import Persistance.dao.AutoEcoleDAO;
import Persistance.models.AutoEcole;

import java.sql.SQLException;

public class LoginService {

    public static int getRowCount(){
        return AutoEcoleService.getRowCount();
    }

    public static AutoEcole find(){
        return AutoEcoleService.find();
    }

    public static void sendPasswordResetEmail(String recipientEmail, String password){
        AutoEcoleService.sendPasswordResetEmail(recipientEmail, password);
    }
}
