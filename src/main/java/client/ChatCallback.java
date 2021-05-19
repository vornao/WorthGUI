package client;

import controllers.WorthController;
import interfaces.ChatCallbackHandler;

public class ChatCallback implements ChatCallbackHandler {
    WorthController controller;

    public ChatCallback(WorthController controller){
        this.controller = controller;
    }

    @Override
    public void handleMessage(String projectName,String message) {
        controller.notifyMessage(projectName, message);
    }
}
