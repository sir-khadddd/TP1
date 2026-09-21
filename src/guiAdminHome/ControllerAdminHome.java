package guiAdminHome;

import database.Database;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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
 * @version 1.02		2026-09-16 Added one time password login and forced password reset
 * @version 1.03        2026-09-19 Added delete user account feature
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
    /**********
     * <p>
     *
     * Title: setOneTimePassword () Method. </p>
     *
     * <p> Description: Protected method that allows the admin to set a one time password for a user
     * who forgot their password. The admin selects the user from the list of all the users in
     * the system, then a one time password is generated and stored for that user, and it is displayed so
     * the admin can pass it along. The user logs in with it and is required to create a new
     * password and after the one time password is cleared. </p>
     */
    protected static void setOneTimePassword () {

    		// Fetch the list of users for the admin to choose from. The list always starts with the
            // "<Select a User>" prompt, so a list of one means there are no users at all.
            List<String> userList = theDatabase.getUserList();
            if (userList == null || userList.size() < 2) {
                    ViewAdminHome.alertOneTimePassword.setTitle("One-Time Password");
                    ViewAdminHome.alertOneTimePassword.setHeaderText("No Users");
                    ViewAdminHome.alertOneTimePassword.setContentText(
                                    "There are no users in the system.");
                    ViewAdminHome.alertOneTimePassword.showAndWait();
                    return;
            }

            // Ask the admin which user forgot their password
            ChoiceDialog<String> dialog = new ChoiceDialog<String>(userList.get(0), userList);
            dialog.setTitle("Set a One-Time Password");
            dialog.setHeaderText("Select the user who forgot their password");
            dialog.setContentText("User:");
            Optional<String> selection = dialog.showAndWait();

            // Do nothing if the admin cancels or leaves the prompt selected
            if (selection.isEmpty()) {
            	return;
            }
            String theSelectedUser = selection.get();
            if (theSelectedUser.compareTo("<Select a User>") == 0) {
            	return;
            }

            // Generate and store the one time password and then show it to admin
            String oneTimePassword = theDatabase.setOneTimePassword(theSelectedUser);
            String msg = "One-time password for " + theSelectedUser + " is: " + oneTimePassword;
            System.out.println(msg);
            ViewAdminHome.alertOneTimePassword.setTitle("One-Time Password");
            ViewAdminHome.alertOneTimePassword.setHeaderText("One-Time Password Set");
            ViewAdminHome.alertOneTimePassword.setContentText(msg);
            ViewAdminHome.alertOneTimePassword.showAndWait();
    }
	
	
    /**********
     * <p>
     *
     * Title: deleteUser () Method. </p>
     *
     * <p> Description: Protected method that allows the admin to remove a user from the system so
     * that person can no longer log in. The admin chooses the user from a list of all other
     * users and must answer "Yes" to an "Are you sure?" question before anything is removed.
     * An admin is not allowed to remove their own access, so the current admin's username is
     * not offered in the list of users that can be deleted. </p>
     */
    protected static void deleteUser() {

            // Get the list of users and take out the admin doing the deleting
            List<String> userList = theDatabase.getUserList();
            if (userList == null) {
            	return;
            }
            userList.remove(ViewAdminHome.theUser.getUserName());

            // The list always starts with the "<Select a User>" prompt, so a list of one means that there
            // is nobody this admin is allowed to delete.
            Alert alertNoUsers = new Alert(AlertType.INFORMATION);
            if (userList.size() < 2) {
                    alertNoUsers.setTitle("Delete a User");
                    alertNoUsers.setHeaderText("No Users");
                    alertNoUsers.setContentText("There are no other users to delete.");
                    alertNoUsers.showAndWait();
                    return;
            }
            // Ask admin which user to delete
            ChoiceDialog<String> dialog = new ChoiceDialog<String>(userList.get(0), userList);
            dialog.setTitle("Delete a User");
            dialog.setHeaderText("Select the user to be deleted");
            dialog.setContentText("User:");
            Optional<String> selection = dialog.showAndWait();

            if (selection.isEmpty()) return;
            String theSelectedUser = selection.get();
            if (theSelectedUser.compareTo("<Select a User>") == 0) return;

            // Admin can not delete their own account
            if (theSelectedUser.compareTo(ViewAdminHome.theUser.getUserName()) == 0) {
                    alertNoUsers.setTitle("Delete a User");
                    alertNoUsers.setHeaderText("Not Allowed");
                    alertNoUsers.setContentText("An admin may not remove their own access.");
                    alertNoUsers.showAndWait();
                    return;
            }

            // Require a yes before removal
            ButtonType buttonYes = new ButtonType("Yes");
            ButtonType buttonNo = new ButtonType("No");
            ViewAdminHome.alertDeleteUser.getButtonTypes().setAll(buttonYes, buttonNo);
            ViewAdminHome.alertDeleteUser.setTitle("Delete a User");
            ViewAdminHome.alertDeleteUser.setHeaderText("Are you sure?");
            ViewAdminHome.alertDeleteUser.setContentText(
                            "This will remove all access for " + theSelectedUser + ".");
            Optional<ButtonType> answer = ViewAdminHome.alertDeleteUser.showAndWait();

            // Anything other than yes leaves the user in place
            if (answer.isEmpty() || answer.get() != buttonYes) {
            	return;
            }

            // Remove the user and report what happened
            if (theDatabase.deleteUser(theSelectedUser)) {
                    System.out.println("*** User " + theSelectedUser + " has been deleted");
                    alertNoUsers.setTitle("Delete a User");
                    alertNoUsers.setHeaderText("User Deleted");
                    alertNoUsers.setContentText(theSelectedUser + " has been removed from the system.");
                    alertNoUsers.showAndWait();

                    // The number of users on the admin home
                    ViewAdminHome.label_NumberOfUsers.setText("Number of users: " +
                                    theDatabase.getNumberOfUsers());
            } else {
                    alertNoUsers.setTitle("Delete a User");
                    alertNoUsers.setHeaderText("Delete Failed");
                    alertNoUsers.setContentText("The user could not be deleted.");
                    alertNoUsers.showAndWait();
            }
    }
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void listUsers() {
		System.out.println("\n*** WARNING ***: List Users Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("List User Issue");
		ViewAdminHome.alertNotImplemented.setContentText("List Users Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
