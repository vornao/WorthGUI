package controllers;

import client.Client;
import components.UserLite;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ListUsersControllers {
    private Client client;
    private Stage stage;

    @FXML Button okButton;
    @FXML TableView<UserLite> usersTable;
    @FXML TableColumn<UserLite, String> usernameColumn;
    @FXML TableColumn<UserLite, String> statusColumn;
    @FXML CheckBox onlineCheck;

    public void set(Client client, Stage stage){
        this.client = client;
        this.stage = stage;
    }

    public void fillTable(){
        ArrayList<UserLite> users = client.listUsers(false);
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        usersTable.getItems().setAll(users);
    }

    public void reloadList(){
        usersTable.getItems().clear();
        ArrayList<UserLite> users = client.listUsers(onlineCheck.isSelected());
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        usersTable.getItems().setAll(users);
    }

    public void close(){
        stage.close();
    }
}
