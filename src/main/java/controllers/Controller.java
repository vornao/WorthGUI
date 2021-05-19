package controllers;

import client.ChatCallback;
import client.ChatHelper;
import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    private Client client;
    private ChatHelper chatHelper;
    private Stage parentStage;

    @FXML
    TextField username;
    @FXML
    PasswordField password;
    @FXML

    public void initialize(){}

    public void setClient(Client client){
        this.client = client;
    }

    public void setChatHelper(ChatHelper chatHelper){ this.chatHelper = chatHelper; }

    public void setParentStage(Stage parentStage){
        this.parentStage = parentStage;
    }

    public void loginButtonClicked() throws IOException {
        if(client.login(username.getText(), password.getText())){
            parentStage.close();
            startProjectPanel();
        }
    }

    public void signupButtonClicked() throws IOException {
        if(client.signUp(username.getText(), password.getText())){
            client.login(username.getText(), password.getText());
            parentStage.close();
            startProjectPanel();
        }
    }

    private void startProjectPanel() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("worth.fxml"));

        Parent root = loader.load();
        stage.setTitle("WORTH GUI");
        stage.setScene(new Scene(root));
        stage.setResizable(true);
        stage.setOnCloseRequest(windowEvent ->{ Platform.exit();
            try {
                client.quit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        WorthController c = loader.getController();

        c.setClient(client);
        chatHelper.setCallbackHandler(new ChatCallback(c));
        c.set();
        stage.show();
    }
}
