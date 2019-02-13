package cloud_storage.client.GUI;

import cloud_storage.client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Клиент");
        primaryStage.setScene(new Scene(root, 810, 630));
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST){
                Optional<ButtonType> result = new Caution(Alert.AlertType.CONFIRMATION,
                        "Вы точно хотите выйти?").showAndWait();
                if (result.isPresent() && result.get() == OK) {
                    Client.getInstance().closeClient();
                    Platform.exit();
                }else event.consume();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
