package org.aktr0s.Luminova;

import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ImageEditor {

    public static double luminance(Color pixel) {
        return 0.2126 * pixel.getRed() + 0.7152 * pixel.getGreen() + 0.0722 * pixel.getBlue();
    }

    public static double hue(Color pixel) {
        return pixel.getHue();
    }

    public static boolean[][] createMask(Image img, int lowThreshold, int highThreshold, String mode, Boolean reverse) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        boolean[][] mask = new boolean[height][width];
        PixelReader reader = img.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                double value = switch (mode) {
                    case "luminance" -> luminance(pixel) * 255;
                    case "red" -> pixel.getRed() * 255;
                    case "green" -> pixel.getGreen() * 255;
                    case "blue" -> pixel.getBlue() * 255;
                    case "hue" -> hue(pixel);
                    default -> throw new IllegalArgumentException("Invalid mode. Choose from 'luminance', 'red', 'green', 'blue', or 'hue'.");
                };

                boolean inRange = (value >= lowThreshold && value <= highThreshold);
                mask[y][x] = reverse != inRange;
            }
        }
        return mask;
    }

    public static Image visualizeMask(Image img, int lowThreshold, int highThreshold, String mode, boolean reverse) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        boolean[][] mask = createMask(img, lowThreshold, highThreshold, mode, reverse);
        WritableImage maskImage = new WritableImage(width, height);
        PixelWriter writer = maskImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean maskValue = mask[y][x];
                writer.setColor(x, y, maskValue ? Color.WHITE : Color.BLACK);
            }
        }
        return maskImage;
    }


    public static Image pixelSort(Image img, int lowThreshold, int highThreshold, String mode, boolean reverse, String direction, boolean randomSpanEnabled, int lineThickness) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        boolean[][] mask = createMask(img, lowThreshold, highThreshold, mode, reverse);
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = img.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        Random random = new Random();

        // Copy original pixels to avoid color loss
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, reader.getColor(x, y));
            }
        }

        int minSpan = Math.max(width / 40, 20);
        int maxSpan = Math.max(width / 15, 50);
        int minSpanY = Math.max(height / 40, 20);
        int maxSpanY = Math.max(height / 15, 50);

        if (direction.equals("left_to_right") || direction.equals("right_to_left")) {
            for (int y = 0; y < height; y += lineThickness) {  // Process multiple rows together
                int x = 0;
                while (x < width) {
                    int span = randomSpanEnabled ? (minSpan + random.nextInt(maxSpan - minSpan)) : width - x;

                    List<Color> maskedPixels = new ArrayList<>();
                    List<Point> positions = new ArrayList<>();  // Store (x, y) positions

                    // Collect pixels for all rows in the thickness range
                    for (int ty = y; ty < Math.min(y + lineThickness, height); ty++) {
                        for (int i = x; i < Math.min(x + span, width); i++) {
                            if (mask[ty][i]) {
                                maskedPixels.add(reader.getColor(i, ty));
                                positions.add(new Point(i, ty));
                            }
                        }
                    }

                    // Sort collected pixels
                    if (!maskedPixels.isEmpty()) {
                        maskedPixels.sort(Comparator.comparingDouble(ImageEditor::luminance));
                        if (direction.equals("right_to_left")) {
                            Collections.reverse(maskedPixels);
                        }

                        // Write sorted pixels back
                        for (int i = 0; i < maskedPixels.size(); i++) {
                            writer.setColor(positions.get(i).x, positions.get(i).y, maskedPixels.get(i));
                        }
                    }
                    x += span;
                }
            }
        } else if (direction.equals("top_to_bottom") || direction.equals("bottom_to_top")) {
            for (int x = 0; x < width; x += lineThickness) {  // Process multiple columns together
                int y = 0;
                while (y < height) {
                    int span = randomSpanEnabled ? (minSpanY + random.nextInt(maxSpanY - minSpanY)) : height - y;

                    List<Color> maskedPixels = new ArrayList<>();
                    List<Point> positions = new ArrayList<>();  // Store (x, y) positions

                    // Collect pixels for all columns in the thickness range
                    for (int tx = x; tx < Math.min(x + lineThickness, width); tx++) {
                        for (int i = y; i < Math.min(y + span, height); i++) {
                            if (mask[i][tx]) {
                                maskedPixels.add(reader.getColor(tx, i));
                                positions.add(new Point(tx, i));
                            }
                        }
                    }

                    // Sort collected pixels
                    if (!maskedPixels.isEmpty()) {
                        maskedPixels.sort(Comparator.comparingDouble(ImageEditor::luminance));
                        if (direction.equals("bottom_to_top")) {
                            Collections.reverse(maskedPixels);
                        }

                        // Write sorted pixels back
                        for (int i = 0; i < maskedPixels.size(); i++) {
                            writer.setColor(positions.get(i).x, positions.get(i).y, maskedPixels.get(i));
                        }
                    }
                    y += span;
                }
            }
        }

        System.gc();
        return outputImage;
    }
}
