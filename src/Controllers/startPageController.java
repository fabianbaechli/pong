package Controllers;

import java.io.IOException;
import java.net.*;
import java.util.*;

import javafx.application.Platform;
import javafx.fxml.*;
import com.jfoenix.controls.*;
import Networking.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class startPageController implements Initializable {

    @FXML
    private JFXTextField usernameTextField;

    @FXML
    private JFXTextField ipTextField;

    @FXML
    private JFXColorPicker userColor;

    @FXML
    private JFXButton connectButton;

    @FXML
    private JFXProgressBar handshakeProgress;
    private int count = 0;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        connectButton.setOnAction(event -> {
            handshakeProgress.setVisible(true);
            connectButton.setDisable(true);
            Thread establishConnection = new Thread() {
                public void run() {
                    NetworkConnection networkConnection = new SocketConnection(ipTextField.getText(),
                            usernameTextField.getText(), userColor.getValue().toString(), System.currentTimeMillis());
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (count < 1) {
                                    //Shows an error window, when the gateway wasn't set
                                    try {
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Graphics/Game.fxml"));
                                        Parent root = (Parent) loader.load();
                                        Stage stage = new Stage();
                                        stage.setTitle("Pong Game");
                                        stage.setScene(new Scene(root));
                                        stage.setResizable(false);

                                        //hides this current window
                                        Stage closingStage = (Stage) connectButton.getScene().getWindow();
                                        GameController controller = loader.getController();
                                        controller.setNetworkConnection(networkConnection);
                                        stage.show();
                                        closingStage.close();
                                        count++;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            establishConnection.setDaemon(true);
            establishConnection.start();

        });
    }
}