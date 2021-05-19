import client.ChatHelper;
import client.Client;
import controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WorthGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ChatHelper chatHelper = new ChatHelper(5678, "192.168.1.3");

        //todo add command line options
        Client worthClient = new Client("192.168.1.3", 6789, 6790, "WORTH-RMI", chatHelper);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("WORTH GUI Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        Controller controller = loader.getController();
        controller.setClient(worthClient);
        controller.setChatHelper(chatHelper);
        controller.setParentStage(primaryStage);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
