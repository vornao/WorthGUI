package client;

import utils.Printer;

import java.io.IOException;

public class ChatListener implements Runnable{
    private final ChatHelper chatHelper;

    public ChatListener(ChatHelper chatHelper){
        this.chatHelper = chatHelper;
    }

    @Override
    public void run() {
        Printer.println("> DEBUG: Chat thread started...", "yellow");
        while(!Thread.interrupted()) {
            chatHelper.startChatListener();
        }
        try {
            chatHelper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
