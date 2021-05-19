package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MoveCardController {

    @FXML
    ComboBox<String> comboBox;

    private Client client;
    private Stage  stage;
    private String projectname;
    private String currentlist;
    private String cardname;


    public void set(Client c, Stage s, String projectname, String currentlist, String cardname){
        client = c;
        stage = s;
        this.currentlist = currentlist;
        this.projectname = projectname;
        this.cardname = cardname;
        comboBox.getItems().addAll(
                "In Progress",
                "To Be Revised",
                "Done"
        );
    }

    public void moveCard() throws IOException {
        String destList = comboBox.getValue();
        boolean res = false;
        switch (destList) {
            case "In Progress":
                res = client.moveCard(projectname, currentlist, "inprogress", cardname); break;
            case "To Be Revised":
                res = client.moveCard(projectname, currentlist, "toberevised", cardname); break;
            case "Done":
                res = client.moveCard(projectname, currentlist, "done", cardname); break;
        }
        if(!res){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("An error occurred when moving card. Check starting list.");
            alert.setTitle("Oops...");
            alert.show();
        }
        stage.close();
    }

    public void cancelAction(){
        stage.close();
    }

}
