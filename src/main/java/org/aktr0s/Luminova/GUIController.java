package org.aktr0s.Luminova;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class GUIController {

    // ImageHolder instance
    private ImageHolder imageHolder;

    // FXML components
    @FXML private Slider minThreshold, maxThreshold, moveThreshold, spanSize;
    @FXML private ToggleGroup sortmode, sortdirection;
    @FXML private RadioButton lumaOption, hueOption, redOption, greenOption, blueOption;
    @FXML private RadioButton ttbOption, bttOption, ltrOption, rtlOption;
    @FXML private CheckBox reverseCheck, spanOffsetCheck;
    @FXML private Button importButton, exportButton, previewModeToggleButton, resetButton, regenerateImage, exportGIFButton;
    @FXML private ImageView imagePreview;
    @FXML private TextField spanSizeTextField;
    @FXML private Spinner<Integer> frameNumberSpinner, frameTimeSpinner;
    @FXML private ProgressIndicator exportProgressIndicator;
    @FXML private Label indicatorLabel;

    //values for the image sorting
    private String sortMode, sortDirection;
    private Boolean maskPreviewEnabled = false, maskReverseEnabled, spanOffsetEnabled = false;
    private Integer minThres, maxThres, spanBarSize;
    private Integer gifFrameNumber, gifTimeNumber;

    @FXML
    public void initialize() {
        System.out.println("Controller initialized!");
        setupNumericSpinner(frameNumberSpinner,35,2,100);
        setupNumericSpinner(frameTimeSpinner,50,10,2000);

        // Initialize image holder
        imageHolder = new ImageHolder();

        final double[] offset = {maxThreshold.getValue() - minThreshold.getValue()};

        // Update offset whenever min or max sliders are changed manually
        minThreshold.valueProperty().addListener((_, _, newValue) -> {
            offset[0] = maxThreshold.getValue() - newValue.doubleValue();
            moveThreshold.setValue(minThreshold.getValue() + (offset[0] / 2)); // Update move slider
        });

        maxThreshold.valueProperty().addListener((_, _, newValue) -> {
            offset[0] = newValue.doubleValue() - minThreshold.getValue();
            moveThreshold.setValue(minThreshold.getValue() + (offset[0] / 2)); // Update move slider
        });

        // Move slider shifts both min and max while keeping the gap
        moveThreshold.valueProperty().addListener((_, _, newValue) -> {
            double moveValue = newValue.doubleValue();
            double newMin = moveValue - (offset[0] / 2);
            double newMax = moveValue + (offset[0] / 2);

            if (newMin < 0) {
                newMin = 0;
                newMax = offset[0];
            }
            if (newMax > 255) {
                newMax = 255;
                newMin = 255 - offset[0];
            }

            minThreshold.setValue(newMin);
            maxThreshold.setValue(newMax);
        });

        minThreshold.setOnMouseReleased(_ -> onAnythingChanged());
        maxThreshold.setOnMouseReleased(_ -> onAnythingChanged());
        moveThreshold.setOnMouseReleased(_ -> onAnythingChanged());


        lumaOption.setOnAction(_ -> onAnythingChanged());
        hueOption.setOnAction(_ -> onAnythingChanged());
        redOption.setOnAction(_ -> onAnythingChanged());
        greenOption.setOnAction(_ -> onAnythingChanged());
        blueOption.setOnAction(_ -> onAnythingChanged());
        resetButton.setOnAction(_ -> {
            resetValues();
            onAnythingChanged();
        });

        ttbOption.setOnAction(_ -> onAnythingChanged());
        bttOption.setOnAction(_ -> onAnythingChanged());
        ltrOption.setOnAction(_ -> onAnythingChanged());
        rtlOption.setOnAction(_ -> onAnythingChanged());

        reverseCheck.setOnAction(_ -> onAnythingChanged());
        spanOffsetCheck.setOnAction(_ -> onAnythingChanged());

        importButton.setOnAction(_ -> {
            resetValues();
            imageHolder.setImage(ImageHandler.loadImage(null));
            if (imageHolder.getImage() != null) {
                imageHolder.setModifiedImage(null);
                System.out.println("Image successfully loaded!");
                showPreview();
                System.gc();
                GUI.showDialog("Success","Image succesufully imported", Alert.AlertType.INFORMATION);
            } else {
                GUI.showDialog("An error accrued when importing image:","No image selected or selection cancelled", Alert.AlertType.ERROR);
            }
        });


        exportButton.setOnAction(_ -> {
            ImageHandler.exportImage(null, imageHolder.getModifiedImage());
        });


        previewModeToggleButton.setOnAction(_ -> {
            checkMaskPreview();
            onAnythingChanged();
        });

        exportGIFButton.setOnAction(_ -> {
            checkValues();
            if (spanOffsetEnabled) {
                GIFHandler.imageModifyGIF(imageHolder,gifFrameNumber,gifTimeNumber,minThres,maxThres,sortMode,maskReverseEnabled,sortDirection,spanOffsetEnabled,spanBarSize,exportProgressIndicator,indicatorLabel,this);
            } else {
                GUI.showDialog("Info","'Random Span Offsets' has to be turned on to use this feature", Alert.AlertType.INFORMATION);
            }
        });


        regenerateImage.setOnAction(_ -> onAnythingChanged());

        spanSize.setOnMouseReleased(_ -> onAnythingChanged());
        spanSize.valueProperty().addListener((_,_,_) ->  spanSizeTextField.setText(Integer.toString((int) spanSize.getValue())));


    }




    private void resetValues() {
        lumaOption.setSelected(true);
        ttbOption.setSelected(true);
        minThreshold.setValue(0);
        maxThreshold.setValue(0);
        spanSize.setValue(0);
        moveThreshold.setValue(0);
        reverseCheck.setSelected(false);
        spanOffsetCheck.setSelected(false);
        maskPreviewEnabled = false;
    }

    private void onAnythingChanged() {
        System.out.println("A thing happened!");
        if (imageHolder.getImage() != null){
        checkValues();
        imageModify();
        }
    }

    private void scaleImageView(ImageView previewObject,Image image) {
        double imageViewWidth = 380;  // Width of your ImageView
        double imageViewHeight = 300; // Height of your ImageView

        double imageWidth = image.getWidth(); // Original image width
        double imageHeight = image.getHeight(); // Original image height

        // Scale factor (allowing both downscaling and upscaling)
        double scaleFactor = Math.min(imageViewWidth / imageWidth, imageViewHeight / imageHeight);

        // Scale the image (either up or down)
        double newWidth = imageWidth * scaleFactor;
        double newHeight = imageHeight * scaleFactor;

        // Apply the scaled dimensions to the ImageView
        previewObject.setFitWidth(newWidth);
        previewObject.setFitHeight(newHeight);

        // Center the image horizontally & vertically
        double horizontalPadding = (imageViewWidth - newWidth) / 2;
        double verticalPadding = (imageViewHeight - newHeight) / 2;

        // Apply the translation for centering
        previewObject.setTranslateX(horizontalPadding);
        previewObject.setTranslateY(verticalPadding);
    }

    private void showPreview(){
        checkValues();
        if (maskPreviewEnabled) {
            imagePreview.setImage(ImageEditor.visualizeMask(imageHolder.getImage(),minThres,maxThres,sortMode, maskReverseEnabled));
            scaleImageView(imagePreview,imageHolder.getImage());
        } else {
            if (imageHolder.getModifiedImage() != null) {
                imagePreview.setImage(imageHolder.getModifiedImage());
                scaleImageView(imagePreview, imageHolder.getImage());
            } else {
                imagePreview.setImage(imageHolder.getImage());
                scaleImageView(imagePreview, imageHolder.getImage());
            }
        }
    }

    private void checkMaskPreview(){
        maskPreviewEnabled = !maskPreviewEnabled;
        if (maskPreviewEnabled){
            previewModeToggleButton.setText("Show Image");
        } else {
            previewModeToggleButton.setText("Show Mask");
        }
        System.out.println("Mask preview: " + maskPreviewEnabled);
    }

    private void checkValues(){
        RadioButton selectedModeButton = (RadioButton) sortmode.getSelectedToggle();
        switch (selectedModeButton.getId()){
            case "lumaOption": sortMode = "luminance"; break;
            case "hueOption": sortMode = "hue"; break;
            case "redOption": sortMode = "red"; break;
            case "greenOption": sortMode = "green"; break;
            case "blueOption": sortMode = "blue"; break;
        }

        RadioButton selectedDirectionButton = (RadioButton) sortdirection.getSelectedToggle();
        switch (selectedDirectionButton.getId()){
            case "ttbOption": sortDirection = "top_to_bottom"; break;
            case "bttOption": sortDirection = "bottom_to_top"; break;
            case "ltrOption": sortDirection = "left_to_right"; break;
            case "rtlOption": sortDirection = "right_to_left"; break;
        }

        maskReverseEnabled = reverseCheck.isSelected();
        spanOffsetEnabled = spanOffsetCheck.isSelected();
        minThres = (int)minThreshold.getValue();
        maxThres = (int)maxThreshold.getValue();
        spanBarSize = (int)spanSize.getValue();

        gifFrameNumber = frameNumberSpinner.getValue();
        gifTimeNumber = frameTimeSpinner.getValue();


        System.out.println("Selected Mode: " + sortMode);
        System.out.println("Selected Direction: " + sortDirection);
        System.out.println("Reverse: " + maskReverseEnabled);
        System.out.println("Minimum Threshold: " + minThres);
        System.out.println("Maximum Threshold: " + maxThres);
        System.out.println("Span Bar Size: " + spanBarSize);

    }

    public void setupNumericSpinner(Spinner<Integer> spinner, int defaultValue, int minValue, int maxValue) {
        // Ensure default value is within bounds
        if (defaultValue < minValue || defaultValue > maxValue) {
            defaultValue = minValue;
        }

        // Set up the Spinner with values from 1 to maxValue
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, defaultValue);
        spinner.setValueFactory(valueFactory);

        // Create a TextFormatter to restrict input dynamically
        Pattern validInputPattern = Pattern.compile("\\d*"); // Only digits allowed
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (validInputPattern.matcher(newText).matches()) {
                return change; // Accept change
            } else {
                return null; // Reject change
            }
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        spinner.getEditor().setTextFormatter(textFormatter);

        // Handle manual input validation
        spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int enteredValue = Integer.parseInt(newValue);

                    if (enteredValue > maxValue) {
                        GUI.showDialog("Warning: ","Value cannot be greater than " + maxValue, Alert.AlertType.WARNING);
                        spinner.getEditor().setText(String.valueOf(maxValue));
                        valueFactory.setValue(maxValue);
                    } else if (enteredValue < minValue) {
                        GUI.showDialog("Warning: ","Value cannot be less than " + minValue, Alert.AlertType.WARNING);                        spinner.getEditor().setText(String.valueOf(minValue));
                        valueFactory.setValue(minValue);
                    } else {
                        valueFactory.setValue(enteredValue);
                    }
                } catch (NumberFormatException e) {
                    GUI.showDialog("Warning: ","VInvalid input! Only numbers are allowed", Alert.AlertType.WARNING);
                    spinner.getEditor().setText(String.valueOf(minValue));
                    valueFactory.setValue(minValue);
                }
            }
        });
    }

    public void turnUI(Boolean state){
        minThreshold.setDisable(!state);
        maxThreshold.setDisable(!state);
        moveThreshold.setDisable(!state);
        spanSize.setDisable(!state);
        lumaOption.setDisable(!state);
        hueOption.setDisable(!state);
        redOption.setDisable(!state);
        greenOption.setDisable(!state);
        blueOption.setDisable(!state);
        ttbOption.setDisable(!state);
        bttOption.setDisable(!state);
        ltrOption.setDisable(!state);
        rtlOption.setDisable(!state);
        reverseCheck.setDisable(!state);
        spanOffsetCheck.setDisable(!state);
        importButton.setDisable(!state);
        exportButton.setDisable(!state);
        previewModeToggleButton.setDisable(!state);
        resetButton.setDisable(!state);
        regenerateImage.setDisable(!state);
        exportGIFButton.setDisable(!state);
        spanSizeTextField.setDisable(!state);
        frameNumberSpinner.setDisable(!state);
        frameTimeSpinner.setDisable(!state);
    }

    private void imageModify() {
        turnUI(false);
        exportProgressIndicator.setVisible(true);
        exportProgressIndicator.setProgress(-1);
        indicatorLabel.setText("Sorting Image...");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Perform the image modification in the background
                imageHolder.setModifiedImage(ImageEditor.pixelSort(
                        imageHolder.getImage(),
                        minThres,
                        maxThres,
                        sortMode,
                        maskReverseEnabled,
                        sortDirection,
                        spanOffsetEnabled,
                        spanBarSize
                ));

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    turnUI(true);
                    exportProgressIndicator.setVisible(false);
                    exportProgressIndicator.setProgress(0);
                    indicatorLabel.setText("");
                    showPreview();
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    turnUI(true);
                    exportProgressIndicator.setVisible(false);
                    exportProgressIndicator.setProgress(0);
                    indicatorLabel.setText("");

                });
            }
        };

        // Start the task in a new thread
        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true); // Set the thread to be a daemon thread
        taskThread.start();
    }

}
