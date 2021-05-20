package client;

import com.google.gson.*;
import components.CardLite;
import components.ProjectLite;
import components.UserLite;
import exceptions.ProjectNotFoundException;
import interfaces.RMIClientInterface;
import interfaces.RMIServerInterface;
import utils.Const;
import utils.Printer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//IMPORTANT: TO RUN WITH GSON LIBRARY RUN EXPORT
//export CLASSPATH=$CLASSPATH:/Users/vornao/Developer/university/WORTH/lib/gson-2.8.6.jar

public class Client {

    private final SocketChannel socketChannel;
    private final ByteBuffer buffer;
    private final ConcurrentHashMap<String, Boolean> worthUsers = new ConcurrentHashMap<>();
    private final ChatHelper chatHelper;

    private String    loginName = null;
    private RMIClient callbackAgent;

    Registry           registry;
    RMIServerInterface remote;
    Thread             chatThread;
    Gson               gson = new Gson();

    //todo server side return different codes
    //todo create enum to convert server codes into fancy string
    private static final HashMap<Integer, String> SignupErrorMessages = new HashMap<>() {
        {
            put(1, "> User created successfully");
            put(2, "> User creation failed, try again");
            put(0, "> Username already exists, try again");
        }
    };

    private static final HashMap<String, String> returnCodes = new HashMap<>() {
        {
            put("200", Const.ANSI_GREEN +"> OK - 200 - Done" + Const.ANSI_RESET);
            put("201", Const.ANSI_GREEN + "> OK - 201 - Resource Created" + Const.ANSI_RESET);
            put("404", Const.ANSI_RED+ "> ERROR: 404 - Resource not found" + Const.ANSI_RESET);
            put("401", Const.ANSI_RED+ "> ERROR: 401 - Unauthorized" + Const.ANSI_RESET);
            put("403", Const.ANSI_RED+ "> ERROR: 403 - Forbidden" + Const.ANSI_RESET);
            put("409", Const.ANSI_RED+ "> ERROR: 409 - Resource already exists" + Const.ANSI_RESET);
        }
    };

    public Client(String address, int port, int rmiport, String registry, ChatHelper chatHelper) throws IOException, NotBoundException {
        //todo parse command line arguments
        Printer.println("Starting client with target: " + address, "yellow");
        this.chatHelper = chatHelper;
        this.registry = LocateRegistry.getRegistry(address, rmiport);
        remote = (RMIServerInterface) this.registry.lookup(registry);

        socketChannel = SocketChannel.open();

        try {
            socketChannel.connect(new InetSocketAddress(address, port));
        } catch (IOException e) {
            System.out.println(Const.ANSI_RED + "ERROR: Failed to connect to server." + Const.ANSI_RESET);
            System.exit(-1);
        }

        buffer = ByteBuffer.wrap(new byte[8192]);
        buffer.clear();
        Printer.println(String.format("> DEBUG: Connection established to WORTH server @ %s:%d", address, port), "yellow");
        ChatListener cl = new ChatListener(chatHelper);
        chatThread = new Thread(cl);
    }

    /**
     * connects to RMI server in order to let user sign up, if signup returns errors, an error message
     * is displayed to console.
     * If something goes wrong exception is catched and error message is printed as well.
     */
    public boolean signUp(String username, String password) {
        try {

            int status = remote.signUp(username, password);
            Printer.println(SignupErrorMessages.get(status), "yellow");
            if(status == 1){
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Log in method. First checks if user is logged in
     * if already logged in, prints red error message;
     * else will send a login json-formatted message to server, and waits for response
     * finally parses response to local object and displays response to user.
     * If something else goes wrong IOException or JSON format exception is catched and mesasge is displayed
     */

    public boolean login(String username, String password) {

        if(loginName != null){
            return false;
        }

        try {

            JsonObject loginJson = new JsonObject();
            loginJson.addProperty("method", "login");
            loginJson.addProperty("username", username);
            loginJson.addProperty("password", password);

            writeSocket(loginJson.toString().getBytes());

            String responseString = readSocket();

            //System.out.println(buffer);
            //System.out.println("> DEBUG: RECEIVED: " + responseString);

            //parsing response as generic JsonObject
            JsonObject response = gson.fromJson(responseString, JsonObject.class);

            //unnecessary, because we're checking socket read above, just a reminder.
            assert response != null;

            //check if login was successful and notify user.
            if (response.get("return-code").getAsString().equals("200")) {
                Printer.println("< Login Successful!", "green");
                this.loginName = username; //associating current login name to session for future requests
            } else {
                Printer.println("< Login failed: " + response.get("return-code").getAsString(), "red");
                return false;
            }

            //parse users to local map
            JsonArray userlist = response.get("registered-users").getAsJsonArray();

            for (JsonElement j : userlist) {
                JsonObject user = (JsonObject) j;
                worthUsers.put(user.get("username").getAsString(), (user.get("status").getAsBoolean()));
            }

            //join to project chats
            for(JsonElement e : response.get("projects-list").getAsJsonArray()){
                JsonObject o = e.getAsJsonObject();
                chatHelper.joinGroup(o.get("chat-addr").getAsString(), o.get("name").getAsString());
            }

        } catch (IOException | JsonIOException e) {
            e.printStackTrace();
            Printer.println("< ERROR: Error sending message to server", "red");
        }

        //todo ask for project list in order to retreive chat info
        registerForCallback();
        chatThread.start();
        return true;
    }

    /**
     *
     * Log out method. First checks if user is logged in
     * If not logged in print red error message;
     * else will send a logout json-formatted message to server, and wait for response
     * finally displays response to user.
     * If something else goes wrong IOException or JSON format exception is catched and mesasge is displayed
     */

    public void logout() throws IOException {

        if (loginName == null) {
            Printer.println("< ERROR: Log you in before logging out!", "red");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("method", "logout");
        request.addProperty("username", loginName);

        try {
            writeSocket(request.toString().getBytes());

            //read response from server
            String responseString = readSocket();
            JsonObject response = gson.fromJson(responseString, JsonObject.class);

            if (!response.get("return-code").getAsString().equals("200")) {
                Printer.println("< ERROR: Error logging out!", "red");
            }

            loginName = null;
            remote.unregisterForCallback(callbackAgent);

        } catch (IOException | JsonIOException e) {
            Printer.println("< Error sending message to server", "red");
            e.printStackTrace();
        }
        socketChannel.close();
    }

    /**
     *  Display registered users with nice looking ASCII table
     *  if user is not logged in prints red error message and returns.
     */

    public boolean createProject(String projectname) {
        if(loginName == null){
            return false;
        }

        JsonObject request = new JsonObject();
        request.addProperty("username", loginName);
        request.addProperty("method", "create-project");
        request.addProperty("projectname", projectname);

        writeSocket(request.toString().getBytes());

        String returnCode = gson.fromJson(readSocket(), JsonObject.class).get("return-code").getAsString();
        return returnCode.equals("201");
    }

    public boolean addMember(String name, String projectname) {
        if(loginName == null){
            Printer.println("< ERROR: you must log in before adding new users", "red");
            return false;
        }

        JsonObject request = new JsonObject();
        request.addProperty("method", "add-member");
        request.addProperty("username", loginName);
        request.addProperty("projectname", projectname);
        request.addProperty("new-member", name);
        writeSocket(request.toString().getBytes());

        String returnCode = gson.fromJson(readSocket(), JsonObject.class).get("return-code").getAsString();
        System.out.println(returnCodes.get(returnCode));
        return "201".equals(returnCode);
    }

    public boolean moveCard(String projectname, String from, String to, String name) throws IOException{
        if(loginName == null){
            return false;
        }

        JsonObject request = new JsonObject();
        request.addProperty("method", "move-card");
        request.addProperty("username", loginName);
        request.addProperty("projectname", projectname);
        request.addProperty("cardname", name);
        request.addProperty("from", from);
        request.addProperty("to", to);

        writeSocket(request.toString().getBytes());
        String returnCode = gson.fromJson(readSocket(), JsonObject.class).get("return-code").getAsString();
        System.out.println(returnCodes.get(returnCode));

        if("200".equals(returnCode)){
            String notification = String.format("%s moved card %s from list \"%s\" to list \"%s\"", loginName, name, from, to);
            try {
                chatHelper.sendMessage(projectname, "Project " + projectname, notification);
                return true;
            }catch (ProjectNotFoundException e){
                Printer.print("> ERROR: Failed to send message: project not found.", "red");
            }
        }
        return false;
    }

    public boolean addCard(String cardname, String cardDescr, String projectname) {
        if(loginName == null){
            return false;
        }

        JsonObject request = new JsonObject();
        request.addProperty("username", loginName);
        request.addProperty("method", "add-card");
        request.addProperty("projectname", projectname);
        request.addProperty("cardname", cardname);
        request.addProperty("cardesc", cardDescr);

        writeSocket(request.toString().getBytes());
        String returnCode = gson.fromJson(readSocket(), JsonObject.class).get("return-code").getAsString();
        return "201".equals(returnCode);
    }

    public boolean deleteProject(String projectname) {
        if(loginName == null){
            return false;
        }

        JsonObject request = new JsonObject();
        request.addProperty("method", "delete-project");
        request.addProperty("username", loginName);
        request.addProperty("projectname", projectname);

        writeSocket(request.toString().getBytes());

        JsonObject response = gson.fromJson(readSocket(), JsonObject.class);
        System.out.println(returnCodes.get(response.get("return-code").getAsString()));
        return "200".equals(response.get("return-code").getAsString());

    }

    public ArrayList<ProjectLite> listProjects(){
        if(loginName == null){
            return null;
        }
        ArrayList<ProjectLite> projects = new ArrayList<>();
        JsonObject request = new JsonObject();
        request.addProperty("username", loginName);
        request.addProperty("method", "list-projects");

        writeSocket(request.toString().getBytes());
        String response = readSocket();

        JsonArray json = gson.fromJson(response, JsonObject.class).get("projects").getAsJsonArray();
        for(JsonElement e : json){
            JsonObject o = e.getAsJsonObject();
            projects.add(new ProjectLite(o.get("name").getAsString()));
        }
        return projects;
    }

    public ArrayList<UserLite> listUsers(Boolean onlyOnlineUsers){

        ArrayList<UserLite> users = new ArrayList<>();

        if(loginName == null) return null;

        for (Map.Entry<String, Boolean> entry : worthUsers.entrySet()) {
            String status = "offline";
            if(entry.getValue()) status = "online";
            if(onlyOnlineUsers) { if (status.equals("online")) users.add(new UserLite(entry.getKey(), status)); }
            else users.add(new UserLite(entry.getKey(), status));
        }
        return users;
    }

    public ArrayList<UserLite> showMembers(String projectname) {
        ArrayList<UserLite> members = new ArrayList<>();
        if(loginName == null){
            Printer.println("< ERROR: you must log in before adding new users", "red");
            return null;
        }

        JsonObject request = new JsonObject();
        request.addProperty("username", loginName);
        request.addProperty("method", "show-members");
        request.addProperty("projectname", projectname);

        writeSocket(request.toString().getBytes());
        JsonObject response = gson.fromJson(readSocket(), JsonObject.class);

        String returnCode = response.get("return-code").getAsString();
        if(!returnCode.equals("200")){
            System.out.println(returnCodes.get(returnCode));
            return null;
        }

        JsonArray projects = response.get("members").getAsJsonArray();

        for(JsonElement e : projects){
            members.add( new UserLite(e.getAsString(), null));
        }
        return members;
    }

    public ArrayList<CardLite> listCards(String projectname){

        ArrayList<CardLite> cards = new ArrayList<>();

        if(loginName == null) {
            Printer.println("> ERROR: You must log in to see registered users.", "red");
            return null;
        }

        JsonObject request = new JsonObject();
        request.addProperty("username", loginName);
        request.addProperty("method", "list-cards");
        request.addProperty("projectname", projectname);

        writeSocket(request.toString().getBytes());
        JsonObject response = gson.fromJson(readSocket(), JsonObject.class);
        String statusCode = response.get("return-code").getAsString();

        if(!statusCode.equals("200")){
            System.out.println(returnCodes.get(statusCode));
            return null;
        }

        JsonArray cardsjson = response.get("card-list").getAsJsonArray();

        //green online string, offline label will be printed with default terminal color.
        for(JsonElement e : cardsjson){
            cards.add(
                    new CardLite(
                    e.getAsJsonObject().get("card-name").getAsString(),
                    e.getAsJsonObject().get("card-desc").getAsString(),
                    e.getAsJsonObject().get("card-state").getAsString()));
        }
        return cards;
    }

    public ArrayList<String> getCardHistory(String name, String projectname) {

        ArrayList<String> cardHistory = new ArrayList<>();
        if(loginName == null){
            return null;
        }

        JsonObject request = new JsonObject();
        request.addProperty("method", "get-card-history");
        request.addProperty("username", loginName);
        request.addProperty("projectname", projectname);
        request.addProperty("cardname", name);

        writeSocket(request.toString().getBytes());

        JsonObject response = gson.fromJson(readSocket(), JsonObject.class);

        if(!response.get("return-code").getAsString().equals("200")){
            System.out.println(returnCodes.get(response.get("return-code").getAsString()));
            return null;
        }

        JsonArray history = response.getAsJsonArray("card-history");

        for(JsonElement e : history){

            int timestamp = e.getAsJsonObject().get("date").getAsInt();
            String start = e.getAsJsonObject().get("from").getAsString();
            String end = e.getAsJsonObject().get("to").getAsString();
            String date =
                    new java.text.SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
                    .format(new Date (timestamp* 1000L));
            cardHistory.add(String.format("- %s - Moved from: %s to %s\n", date, start, end));
        }
        return cardHistory;
    }

    private void registerForCallback(){
        callbackAgent = new RMIClient(worthUsers, chatHelper, this.loginName);

        try {
            RMIClientInterface stub = (RMIClientInterface) UnicastRemoteObject.exportObject(callbackAgent, 0);
            remote.registerForCallback(stub);

        }catch (RemoteException e){
            e.printStackTrace();
            Printer.println("> ERROR: (RMI) cannot subscribe to server callback", "red");
        }
    }

    /** logout and quit client */
    public void quit() throws IOException {
        if(loginName != null) logout();
        chatThread.interrupt();
        System.exit(0);
    }

    public void sendChat(String projectname, String text) throws IOException {
        if(loginName == null) return;
        try {
            chatHelper.sendMessage(projectname, loginName, text);
        }catch (ProjectNotFoundException ignored){

        }
    }

    private void writeSocket(byte[] request){
        try {
            buffer.clear();
            buffer.put(request);
            buffer.flip();
            while (buffer.hasRemaining()) socketChannel.write(buffer);
            buffer.clear();
        }catch(IOException e){
            Printer.println("< Failed sending message to server. Try Again",  "red");
        }
    }

    private String readSocket(){
        try {
            if (socketChannel.read(buffer) < 0) {
                Printer.println("< ERROR: Server closed connection unexpectedly.", "red");
                System.exit(-1);
            }
            buffer.flip();
            String responseString;
            responseString = StandardCharsets.UTF_8.decode(buffer).toString();
            buffer.clear();
            return responseString;

        }catch(IOException e){
            Printer.println("< ERROR: Server closed connection unexpectedly.", "red");
        }
        return null;
    }

}