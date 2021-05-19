package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;


public class CardInfoController {
    @FXML TextArea cardHistoryTextArea;
    @FXML Button   dismissButton;
    @FXML Label cardName;
    @FXML Label cardDesc;

    private Client client;
    private Stage  stage;
    private String projectname;


    public void set(Client c, Stage s, String projectname, String desc, String cardname) throws IOException {
        client = c;
        stage = s;
        cardName.setText(cardname);
        cardDesc.setText(desc);
        cardHistoryTextArea.appendText("\n");
        for(String event : client.getCardHistory(cardname, projectname)){
            cardHistoryTextArea.appendText(event);
        }
    }

    public void dismiss(){
        stage.close();
    }


}
