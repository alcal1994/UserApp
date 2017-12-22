/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import models.User;

/**
 * FXML Controller class
 *
 * @author albert
 */
public class LogHoursViewController implements Initializable , ControllerClass{

    @FXML private DatePicker datePicker;
    @FXML private Spinner hoursWorkedSpinner;
    @FXML private Label userIDLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label errMsgLabel;
    @FXML private Button backButton;
    
    @FXML private LineChart<?,?> lineChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis hourAxis;
    private XYChart.Series hoursLoggedSeries;
    
    
    private User user;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
         SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,3000,500);   
         hoursWorkedSpinner.setValueFactory(valueFactory);
         
         //change the text on the button if it is an administrative user
         if(!SceneChanger.getLoggedInUser().isAdmin())
             backButton.setText("Edit");
         
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

    @Override
    public void preloadData(User user) {
        this.user = user;
        userIDLabel.setText(Integer.toString(user.getUserID()));
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());
        datePicker.setValue(LocalDate.now());
        errMsgLabel.setText("");
        
        updateLineChart();
 
    }
    
    /**
     * Update line chart with latest information stored in database
     */
    
    public void updateLineChart()
    {
        hoursLoggedSeries = new XYChart.Series<>();
        hoursLoggedSeries.setName(Integer.toString(LocalDate.now().getYear()));
        lineChart.getData().clear();
        
        try{
            populateSeriesFromDB();
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        lineChart.getData().addAll(hoursLoggedSeries);
    }
    
    /**
     * this method will populate the hoursLoggedSeries with the latest info from the DB
     */
    
    public void populateSeriesFromDB() throws SQLException
    {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try{
            //1.connect to the DB
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            
            //2. create the string for the sql statement
            String sql = "SELECT MONTHNAME(dateWorked), SUM(hoursWorked) " +
                         "FROM hoursworked " +
                         "WHERE userID=? AND YEAR(dateWorked)=? " +
                         "GROUP BY MONTH(dateWorked);";
            //3. create the statement
            statement = conn.prepareCall(sql);
            
            //4. bind the parameters
            statement.setInt(1, user.getUserID());
            statement.setInt(2, LocalDate.now().getYear());
            
            //5.execute the query
            resultSet = statement.executeQuery();
            
            //6.loop over result set and build series
            while(resultSet.next())
            {
                hoursLoggedSeries.getData().add(new XYChart.Data(resultSet.getString(1),resultSet.getInt(2)));
            }

        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        finally
        {
            if(conn!= null)
                conn.close();
            if(statement != null)
                statement.close();
            if(resultSet != null)
                resultSet.close();
        }
    }
    
    /**
     * This method will read/validate and store the information
     * in the hoursWorked table
     * @param event
     */
    
    public void saveButtonPushed(ActionEvent event)
    {
        try{
            user.logHours(datePicker.getValue(), (int) hoursWorkedSpinner.getValue());
            errMsgLabel .setText("Sales Logged");
            updateLineChart();
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        catch(IllegalArgumentException e)
        {
            errMsgLabel.setText(e.getMessage());
        }
    }
    
       /**
     * This method will return the user to the table of users
     */
    
    public void cancelButtonPushed(ActionEvent event) throws IOException
    {
        //if this is an admin user, go back to the table of users
        SceneChanger sc = new SceneChanger();
        
        if(SceneChanger.getLoggedInUser().isAdmin())
        sc.changeScenes(event, "UserTableView.fxml", "All Users");
        else
        {
            NewUserViewController controller = new NewUserViewController();
            sc.changeScenes(event, "NewUserView.fxml", "Edit", user, controller);
        }
    }
    
}
