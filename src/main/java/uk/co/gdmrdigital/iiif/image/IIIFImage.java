package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class IIIFImage {
    protected BufferedImage _image = null;
    protected String _id = "";

    public IIIFImage(final File pImageFile) throws IOException {
        this.loadImage(pImageFile);
    }

    protected void loadImage(final File pImageFile) throws IOException {
        try {
            _image = ImageIO.read(pImageFile);
        } catch (IOException exception) {
            throw exception;
        }
        this.setId(pImageFile.getName().split("\\.")[0]);
    }

    public String getId() {
        return _id;
    }

    public void setId(final String pId) {
        _id = pId;
    }

    public int getWidth() {
        return _image.getWidth();
    }

    public int getHeight() {
        return _image.getHeight();
    }

    public BufferedImage getImage() {
        return _image;
    }
}
