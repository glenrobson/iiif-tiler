package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class stores the source image as a BufferedImage and also works out the IIIF image identifier from the filename
 */
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

        if (_image.getColorModel().hasAlpha()) {
            // If image has Alpha remove it as jpg doesn't support Alpha
            BufferedImage withoutAlpha = new BufferedImage((int) _image.getWidth(),
                                                            (int) _image.getHeight(), 
                                                                BufferedImage.TYPE_INT_RGB);
            Graphics g = withoutAlpha.getGraphics();
            g.drawImage(_image, 0, 0, null);
            g.dispose();
            _image = withoutAlpha;
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
