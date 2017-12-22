/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import models.User;

/**
 * FXML Controller class
 *
 * @author albert
 */
public class NewUserViewController implements Initializable, ControllerClass  {
    
    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField phoneTextField;
    @FXML private DatePicker birthday;
    @FXML private Label errMsgLabel;
    @FXML private Label headerLabel;
    @FXML private ImageView imageView;
    
    private File imageFile;
    private boolean imageFileChanged;
    private User user;
    //used for passwords
    @FXML private PasswordField pwField;
    @FXML private PasswordField confirmPwField;
    
    //used for controlling whether or not the user is an administrator
    @FXML private CheckBox adminCheckBox;
    @FXML private Label adminLabel;
    /**
     * This method will change back to the TableView of users without adding
     * a user. All data in the form will be lost
     */
    
    public void cancelButtonPushed(ActionEvent event) throws IOException
    {
        SceneChanger sc = new SceneChanger();
        if(SceneChanger.getLoggedInUser().isAdmin())
            sc.changeScenes(event, "UserTableView.fxml", "All Users");
        else
        {
        LogHoursViewController controller = new LogHoursViewController();
        sc.changeScenes(event,"LogHoursView.fxml", "Log Hours", user, controller);
        }
    }
    
    /**
     * When the button is pushed, a FileChooser object is launched to allow the user to browse for a new image file. When that is complete, it will update 
     * the view with a new image
     */
    public void chooseImageButtonPushed(ActionEvent event)
    {
        //get the Stage to open a new window 
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        
        //Instantiate a fileChooser object
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        
        //filter for .jpg and .png
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("Image File (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("Image File (*.png)", "*.png");
        fileChooser.getExtensionFilters().addAll(jpgFilter, pngFilter);
        
        //Set to the user's picture directory or user directory if not available 
        String userDirectoryString = System.getProperty("user.home") + "\\Pictures";
        File userDirectory = new File(userDirectoryString);
        
        //if you cannot navigate to the pictures directory, go to the user home
        
        if(!userDirectory.canRead())
            userDirectory = new File(System.getProperty("user.home"));
        
        fileChooser.setInitialDirectory(userDirectory);
        
        //open the file dialog window
        File tmpImageFile = fileChooser.showOpenDialog(stage);
        
        if(tmpImageFile != null)
          {
            imageFileChanged = true;
            imageFile = tmpImageFile;
          }
        
        
        //update the imageView with the new image
        if(imageFile.isFile())
        {
            try
            {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                Image img = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(img);
            }catch(IOException e)
            {
            System.err.println(e.getMessage());
            }
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        birthday.setValue(LocalDate.now().minusYears(18));
        
        //if the user logged in is not an admin user, do not show the admin 
        //checkbox
        if(!SceneChanger.getLoggedInUser().isAdmin())
        {
            adminCheckBox.setVisible(false);
            adminLabel.setVisible(false);
        }
        
        imageFileChanged = false; // initially the image has not changed, use the default
        
        
        errMsgLabel.setText(""); //set the error message to be empty to start
        
        //Load default image for the person
        try{
            imageFile = new File("./src/images/defaultimage.jpg");
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
            imageFileChanged = true;
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
        }
    }    
    
     /**
     * This method will read from the scene and try to create a new instance of a volunteer.
     * If a volunteer was successfully created, it is updated in the database.
     * @param event
     */
     public void saveUserButtonPushed(ActionEvent event)
     {
         if(validPassword() || user!= null)
         {
          try
            { 
            if(user !=null) // we need to edit/update an existing user
            {
                //update user information
                updateUser();
                user.updateUserInDB();
                
                //update password if it changed
                if(!pwField.getText().isEmpty())
                {
                    if(validPassword())
                    {
                        user.changePassword(pwField.getText());
                    }
                }
            }
            else
            {
             if(imageFileChanged)
              {
           user = new User(firstNameTextField.getText(), lastNameTextField.getText(), phoneTextField.getText(), birthday.getValue(), imageFile, adminCheckBox.isSelected(), pwField.getText()); 
        }
        else{
        
            user = new User(firstNameTextField.getText(), lastNameTextField.getText(), phoneTextField.getText(), birthday.getValue(), pwField.getText(), adminCheckBox.isSelected());    
            }
             errMsgLabel.setText("");  //do not show errors if creating User was successful
             user.insertIntoDB();
            }
           
             SceneChanger sc = new SceneChanger();
             sc.changeScenes(event,"UserTableView.fxml", "All Users");
        }
        catch (Exception e)
        {
            errMsgLabel.setText(e.getMessage());
        }  
         }
  
     }

    @Override
    public void preloadData(User user) {
        this.user = user;
        this.firstNameTextField.setText(user.getFirstName());
        this.lastNameTextField.setText(user.getLastName());
        this.birthday.setValue(user.getBirthday());
        this.phoneTextField.setText(user.getPhoneNumber());
        this.headerLabel.setText("Edit User");
        
        if(user.isAdmin())
            adminCheckBox.setSelected(true);
        
        //load image
        try{
            String imgLocation = ".\\src\\images\\" + user.getImageFile().getName();
            imageFile = new File(imgLocation);
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            Image img = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(img);
        }
        catch(IOException e)
                {
                    System.err.println(e.getMessage());
                }
    }
    
     /**
     * This method will update the user with data that is stored in the GUI's field
     */
     
    public void updateUser() throws IOException{
        user.setFirstName(firstNameTextField.getText());
        user.setLastName(lastNameTextField.getText());
        user.setPhoneNumber(phoneTextField.getText());
        user.setBirthday(birthday.getValue());
        user.setImageFile(imageFile);
        user.setAdmin(adminCheckBox.isSelected());
        
        if(imageFileChanged)
            user.copyImageFile();
        
        
    }
    /**
     * This method will validate that passwords match
     */
    
    /**
     * This method will validate that passwords match
     * @return
     */
    public boolean validPassword(){
        if(pwField.getText().length()<5)
        {
            errMsgLabel.setText("Passwords must be greater than five characters");
            return false;
        }
        
        if (pwField.getText().equals(confirmPwField.getText()))
            return true;
        
            else
                return false;
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
