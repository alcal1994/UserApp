/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.Period;

/**
 *
 * @author albert
 */
public class User {
    private String firstName, lastName, phoneNumber, password;
    private LocalDate birthday;
    private File imageFile;
    private int userID;
    private byte[] salt;
    private boolean admin;
   

    public User(String firstName, String lastName, String phoneNumber, LocalDate birthday, String password, boolean admin) throws NoSuchAlgorithmException {
        
        setFirstName(firstName);
        setLastName(lastName);
        setPhoneNumber(phoneNumber);
        setBirthday(birthday);
        setImageFile(new File("./src/images/defaultimage.jpg"));
        salt = PasswordGenerator.getSalt();
        this.password = PasswordGenerator.getSHA512Password(password, salt);
        this.admin = admin;

        
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public User(String firstName, String lastName, String phoneNumber, LocalDate birthday, File imageFile, boolean admin, String password) throws IOException, NoSuchAlgorithmException {
        this(firstName, lastName, phoneNumber, birthday, password, admin);
        setImageFile(imageFile);
        copyImageFile();
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        if(userID >= 0)            
            this.userID = userID;
        else
            throw new IllegalArgumentException("UserID must be >= 0");
    }
    

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * area code    city    house
     * NXX          -xxx    -xxxx
     * @param phoneNumber 
     */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber.matches("[2-9]\\d{2}[-.]?\\d{3}[-.]\\d{4}"))
        this.phoneNumber = phoneNumber;
        else
            throw new IllegalArgumentException("Phone number must be in the pattern of NXX-XXX-XXXX");
    }

    public void setBirthday(LocalDate birthday) {
        int age = Period.between(birthday, LocalDate.now()).getYears(); 
        
        if(age>= 10 && age<=100)
            this.birthday = birthday;
        else
            throw new IllegalArgumentException("User must be 10-100 years of age");
        
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
    
    /*
     This method will copy the file specified to the images directory on this server and give it a unique name
     
     */
     
    public void copyImageFile() throws IOException{
        //Create a new Path to copy the image into a local directary
        Path sourcePath = imageFile.toPath();
        
        String uniqueFileName = getUniqueFileName(imageFile.getName());
        
        Path targetPath = Paths.get("./src/images/" + uniqueFileName);
        
        //copy the file to the new directory
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        //update the imageFile to the point to the new file
        imageFile = new File(targetPath.toString());
    }
    
    /**
     * This method will receive a String that represents a file name and return a 
     * String with a random, unique set of letters prefixed to it
     */
    
    private String getUniqueFileName(String oldFileName){
        String newName;
        
        //create a Random Number Generator
        SecureRandom rng = new SecureRandom();
        
        //loop until file name is unique
        
        do
        {
            newName = "";
        
        
        //generate 32 random characters
        for(int count =1; count <=32; count++)
        {
            int nextChar;
            
            do
            {
              nextChar = rng.nextInt(123);
            }while(!validCharacterValue(nextChar));
            
            newName = String.format("%s%c", newName, nextChar);
        }
        newName += oldFileName;
        }while(!uniqueFileInDirectory(newName));
        
        return newName;
    }
    
    
     /*
    This method will search the images directory and ensure that the file name 
    if unique.
    */
    public boolean uniqueFileInDirectory(String fileName){
        File directory = new File("./src/images/");
        
        File[] dir_contents = directory.listFiles();
        
        for(File file: dir_contents)
        {
            if(file.getName().equals(fileName))
                return false;
        }
        return true;
            
    }
    /*
    
    This method will validate if the integer given corresponds to a valid
    ASCII character that could be used in a file name
    
    */
    
    public Boolean validCharacterValue(int asciiValue){
        
        //0-9 ASCII range 48-57
        if(asciiValue>=48 && asciiValue<=57)
            return true;
        
        //A-Z ASCII range 65-90
         if(asciiValue>=65 && asciiValue<=90)
            return true;
         
         //A-Z ASCII range 97-122
         if(asciiValue>=97 && asciiValue<=122)
            return true;
         
         return false;
    }
    
    /**
     * This method will return a formatted string with the persons first name, last name, and age
     */
    public String toString(){
        return String.format("%s %s is %d years old", firstName, lastName, Period.between(birthday, LocalDate.now()).getYears());
    }
    
    /**
     * This method will write the instance of the volunteer into the database.
     */
    public void insertIntoDB() throws SQLException{
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        
        try
        {
            //1. Connect to database
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            
            //2. Create a String that holds the query with ? as user inputs
            String sql = "INSERT INTO USERS (firstName, lastName, phoneNumber, birthday, imageFile, password, salt, admin)" + "VALUES (?,?,?,?,?,?,?,?)";
            
            //3. Prepare the query 
            preparedStatement = conn.prepareStatement(sql);
            
            //4.Convert the birthday into a SQL date
            Date db = Date.valueOf(birthday);
            
            //5.Bind the values to the parameters
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setDate(4, db);
            preparedStatement.setString(5, imageFile.getName());
            preparedStatement.setString(6, password);
            preparedStatement.setBlob(7, new javax.sql.rowset.serial.SerialBlob(salt));
            preparedStatement.setBoolean(8, admin);
            
            preparedStatement.executeUpdate();
            
        }
        catch(Exception e)
        {
            System.err.println(e.getMessage());
        }
        finally
        {
            if(preparedStatement != null)
                preparedStatement.close();
            
            if(conn!=null)
               conn.close();
        }
    }
    
    /**
     * This method will update user in the database
     */
    
    public void updateUserInDB() throws SQLException
    {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        
        try
        {   //1.connect to db
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            //2.create a String that holds our SQL command with ? for user inputs
            String sql = "UPDATE users SET firstName = ?, lastName = ?, "
                    + "phoneNumber = ?, birthday = ?, imageFile = ?, admin = ? " 
                    + "WHERE userID = ?";
            
            //3.prepare the query against SQL injection
            preparedStatement = conn.prepareStatement(sql);
            
            //4.convert the birthday into date object
            Date bd = Date.valueOf(birthday);
            
            //5.bind the parameters 
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setDate(4, bd);
            preparedStatement.setString(5, imageFile.getName());
            preparedStatement.setBoolean(6, admin);
            preparedStatement.setInt(7, userID);
            
            //6. run the command on the SQL server
            preparedStatement.executeUpdate();
            preparedStatement.close();
               
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        finally
        { 
            if(conn!=null)
               conn.close();
             if(preparedStatement != null)
                preparedStatement.close();
        }
    }
    
    /**
     * This method will record the hours worked for the user.
     * @param dateWorked - must be in the current year and previous to the current date
     * @param hoursWorked - must be less than 18 hours
     */
    
    public void logHours(LocalDate dateWorked, int hoursWorked) throws SQLException{
        //validate date
        if(dateWorked.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Sales recorded cannot be in the future");
        
        if(dateWorked.isBefore(LocalDate.now().minusYears(1)))
            throw new IllegalArgumentException("Sales recorded must be within the last 12 months");
        
        //validate hours worked
        if(hoursWorked <=0 || hoursWorked > 3000)
            throw new IllegalArgumentException("Hours worked must be in the range of $0-3000");
        
        //store in the database
        Connection conn = null;
        PreparedStatement ps = null;
        
        try{

                //1. connect to the database
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
                
                //2.create a prepared statement
                String sql = "Insert Into hoursWorked (userID, dateWorked, hoursWorked) VALUES (?,?,?)";
                
                //3.prpeare query
                ps = conn.prepareStatement(sql);
                
                //4.convert localdate to sql date
                Date dw = Date.valueOf(dateWorked);
                
                //5.bind the parameters
                ps.setInt(1, userID);
                ps.setDate(2,dw);
                ps.setInt(3, hoursWorked);
                
                //6. execute the update
                ps.executeUpdate();
        }
        catch(SQLException e)
            
            {
                 System.err.println(e.getMessage());
            }
        finally
        {
            if(conn != null)
                conn.close();
            if(ps != null)
                ps.close();
        }
    }
    
    /**
     * This method will create a new salt and encrypted password and store it in the database
     */
    
    public void changePassword(String newPassword) throws NoSuchAlgorithmException, SQLException
    {
         salt = PasswordGenerator.getSalt();
         password = PasswordGenerator.getSHA512Password(newPassword, salt);
         
           Connection conn = null;
        PreparedStatement preparedStatement = null;
        
        try
        {   //1.connect to db
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user?autoReconnect=true&useSSL=false", "root", "longlegs94");
            //2.create a String that holds our SQL command with ? for user inputs
            String sql = "UPDATE users SET password = ?, salt = ?" + "WHERE userID =?";
            
            //3.prepare the query against SQL injection
            preparedStatement = conn.prepareStatement(sql);
            
            //4.bind the parameters 
            preparedStatement.setString(1, password);
            preparedStatement.setBlob(2, new javax.sql.rowset.serial.SerialBlob(salt));
            preparedStatement.setInt(3, userID);
            
            //5. run the command on the SQL server
            preparedStatement.executeUpdate();
            preparedStatement.close();
               
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        finally
        { 
            if(conn!=null)
               conn.close();
             if(preparedStatement != null)
                preparedStatement.close();
        }
    }

}
