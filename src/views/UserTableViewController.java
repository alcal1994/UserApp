/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.User;

/**
 * FXML Controller class
 *
 * @author albert
 */
public class UserTableViewController implements Initializable{
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIDColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, LocalDate> birthdayColumn;
    @FXML private Button editUserButton;
    @FXML private Button logHoursButton;
    
    /**
     * If the edit button is pushed, pass the selected user to the NewUserView
     * and preload it with the data
     */
    public void editButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger sc = new SceneChanger();
        User user = this.userTable.getSelectionModel().getSelectedItem();
        NewUserViewController npvc = new NewUserViewController();
        
        sc.changeScenes(event, "NewUserView.fxml", "Edit User", user, npvc);
    }
    
    /**
     * If a user has been selected in the table, enable the edit button
     */
    
    public void userSelected(){
        editUserButton.setDisable(false);
        logHoursButton.setDisable(false);
    }
    
    /**
     * This method will switch to the NewUserView scene when the button is pushed
     * @param event
     * @throws java.io.IOException
     */
    public void newUserButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger sc = new SceneChanger();
        sc.changeScenes(event, "NewUserView.fxml", "Create New User");
        
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //disable the edit button until a user has been selected from the table
        editUserButton.setDisable(true);
        logHoursButton.setDisable(true);
        // Configure the table columns
        userIDColumn.setCellValueFactory(new PropertyValueFactory<User, Integer>("userID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<User, String>("phoneNumber"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<User, LocalDate>("birthday"));
        
        try{
            loadUsers();
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }    
    
    /**
     * This method will load the users from the database and load them into the 
     * TableView object
     */
    
    public void loadUsers() throws SQLException
    {
        ObservableList<User> users = FXCollections.observableArrayList();
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try{
            //1.connect to database
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            //2.create a statement object
            statement = conn.createStatement();
            //3. create the sql query
            resultSet = statement.executeQuery("SELECT * FROM users");
            //4. create user objects from each record
            while (resultSet.next())
            {
                User newUser = new User(resultSet.getString("firstName"),
                                        resultSet.getString("lastName"),
                                        resultSet.getString("phoneNumber"),
                                        resultSet.getDate("birthday").toLocalDate(),
                                        resultSet.getString("password"),
                                        resultSet.getBoolean("admin"));
                newUser.setUserID(resultSet.getInt("UserID"));
                newUser.setImageFile(new File(resultSet.getString("imageFile")));
                users.add(newUser);
            }
            userTable.getItems().addAll(users);
        }catch(Exception e)
        {
            System.err.println(e.getMessage());           
        }
        finally
        {
            if(conn!= null)
               conn.close();
            if(statement!=null)
               statement.close();
            if(resultSet != null)
                resultSet.close();
        }
        
    }
    
    /**
     * This method will pull up the logHours view
     */
    
    public void logHoursButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger sc = new SceneChanger();
        //This gets the user from the table
        User user = this.userTable.getSelectionModel().getSelectedItem();
        if(user == null)
            return;
        
        LogHoursViewController lhvc = new LogHoursViewController();
        sc.changeScenes(event, "LogHoursView.fxml", "Log Hours", user, lhvc);
        
    }
    
    /**
     * Change scenes to the monthly report view when pushed
     */
    public void monthlyHoursButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger sc = new SceneChanger();
        sc.changeScenes(event, "MonthlyHoursView.fxml", "View Hours");
    }
    
     /**
     * This method will log the user out of the application and return them to the LoginView scene
     * 
     */
    
    public void logoutButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger.setLoggedInUser(null);
        SceneChanger sc = new SceneChanger();
        sc.changeScenes(event, "LoginView.fxml","Login");
    }

}
