package interfaces;

import java.util.List;

public interface ChatCallbackHandler {
    void handleMessage(String projectName, String message);
}
