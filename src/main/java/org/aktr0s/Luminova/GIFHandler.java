package org.aktr0s.Luminova;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

public class GIFHandler {

    public static void imageModifyGIF(ImageHolder imageHolder, int gifFrameNumber, int gifTimeNumber,
                                int minThres, int maxThres, String sortMode, boolean maskReverseEnabled,
                                String sortDirection, boolean spanOffsetEnabled, int spanBarSize,
                                ProgressIndicator exportProgressIndicator, Label indicatorLabel, GUIController guiController) {
        if (imageHolder.getImage() != null) {
            // Open FileChooser to let the user choose where to save the GIF file
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GIF Files", "*.gif"));
            fileChooser.setInitialFileName("exported_animation.gif");

            // Show the save dialog and get the selected file
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                // Create a Task to handle the GIF export
                Task<Void> exportTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Pass the GUIController to manage UI
                        Platform.runLater(() -> {
                            guiController.turnUI(false);
                            exportProgressIndicator.setProgress(0);
                            exportProgressIndicator.setVisible(true);
                            indicatorLabel.setText("Sorting Images...");
                        });


                        int totalFrames = gifFrameNumber;
                        try (FileOutputStream output = new FileOutputStream(file)) {
                            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                            encoder.start(output);
                            encoder.setDelay(gifTimeNumber);
                            encoder.setRepeat(0);

                            for (int i = 0; i < totalFrames; i++) {
                                // Sort the image and add it to the GIF file buffer
                                Image sortedImage = ImageEditor.pixelSort(imageHolder.getImage(), minThres, maxThres, sortMode,
                                        maskReverseEnabled, sortDirection, spanOffsetEnabled, spanBarSize);
                                BufferedImage bufferedFrame = SwingFXUtils.fromFXImage(sortedImage, null);
                                encoder.addFrame(bufferedFrame);

                                // Update progress on the JavaFX Application thread
                                final int progress = i + 1; // Final to be used in the lambda
                                Platform.runLater(() -> {
                                    // Update the progress bar
                                    exportProgressIndicator.setProgress((double) progress / totalFrames);
                                });

                            }

                            encoder.finish();

                            Platform.runLater(() -> {
                                System.gc();
                                indicatorLabel.setText("");
                                exportProgressIndicator.setProgress(1);  // Set progress to 100% when done
                                guiController.turnUI(true);
                                GUI.showDialog("Success", "GIF export was successful", Alert.AlertType.INFORMATION);
                            });


                        } catch (Exception e) {
                            GUI.showDialog("An error occurred when exporting GIF:", e + "\n" + "\nReport this incident on GitHub with provided circumstances", Alert.AlertType.ERROR);
                        }

                        return null; // Task completion
                    }
                };

                exportTask.setOnFailed(event -> {
                    Throwable exception = exportTask.getException();
                    GUI.showDialog("Error", "Task failed: " + exception.getMessage(), Alert.AlertType.ERROR);
                });

                new Thread(exportTask).start();
            } else {
                // User canceled the file chooser, no export
                GUI.showDialog("Cancelled", "GIF export was canceled by the user.", Alert.AlertType.INFORMATION);
            }
        } else {
            GUI.showDialog("Warning:", "There is no image to export or you are trying to export a non-edited image", Alert.AlertType.WARNING);
        }
        System.gc();
    }


}
