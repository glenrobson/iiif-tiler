package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;

import java.util.List;
import java.util.ArrayList;

import java.awt.Point;


public class ImageInfo {
    protected int _tileWidth = 1024;
    protected int _tileHeight = 1024;
    protected int _zoomLevels = 2;
    protected IIIFImage _image = null;
    protected List<Integer> _scaleFactors = new ArrayList<Integer>();
    protected List<Point> _sizes = new ArrayList<Point>();


    public ImageInfo(final IIIFImage pImage) {
        this(pImage, 1024, 1024, 2);
    }

    public ImageInfo(final IIIFImage pImage, final int pTileWidth, final int pTileHeight, final int pZoomLevel) {
        this.setImage(pImage);
        this.initializeImageInfo();
        this.setTileWidth(pTileWidth);
        this.setTileHeight(pTileHeight);
        this.setZoomLevel(pZoomLevel);
    }

    protected void initializeImageInfo() {
        _scaleFactors = new ArrayList<Integer>();
        _sizes = new ArrayList<Point>();
        int i = 1;
        for (i = 1; i < _zoomLevels + 1; i++) {
            _sizes.add(0, new Point((int)(_image.getWidth() / i), (int)(_image.getHeight() / i)));
            _scaleFactors.add((int)Math.pow(2, i - 1));
        }
        _sizes.add(0, new Point((int)(_image.getWidth() / i), (int)(_image.getHeight() / i)));
    }

    public String getId() {
        return _image.getId();
    }

    public List<Integer> getScaleFactors() {
        return _scaleFactors;
    }

    public List<Point> getSizes() {
        return _sizes;
    }

    public int getWidth() {
        return _image.getWidth();
    }
    public int getHeight() {
        return _image.getHeight();
    }

    /**
     * Get tileWidth.
     *
     * @return tileWidth as int.
     */
    public int getTileWidth() {
        return _tileWidth;
    }
    
    /**
     * Set tileWidth.
     *
     * @param tileWidth the value to set.
     */
    public void setTileWidth(final int pTileWidth) {
         _tileWidth = pTileWidth;
    }
    
    /**
     * Get tileHeight.
     *
     * @return tileHeight as int.
     */
    public int getTileHeight() {
        return _tileHeight;
    }
    
    /**
     * Set tileHeight.
     *
     * @param tileHeight the value to set.
     */
    public void setTileHeight(final int pTileHeight) {
         _tileHeight = pTileHeight;
    }
    
    /**
     * Get zoomLevel.
     *
     * @return zoomLevel as int.
     */
    public int getZoomLevel() {
        return _zoomLevels;
    }
    
    /**
     * Set zoomLevel.
     *
     * @param zoomLevel the value to set.
     */
    public void setZoomLevel(final int pZoomLevel) {
         _zoomLevels = pZoomLevel;
    }
    
    /**
     * Get image.
     *
     * @return image as IIIFImage.
     */
    public BufferedImage getImage() {
        return _image.getImage();
    }
    
    /**
     * Set image.
     *
     * @param image the value to set.
     */
    protected void setImage(final IIIFImage pImage) {
        _image = pImage;
        this.initializeImageInfo();
    }
}
