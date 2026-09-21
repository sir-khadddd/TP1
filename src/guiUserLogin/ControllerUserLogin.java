package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *
 * @version 1.02		2026-09-16 Added the one time password login and forced password reset  
 */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
    	boolean loginResult = false;
    	
		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
     		// Don't provide too much information.  Don't say the username is invalid or the
     		// password is invalid.  Just say the pair is invalid.
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		// System.out.println("*** Username is valid");
        // Check if the login password matches the account password
        String actualPassword = theDatabase.getCurrentPassword();

        // If the admin has set a one time password for the user, that is the only credential
        // accepted until a new password has been created.
        if (theDatabase.getCurrentOneTimePasswordFlag()) {
                String oneTimePassword = theDatabase.getCurrentOneTimePassword();
                if (oneTimePassword == null || password.compareTo(oneTimePassword) != 0) {
                        ViewUserLogin.alertUsernamePasswordError.setContentText(
                                        "Incorrect username/password. Try again!");
                        ViewUserLogin.alertUsernamePasswordError.showAndWait();
                        return;
                }
                // The one time password was correct, so this requires a new password to be made
                doNewPassword(username);
                return;
        }
        if (password.compareTo(actualPassword) != 0) {
            ViewUserLogin.alertUsernamePasswordError.setContentText(
                            "Incorrect username/password. Try again!");
            ViewUserLogin.alertUsernamePasswordError.showAndWait();
            return;
        }
		
		// Establish this user's details
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    			theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    			theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    			theDatabase.getCurrentAdminRole(), 
    			theDatabase.getCurrentNewContributor(), theDatabase.getCurrentNewViewer());
    	
    	// See which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		// System.out.println("*** The number of roles: "+ numberOfRoles);
		if (numberOfRoles == 1) {
			// Single Account Home Page - The user has no choice here
			
			// Admin role
			if (user.getAdminRole()) {
				loginResult = theDatabase.loginAdmin(user);
				if (loginResult) {
					guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
				}
			} else if (user.getNewContributor()) {
				loginResult = theDatabase.loginContributor(user);
				if (loginResult) {
					guiContributor.ViewContributorHome.displayContributorHome(theStage, user);
				}
			} else if (user.getNewViewer()) {
				loginResult = theDatabase.loginViewer(user);
				if (loginResult) {
					guiViewer.ViewViewerHome.displayViewerHome(theStage, user);
				}
				// Other roles
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page - The user chooses which role to play
			// System.out.println("*** Going to displayMultipleRoleDispatch");
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		}
	}
	
    /**********
     * <p> Method: doNewPassword(String username) </p>
     *
     * <p> Description: Called after a user logs in with a one time password. They have to set a
     * new password here before they can go back to a home page.
     *
     * The dialog shows the password requirements and updates them as the user types, like the
     * first admin page does.
     *
     * Once the password is entered twice and accepted, it is saved, the one time password is
     * cleared so it cannot be used again, and the user returns to the login page.</p>
     *
     * @param username specifies the user who is setting a new password
     *
     */
    private static void doNewPassword(String username) {

        String errorMessage = "";

        while (true) {

        		// This is structured the same way the first admin page 
                Pane thePane = new Pane();
                thePane.setPrefSize(560, 420);

                Label label_NewPassword = new Label("New password:");
                label_NewPassword.setLayoutX(20);
                label_NewPassword.setLayoutY(20);
                PasswordField text_NewPassword = new PasswordField();
                text_NewPassword.setPromptText("Enter New Password");
                text_NewPassword.setLayoutX(170);
                text_NewPassword.setLayoutY(15);
                text_NewPassword.setPrefWidth(300);

                Label label_ConfirmPassword = new Label("Confirm password:");
                label_ConfirmPassword.setLayoutX(20);
                label_ConfirmPassword.setLayoutY(60);
                PasswordField text_ConfirmPassword = new PasswordField();
                text_ConfirmPassword.setPromptText("Enter New Password Again");
                text_ConfirmPassword.setLayoutX(170);
                text_ConfirmPassword.setLayoutY(55);
                text_ConfirmPassword.setPrefWidth(300);

                Label label_Error = new Label(errorMessage);
                label_Error.setLayoutX(20);
                label_Error.setLayoutY(95);
                label_Error.setTextFill(Color.RED);

                thePane.getChildren().addAll(label_NewPassword, text_NewPassword,
                                label_ConfirmPassword, text_ConfirmPassword, label_Error);

                // Show the requirements as they type
                passwordPopUpWindow.View.passwordRequirementDisplay(thePane, 20, 130, 540);
                text_NewPassword.textProperty().addListener((_, _, _) ->
                                passwordPopUpWindow.View.updateRequirements(text_NewPassword.getText()));

                Dialog<String> dialogNewPassword = new Dialog<String>();
                dialogNewPassword.setTitle("Create a New Password");
                dialogNewPassword.setHeaderText("Your one-time password has been accepted");
                dialogNewPassword.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialogNewPassword.getDialogPane().setContent(thePane);
                dialogNewPassword.setResultConverter((_) -> text_NewPassword.getText());

                Optional<String> result = dialogNewPassword.showAndWait();
                if (result.isEmpty()) continue;
                String newPassword = result.get();

                // Check the new password against the requirements
                if (!passwordPopUpWindow.Model.evaluatePassword(newPassword).isEmpty()) {
                        errorMessage = "One or more password requirements not satisfied!";
                        continue;
                }

                // The two entries must be the same
                if (newPassword.compareTo(text_ConfirmPassword.getText()) != 0) {
                        errorMessage = "The two passwords must match. Please try again!";
                        continue;
                }

                // Store the new password and remove the one time password so it cannot be reused
                theDatabase.updatePassword(username, newPassword);
                theDatabase.clearOneTimePassword(username);
                break;
        }

        // The user needs to log in again using the new password
        ViewUserLogin.displayUserLogin(theStage);
}
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}
