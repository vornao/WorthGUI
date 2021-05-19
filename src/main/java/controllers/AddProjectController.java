package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class AddProjectController {

    private Client client;
    private Stage stage;

    @FXML
    TextField projectname;

    public void set(Client c, Stage s){
        client = c;
        stage = s;
    }

    public void addProject() {
        if(!client.createProject(projectname.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("An error occurred when creating project. Try again.");
            alert.setTitle("Error!");
            alert.show();

        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Success");
            alert.setContentText("Project created");
            alert.show();
        }
        stage.close();
    }

    public void cancelAction(){
        stage.close();
    }
}
