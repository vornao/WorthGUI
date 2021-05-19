package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AddCardController {
    private Client client;
    private Stage stage;
    private String projectname;

    @FXML TextField cardName;
    @FXML TextField cardDesc;

    public void set(Client c, Stage s, String projectname){
        client = c;
        stage = s;
        this.projectname = projectname;
    }

    public void addCard(){
        if(!client.addCard(cardName.getText(), cardDesc.getText(), projectname)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("An error occurred when creating card. Try again.");
            alert.setTitle("Oops...");
            alert.show();

        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Success");
            alert.setContentText("Card created");
            alert.show();
        }
        stage.close();
    }

    public void cancelAction(){
        stage.close();
    }
}
