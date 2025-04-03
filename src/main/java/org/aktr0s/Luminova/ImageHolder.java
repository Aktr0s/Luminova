package org.aktr0s.Luminova;

import javafx.scene.image.Image;

public class ImageHolder {
    private Image image;
    private Image modifiedImage;

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }


    public Image getModifiedImage() {
        return modifiedImage;
    }

    public void setModifiedImage(Image modifiedImage) {
        this.modifiedImage = modifiedImage;
    }
}
