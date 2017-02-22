package Controllers;

import Networking.NetworkConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML
    Pane firstBat;

    @FXML
    Pane secondBat;

    @FXML
    Ellipse ball;

    @FXML
    StackPane groundPane;

    @FXML
    Label firstPlayerWinCount;

    @FXML
    Label secondPlayerWinCount;

    @FXML
    Label leftUsername;

    @FXML
    Label rightUsername;

    private NetworkConnection networkConnection;
    private static Pane userBat;
    private static Pane opponentBat;
    private double currentPos;
    public static double ySpeed;
    private double xSpeed = 1;
    private Difficulty gameDiff;
    private static Timeline ballMovement;

    private enum Difficulty {Hard, Medium, Easy}

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        gameDiff = Difficulty.Hard;
        groundPane.setOnMouseMoved(arg0 -> {
            if (arg0.getEventType() == MouseEvent.MOUSE_MOVED) {
                if ((currentPos - arg0.getSceneY() > 10 || currentPos - arg0.getSceneY() < -10) &&
                        (arg0.getSceneY() + 0.8 * (userBat.getHeight() / 2) < 400 &&
                                arg0.getSceneY() - 0.8 * (userBat.getHeight() / 2) > 0)) {
                    currentPos = arg0.getSceneY();
                    userBat.setLayoutY(arg0.getSceneY() - userBat.getHeight() / 2);
                    networkConnection.sendMessage(String.valueOf(arg0.getSceneY()));
                }
            }
        });
    }

    void setNetworkConnection(NetworkConnection networkConnection) {
        this.networkConnection = networkConnection;
        if (networkConnection.getOwnTimeOfPackage() > networkConnection.getTimeOfPackageOfClient()) {
            userBat = firstBat;
            opponentBat = secondBat;
            leftUsername.setText(networkConnection.getOwnUsername());
            rightUsername.setText(networkConnection.getUsernameOfClient());
        } else if (networkConnection.getOwnTimeOfPackage() < networkConnection.getTimeOfPackageOfClient()) {
            userBat = secondBat;
            opponentBat = firstBat;
            rightUsername.setText(networkConnection.getOwnUsername());
            leftUsername.setText(networkConnection.getUsernameOfClient());
        } else {
            userBat = firstBat;
            opponentBat = secondBat;
            leftUsername.setText(networkConnection.getOwnUsername());
            rightUsername.setText(networkConnection.getUsernameOfClient());
        }
        currentPos = 200;
        userBat.setStyle("-fx-background-color: #" + networkConnection.getOwnHexColor());
        opponentBat.setStyle("-fx-background-color: #" + networkConnection.getHexColorOfClient());

        ballMovement = new Timeline(new KeyFrame(Duration.millis(4), event -> {
            if (networkConnection.getOwnTimeOfPackage() > networkConnection.getTimeOfPackageOfClient() ||
                    networkConnection.getOwnTimeOfPackage() == networkConnection.getTimeOfPackageOfClient()) {
                ball.setLayoutX(ball.getLayoutX() + xSpeed);
                ball.setLayoutY(ball.getLayoutY() + ySpeed);
            }

            double firstBatStartPositionHeight = firstBat.getLayoutY();
            double firstBatEndPositionHeight = firstBat.getLayoutY() + firstBat.getHeight();
            double firstBatPositionWidth = firstBat.getLayoutX() + firstBat.getWidth();
            double secondBatStartPositionHeight = secondBat.getLayoutY();
            double secondBatEndPositionHeight = secondBat.getLayoutY() + secondBat.getHeight();
            double secondBatPositionWidth = secondBat.getLayoutX();
            if ((ball.getLayoutY() > secondBatStartPositionHeight && ball.getLayoutY() < secondBatEndPositionHeight && ball.getLayoutX() >= secondBatPositionWidth) ||
                    (ball.getLayoutY() > firstBatStartPositionHeight && ball.getLayoutY() < firstBatEndPositionHeight && ball.getLayoutX() <= firstBatPositionWidth)) {
                ballHitsBat();
            } else if ((ball.getLayoutX() < firstBatPositionWidth - firstBat.getWidth())) {
                ballOut("left");
            } else if (ball.getLayoutX() > secondBatPositionWidth + secondBat.getWidth()) {
                ballOut("right");
            } else if (ball.getLayoutY() <= 0 || ball.getLayoutY() >= 400) {
                ballHitsWall();
            }
        }));
        ballMovement.setCycleCount(-1);
        ballMovement.play();
    }

    public static void changeOpponentBatPosition(double posY) {
        opponentBat.setLayoutY(posY - userBat.getHeight() / 2);
    }

    private void ballHitsBat() {
        xSpeed = -1 * xSpeed;
        if (gameDiff == Difficulty.Medium) {
            xSpeed = (xSpeed / 20) + xSpeed;
            userBat.setPrefHeight(userBat.getHeight() - 1);
            opponentBat.setPrefHeight(userBat.getHeight() - 1);
        } else if (gameDiff == Difficulty.Easy) {
            xSpeed = (xSpeed / 30) + xSpeed;
            userBat.setPrefHeight(userBat.getHeight() - 0.5);
            opponentBat.setPrefHeight(userBat.getHeight() - 0.5);
        } else if (gameDiff == Difficulty.Hard) {
            xSpeed = (xSpeed / 10) + xSpeed;
            userBat.setPrefHeight(userBat.getHeight() - 5);
            opponentBat.setPrefHeight(userBat.getHeight() - 5);
        }
    }

    private void ballOut(String side) {
        int winCount;
        if (side.equals("right")) {
            winCount = Integer.parseInt(firstPlayerWinCount.getText());
            winCount += 1;
            firstPlayerWinCount.setText(Integer.toString(winCount));
            Random r = new Random();
            ySpeed = -0.9 + (1 - 0.1) * r.nextDouble();
            xSpeed = -1;
        } else if (side.equals("left")) {
            winCount = Integer.parseInt(secondPlayerWinCount.getText());
            winCount += 1;
            secondPlayerWinCount.setText(Integer.toString(winCount));
            Random r = new Random();
            ySpeed = -0.9 + (1 - 0.1) * r.nextDouble();
            xSpeed = 1;
        }
        Media succ = null;
        Media goofy = null;
        try {
            goofy = new Media(GameController.class.getResource("sounds/goofy.mp3").toURI().toString());
            succ = new Media(GameController.class.getResource("sounds/succ.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert succ != null;
        if (side.equals("left") && userBat.getLayoutX() < 100) {

            MediaPlayer mediaPlayer = new MediaPlayer(succ);
            mediaPlayer.play();
        } else if (side.equals("right") && userBat.getLayoutX() < 100) {

            MediaPlayer mediaPlayer = new MediaPlayer(goofy);
            mediaPlayer.play();
        } else if (side.equals("left") && userBat.getLayoutX() > 100) {
            MediaPlayer mediaPlayer = new MediaPlayer(goofy);
            mediaPlayer.play();
        } else if (side.equals("right") && userBat.getLayoutX() > 100) {
            MediaPlayer mediaPlayer = new MediaPlayer(succ);
            mediaPlayer.play();
        }
        if (networkConnection.getOwnTimeOfPackage() > networkConnection.getTimeOfPackageOfClient() ||
                true) {
            networkConnection.sendMessage("newYSpeed:" + ySpeed);

            ballMovement.play();
        }
        ball.setLayoutY(200);
        ball.setLayoutX(300);
        userBat.setPrefHeight(200);
        opponentBat.setPrefHeight(200);
    }

    private void ballHitsWall() {
        ySpeed = -1 * ySpeed;
    }
}
