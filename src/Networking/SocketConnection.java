package Networking;

import Controllers.GameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Random;

public class SocketConnection extends Observable implements NetworkConnection {
    private static final int port = 4444;
    private int count = 0;
    private String ipOfClient;
    private String ownUsername;
    private String usernameOfClient;
    private String ownHexColor;
    private String hexColorOfClient;
    private long ownTimeOfPackage;
    private long timeOfPackageOfClient;
    private double ySpeed;

    public String getIpOfClient() {
        return ipOfClient;
    }

    public String getOwnUsername() {
        return ownUsername;
    }

    public String getUsernameOfClient() {
        return usernameOfClient;
    }

    public String getOwnHexColor() {
        return ownHexColor;
    }

    public String getHexColorOfClient() {
        return hexColorOfClient;
    }

    public long getOwnTimeOfPackage() {
        return ownTimeOfPackage;
    }

    public long getTimeOfPackageOfClient() {
        return timeOfPackageOfClient;
    }

    public SocketConnection(String ipOfClient, String ownUsername, String ownHexColor, long ownTimeOfPackage) {
        this.ipOfClient = ipOfClient;
        this.ownUsername = ownUsername;

        Random r = new Random();
        ySpeed = -0.9 + (1 - 0.1) * r.nextDouble();

        StringBuilder stringBuilder = new StringBuilder(ownHexColor);
        stringBuilder.delete(0, 2);
        stringBuilder.delete(stringBuilder.toString().length() - 2, stringBuilder.toString().length());
        this.ownHexColor = ownHexColor = stringBuilder.toString();

        this.ownTimeOfPackage = ownTimeOfPackage;
        AnalyzeNetworkMessage analyzer = new AnalyzeNetworkMessage();
        this.addObserver(analyzer);

        String message = "username:" + ownUsername + "&&" + "hexColor:" + ownHexColor + "&&" + "timeOfPackage:" + ownTimeOfPackage +
                "&&" + "ySpeed:" + ySpeed;

        Thread openSocketThread = new Thread(() -> {
            try {
                socket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        openSocketThread.setDaemon(true);
        openSocketThread.start();
        while (!analyzer.handshakePackageReceived) {
            try {
                Thread.sleep(1000);
                sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        usernameOfClient = analyzer.username;
        hexColorOfClient = analyzer.hexColor;
        timeOfPackageOfClient = analyzer.timeOfPackage;
        if (ownTimeOfPackage < timeOfPackageOfClient) {
            GameController.ySpeed = analyzer.ySpeed;
        } else {
            GameController.ySpeed = ySpeed;
        }
    }

    public boolean sendMessage(String message) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(ipOfClient, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(message);
            System.out.println("sent " + message + " to " + ipOfClient);
            clientSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void socket() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.err.println("Started server on port " + port);

            // repeatedly wait for connections, and process
            while (true) {
                // a "blocking" call which waits until a connection is requested
                Socket clientSocket = serverSocket.accept();
                System.err.println("Accepted connection from client");

                // open up IO streams
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

                String receivedMessage;
                while ((receivedMessage = in.readLine()) != null) {
                    out.println(receivedMessage);
                    setChanged();
                    notifyObservers(receivedMessage);
                    System.out.println("received string: " + receivedMessage);
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
