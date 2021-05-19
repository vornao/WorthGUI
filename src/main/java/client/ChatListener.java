package client;

import utils.Printer;

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
    }
}
