package org.aktr0s.Luminova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;


import java.io.IOException;
import java.util.Objects;

public class GUI extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(GUI.class.getResource("Pixel_Sort_GUI.fxml"));
        GUIController controller = new GUIController();
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("luminova.css")).toExternalForm());
        stage.setTitle("Luminova: Image Pixel Sorter");
        stage.getIcons().add(new Image(Objects.requireNonNull(GUI.class.getResource("LuminovaLogo2.png")).toString()));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void showDialog(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(GUI.class.getResource("LuminovaLogo.png")).toString()));
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Scene alertScene = alert.getDialogPane().getScene();
        alertScene.getStylesheets().add(Objects.requireNonNull(primaryStage.getScene().getStylesheets().get(0)));

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
