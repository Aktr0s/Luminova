package org.aktr0s.Luminova;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.File;


public class ImageHandler {

    public static Image loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            return new Image(file.toURI().toString());
        }
        return null;
    }

    public static void exportImage(Stage stage, Image image) {
        if (image != null) {

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            fileChooser.setInitialFileName("exported_image.png");

            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    // Convert Image to WritableImage
                    WritableImage writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
                    PixelReader pixelReader = image.getPixelReader();

                    if (pixelReader != null) {
                        writableImage.getPixelWriter().setPixels(0, 0,
                                (int) image.getWidth(), (int) image.getHeight(),
                                pixelReader, 0, 0);
                    }

                    // Save the image
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                } catch (Exception e) {
                    //e.printStackTrace();
                    GUI.showDialog("An error occurred when exporting image:",e + "\n" + "\nReport this incident on github with provided circumstances", Alert.AlertType.ERROR);
                }
            }
        } else {
            GUI.showDialog("Warning:","There is no image to export or you are trying to export non-edited image", Alert.AlertType.WARNING);
        }
    }

}

