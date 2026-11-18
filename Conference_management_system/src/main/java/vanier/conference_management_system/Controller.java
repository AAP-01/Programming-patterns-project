package vanier.conference_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller
{
    @FXML private Label errorLabel;

    // In log-in.fxml
    @FXML
    protected void onSignUpHyperlinkClick(ActionEvent actionEvent) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("sign-up.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    // In sign-up.fxml
    @FXML
    protected void onLogInHyperlinkClick(ActionEvent actionEvent) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("log-in.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    // In log-in.fxml
    @FXML
    protected void onLogInButtonClick(ActionEvent actionEvent) throws IOException
    {
        if (true)   // REPLACE THIS WITH A CONDITION CHECKING IF THE INFORMATION EXISTS
        {
            Parent root = FXMLLoader.load(getClass().getResource("registered-conferences.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        }
        else
        {
            errorLabel.setVisible(true);
        }
    }

    // In sign-up.fxml
    @FXML
    protected void onSignUpButtonClick(ActionEvent actionEvent) throws IOException
    {
        if (true)   // REPLACE THIS WITH A CONDITION CHECKING IF THE INFORMATION IS VALID
        {
            Parent root = FXMLLoader.load(getClass().getResource("registered-conferences.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        }
        else
        {
            errorLabel.setVisible(true);
        }
    }

    // In registered-conferences.fxml
    @FXML
    protected void onLogOutButtonClick(ActionEvent actionEvent) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("log-in.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}
