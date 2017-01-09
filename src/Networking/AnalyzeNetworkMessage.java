package Networking;

import Controllers.GameController;
import java.util.Observable;
import java.util.Observer;

public class AnalyzeNetworkMessage implements Observer {
    boolean handshakePackageReceived = false;
    String username;
    String hexColor;
    long timeOfPackage;
    double ySpeed;

    public void update(Observable o, Object arg) {
        String message = arg.toString();
        if (message.contains("username:") && message.contains("hexColor:") && message.contains("timeOfPackage:")) {
            handshakePackageReceived = true;
            username = message.split("&&")[0].replace("username:", "");
            hexColor = message.split("&&")[1].replace("hexColor:", "");
            timeOfPackage = Long.parseLong(message.split("&&")[2].replace("timeOfPackage:", ""));
            ySpeed = Double.parseDouble(message.split("&&")[3].replace("ySpeed:", ""));
        } else if (message.contains("newYSpeed:") && !message.contains("username:")) {
            GameController.ySpeed = Double.parseDouble(message.replace("newYSpeed:", ""));
        } else {
            GameController.changeOpponentBatPosition(Double.parseDouble(arg.toString()));
        }
    }
}
