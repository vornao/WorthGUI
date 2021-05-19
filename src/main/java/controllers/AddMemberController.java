package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddMemberController {
    private String projectname;
    private Client client;
    private Stage stage;

    @FXML TextField memberName;

    public void set(Client c, Stage s, String projectname){
        this.projectname = projectname;
        client = c;
        stage = s;
    }

    public void addMember(){
        if(!client.addMember(memberName.getText(), projectname)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("An error occurred when adding member. Try again.");
            alert.setTitle("Error!");
            alert.show();

        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Success");
            alert.setContentText("Member added");
            alert.show();
        }
        stage.close();
    }

    public void cancelAction(){
        stage.close();
    }
}