import client.ChatHelper;
import client.Client;
import controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WorthGUI extends Application {

    private static String ADDRESS;
    private static String REGISTRY_NAME;
    private static int TCP_PORT;
    private static int CHAT_PORT;
    private static int RMI_PORT;
    private static String CHAT_SOCKET_ADDR;

    @Override
    public void start(Stage primaryStage) throws Exception{


        ChatHelper chatHelper = new ChatHelper(CHAT_PORT, CHAT_SOCKET_ADDR);

        //todo add command line options
        Client worthClient = new Client(ADDRESS, TCP_PORT, RMI_PORT, REGISTRY_NAME, chatHelper);
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
        Options options = new Options();
        options.addOption("s", "server-address",true,"Server Address  - default local machine address");
        options.addOption("p", "tcp-port",    true,  "Server TCP Port   - default 6789");
        options.addOption("r", "rmi-port",    true,  "Server RMI Port   - default 6790");
        options.addOption("n", "registry-name",    true,  "RMI Registry name   - default WORTH-RMI");
        options.addOption("c", "chat-port", true,    "UDP Multicast chat port - default 5678");
        options.addOption("u", "chat-socket", true,    "UDP socket address  - default machine network ip address");
        options.addOption("h", "help",        false, "Prompt help dialog");

        HelpFormatter helpFormatter = new HelpFormatter();

        //try to parse command line. if argument missing using default
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if(args.length == 1 && (commandLine.hasOption("h")  || commandLine.hasOption("--help"))){
                throw new ParseException("help dialog");
            }

            if (commandLine.hasOption("s") || commandLine.hasOption("--server-address"))
                 ADDRESS = (commandLine.getOptionValues("s")[0]);
            else ADDRESS = InetAddress.getLocalHost().getHostAddress();

            if (commandLine.hasOption("p") || commandLine.hasOption("--tcp-port") )
                 TCP_PORT = Integer.parseInt(commandLine.getOptionValues("p")[0]);
            else TCP_PORT = 6789;

            if (commandLine.hasOption("r") || commandLine.hasOption("--rmi-port") )
                 RMI_PORT = Integer.parseInt(commandLine.getOptionValues("r")[0]);
            else RMI_PORT = 6790;

            if (commandLine.hasOption("c") || commandLine.hasOption("--chat-port"))
                CHAT_PORT = Integer.parseInt(commandLine.getOptionValues("d")[0]);
            else CHAT_PORT = 5678;

            if (commandLine.hasOption("u") || commandLine.hasOption("--chat-socket"))
                 CHAT_SOCKET_ADDR = commandLine.getOptionValues("u")[0];
            else CHAT_SOCKET_ADDR = InetAddress.getLocalHost().getHostAddress();

            if (commandLine.hasOption("n") || commandLine.hasOption("--registry-name"))
                 REGISTRY_NAME = commandLine.getOptionValues("n")[0];
            else REGISTRY_NAME = "WORTH-RMI";

        }catch(ParseException | UnknownHostException p){
            helpFormatter.printHelp("java WorthClient", options);
            System.exit(-1);
        }
        launch(args);
    }
}
