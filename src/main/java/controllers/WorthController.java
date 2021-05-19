package controllers;

import client.Client;
import components.CardLite;
import components.ProjectLite;
import components.UserLite;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class WorthController {
    private Client client;
    private final ConcurrentHashMap<String, ArrayList<String>> messages = new ConcurrentHashMap<>();
    @FXML TableView<ProjectLite> projectsTable;
    @FXML TableColumn<ProjectLite, String> projectColumn;
    @FXML TableView<UserLite> membersTable;
    @FXML TableColumn<UserLite, String> membersColumn;
    @FXML TableView<CardLite> cardTable;
    @FXML TableColumn<CardLite, String> cardNameColumn;
    @FXML TableColumn<CardLite, String> cardDescriptionColumn;
    @FXML TableColumn<CardLite, String> currentCardListColumn;
    @FXML Button newProjectButton;
    @FXML Button addMemberButton;
    @FXML Button newCardButton;
    @FXML Button moveCardButton;
    @FXML Button cardInfoButton;
    @FXML Button deleteProjectButton;
    @FXML TextArea chatArea;
    @FXML TextField chatTextBox;


    public void setClient(Client c){
        client = c;
        chatArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                try {
                    sendChat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //todo check for null values and change name
    public synchronized void updateView(){
        chatArea.clear();
        addMemberButton.setDisable(false);
        deleteProjectButton.setDisable(false);
        newCardButton.setDisable(false);
        disableCardButtons(true);
        String selectedProj = projectsTable.getSelectionModel().getSelectedItem().getName();
        ArrayList<UserLite> members = client.showMembers(selectedProj);
        ArrayList<CardLite> cards = client.listCards(selectedProj);

        membersColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        membersTable.getItems().setAll(members);

        cardNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cardDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descr"));
        currentCardListColumn.setCellValueFactory(new PropertyValueFactory<>("list"));
        cardTable.getItems().setAll(cards);
        if(messages.get(projectsTable.getSelectionModel().getSelectedItem().getName()) != null)
        for(String m : messages.get(projectsTable.getSelectionModel().getSelectedItem().getName())){
            chatArea.appendText(m + "\n");
        }

    }

    public synchronized void set(){
        addMemberButton.setDisable(true);
        newCardButton.setDisable(true);
        disableCardButtons(true);
        deleteProjectButton.setDisable(true);
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        projectsTable.getItems().setAll(client.listProjects());
        cardTable.getItems().clear();
        chatArea.clear();
        membersTable.getItems().clear();
    }

    public void addProject() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("addProject.fxml"));

        Parent root = loader.load();
        stage.setTitle("Add Project");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setOnCloseRequest(windowEvent -> { Platform.exit(); System.exit(0);});
        AddProjectController c = loader.getController();
        c.set(client, stage);
        stage.show();
    }

    public void addMember() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("addMember.fxml"));

        Parent root = loader.load();
        stage.setTitle("Add Member to: " + projectsTable.getSelectionModel().getSelectedItem().getName());
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        AddMemberController c = loader.getController();
        c.set(client, stage, projectsTable.getSelectionModel().getSelectedItem().getName());
        stage.show();
    }

    public void deleteProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure to want to delete project " + projectsTable.getSelectionModel().getSelectedItem().getName());
        alert.setTitle("Delete Project");
        Optional<ButtonType> res = alert.showAndWait();
        if(res.isEmpty()) return;
        else if(res.get() == ButtonType.OK) {
            if(!client.deleteProject(projectsTable.getSelectionModel().getSelectedItem().getName())){
                Alert al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Oops...");
                al.setContentText("Something went wrong while deleting project. Check if all cards were done and try again");
                al.show();
            }
        }
        set();
    }

    public void addCard() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("addCard.fxml"));
        Parent root = loader.load();
        stage.setTitle("Add New Card");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        AddCardController c = loader.getController();
        c.set(client, stage, projectsTable.getSelectionModel().getSelectedItem().getName());
        stage.show();
    }

    public void moveCard() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("moveCard.fxml"));

        Parent root = loader.load();
        stage.setTitle("Add New Card");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        MoveCardController c = loader.getController();
        c.set(client,
                stage,
                projectsTable.getSelectionModel().getSelectedItem().getName(),
                cardTable.getSelectionModel().getSelectedItem().getList(),
                cardTable.getSelectionModel().getSelectedItem().getName());
        stage.show();
    }

    public void showCardInfo() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("cardInfo.fxml"));

        Parent root = loader.load();
        stage.setTitle("Card Information");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        CardInfoController c = loader.getController();
        c.set(client,
                stage,
                projectsTable.getSelectionModel().getSelectedItem().getName(),
                cardTable.getSelectionModel().getSelectedItem().getDescr(),
                cardTable.getSelectionModel().getSelectedItem().getName());
        stage.show();
    }

    private void disableCardButtons(Boolean disabled){
        moveCardButton.setDisable(disabled);
        cardInfoButton.setDisable(disabled);
    }

    public void cardSelected(){
        disableCardButtons(false);
    }

    public void showUserList() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("listUsers.fxml"));

        Parent root = loader.load();
        stage.setTitle("WORTH Users");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        ListUsersControllers c = loader.getController();
        c.set(client, stage);
        c.fillTable();
        stage.show();
    }

    public synchronized void notifyMessage(String projectname, String message){
        if(!messages.containsKey(projectname)) messages.put(projectname, new ArrayList<>());
        ArrayList<String> localMsg = messages.get(projectname);
        localMsg.add(message);
        if (projectsTable.getSelectionModel().getSelectedItem().getName().equals(projectname)){
            chatArea.appendText(message + "\n");
        }
    }

    public void sendChat() throws IOException {
        String msg = chatTextBox.getText();
        String projectname = projectsTable.getSelectionModel().getSelectedItem().getName();
        chatTextBox.clear();
        client.sendChat(projectname, msg);
        if(!messages.containsKey(projectname)) messages.put(projectname, new ArrayList<>());
    }

    public void initialize(){

    }

}
