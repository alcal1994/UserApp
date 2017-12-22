/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.PasswordGenerator;
import models.User;

/**
 * FXML Controller class
 *
 * @author albert
 */
public class LoginViewController implements Initializable {

    @FXML private TextField userIDTextField;
    @FXML private PasswordField pwField;
    @FXML private Label errMsgLabel;
    
    public void loginButtonPushed(ActionEvent event) throws IOException, NoSuchAlgorithmException
    {
        //query the database with the userID, get the salt
        //and encrypted password stored in the database
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        
        int userNum = Integer.parseInt(userIDTextField.getText());
        
        try{
            //1.connect to the DB
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            
            //2. create a query string with ? used instead of the values given by user
            String sql = "SELECT * FROM users WHERE userID = ?";
            
            //3. prepare statement
            ps = conn.prepareStatement(sql);
            
            //4. bind the userID to the ?
            ps.setInt(1, userNum);
            
            //5. execute the query 
            resultSet = ps.executeQuery();
            
            //6. extract the password and salt from the resultSet
            String dbPassword = null;
            byte[] salt = null;
            boolean admin = false;
            User user = null;
            while(resultSet.next())
            {
                dbPassword = resultSet.getString("password");
                
                Blob blob = resultSet.getBlob("salt");
                //convert into a byte array
                int blobLength = (int) blob.length();
                salt = blob.getBytes(1, blobLength);  
                
                admin = resultSet.getBoolean("admin");
                
                user = new User(resultSet.getString("firstName"),
                                        resultSet.getString("lastName"),
                                        resultSet.getString("phoneNumber"),
                                        resultSet.getDate("birthday").toLocalDate(),
                                        resultSet.getString("password"),
                                        resultSet.getBoolean("admin"));
                user.setUserID(resultSet.getInt("UserID"));
                user.setImageFile(new File(resultSet.getString("imageFile")));
               
            }
            //convert password given by the user into an encrytped password
            //using the salt from the database
            String userPW = PasswordGenerator.getSHA512Password(pwField.getText(), salt);
            
            SceneChanger sc = new SceneChanger();
            if(userPW.equals(dbPassword))
            SceneChanger.setLoggedInUser(user);
            //if the passwords match than change to the userTableView
            if(userPW.equals(dbPassword) && admin)
                sc.changeScenes(event, "UserTableView.fxml", "All Users");
            else if(userPW.equals(dbPassword))
            {
                //create an instance of the controller class for the log hours view
                LogHoursViewController controllerClass = new LogHoursViewController();
                sc.changeScenes(event, "LogHoursView.fxml", "Log Hours", user, controllerClass);
            }
            else
                //if they don't match, send error message
                errMsgLabel.setText("The userID and password do not match");
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       errMsgLabel.setText("");
    }    
    
}
