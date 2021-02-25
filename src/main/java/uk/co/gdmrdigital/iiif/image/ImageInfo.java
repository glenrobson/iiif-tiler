package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;

import java.util.List;
import java.util.ArrayList;

import java.awt.Point;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ImageInfo {
    private static final Logger _logger = LogManager.getLogger();

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
        this.setTileWidth(pTileWidth);
        this.setTileHeight(pTileHeight);
        this.setZoomLevel(pZoomLevel);
        this.initializeImageInfo();
    }

    public void fitToZoomLevel() {
       /* int[] tTileSize = {1024,512,256};
        for (int i = 0; i < tTileSize.length; i++) {
            System.out.println("Trying out tile size " + tTileSize[i] + " zoom level " + _zoomLevels + " Power " + Math.pow(2, _zoomLevels) + " width " + ((_image.getWidth() / Math.pow(2, _zoomLevels))) + " height " +  (_image.getHeight() / Math.pow(2, _zoomLevels)));
            if ((_image.getWidth() / Math.pow(2, _zoomLevels)) > tTileSize[i] && (_image.getHeight() / Math.pow(2, _zoomLevels)) > tTileSize[i]) {
                this.setTileWidth(tTileSize[i]);
                this.setTileHeight(tTileSize[i]);
                this.initializeImageInfo();
                return;
            }
        }
        System.out.println("Image is too small to produce " + _zoomLevels + " zoom levels.");
        this.setTileWidth(tTileSize[2]);
        this.setTileHeight(tTileSize[2]);*/
        this.initializeImageInfo();
    }

    public void fitToMaxFileNo(final int pMaxFileNo) {
        int tMaxZoom = 4;
        int tMaxTileSizeFacter = 5;
        int tZoom = 0;
        int tTileSize = 0;
        boolean tFound  = false;
        int tFileCount = 0;
        outerloop:
        for (int j = 1; j <= tMaxTileSizeFacter; j++) {
            for (tZoom = tMaxZoom; tZoom > 0; tZoom--){
                tTileSize = j * 256;
                tFileCount = this.calculateFileCount(tZoom, tTileSize, tTileSize);
                if (tFileCount < pMaxFileNo) {
                    _logger.debug("Using TileSize: " + tTileSize + " Zoom: " + tZoom + " came back with " + tFileCount + " files. Target: " + pMaxFileNo );
                    tFound = true;
                    break outerloop;
                } else {
                    _logger.debug("Rejected TileSize: " + tTileSize + " Zoom: " + tZoom + " came back with " + tFileCount + " files. Target: " + pMaxFileNo );
                }
            }
        }
        if (tFound) {
            this.setTileWidth(tTileSize);
            this.setTileHeight(tTileSize);
            this.setZoomLevel(tZoom);
            this.initializeImageInfo();
            _logger.debug("Found Goldilocks combinations " + this.toString() + " with a file count of " + tFileCount);
        } else {    // Raies an exception
            throw new IllegalArgumentException("Failed to find combination under " + pMaxFileNo + " files");
        }
    }

    public int calculateFileCount() {
        return this.calculateFileCount(_zoomLevels, _tileWidth, _tileHeight);
    }

    protected int calculateFileCount(final int pZoom, final int pTileWidth, final int pTileHeight) {
        int tFileCount = 0;

       // System.out.println("zoom: " + pZoom + " tile width " + pTileWidth);
        boolean reachedMultipleFullsizedTile = false;
        for (int tZoom = 0; tZoom < pZoom ; tZoom++) {
            int tZoomFactor = (int)Math.pow(2, tZoom);
            int tWidth = _image.getWidth() / tZoomFactor;
            int tHeight = _image.getHeight() / tZoomFactor;
            // Reached smallest tile
            int tTileXCount = (int)Math.ceil((double)tWidth / pTileWidth);
            int tTileYCount = (int)Math.ceil((double)tHeight / pTileHeight);
            //System.out.println("Zoomfactor " + tZoomFactor + " tiles-x " + tTileXCount + " tiles-y " + tTileYCount + " width = " + tWidth + " tileCount = " + (tTileXCount * tTileYCount));
            // each tile creates 4 files. 3 directories and 1 image
            if (tWidth < pTileWidth && tHeight < pTileHeight) {
                tFileCount += tTileXCount * tTileYCount * 3;
                reachedMultipleFullsizedTile = true;
            } else {
                tFileCount += tTileXCount * tTileYCount * 4;
            }    
        }
        // If the tile is bigger than the image size we add 3 directories but 
        // for one we need to add 4.
        if (reachedMultipleFullsizedTile) {
            tFileCount++;
        }
      //  System.out.println("Total tiles " + tFileCount);
        // Add full sizes (1 full directory then three sub directores (size/rotation/file)
        // And full w,h
       // System.out.println("Sizes " + (((pZoom + 1) * 3) + 1) + " should be 16");
        tFileCount += ((pZoom + 2) * 3) + 1;

        // Add info.json
        tFileCount++;
        // Add containing directory
        tFileCount++;
        return tFileCount;
    }

    protected void initializeImageInfo() {
        _scaleFactors = new ArrayList<Integer>();
        _sizes = new ArrayList<Point>();
        for (int i = _zoomLevels; i >= 0; i--) {
            int scale = (int)Math.pow(2, i);
            _sizes.add(new Point((int)Math.ceil((double)_image.getWidth() / scale), (int)Math.ceil((double)_image.getHeight() / scale)));
            _scaleFactors.add(scale);
        }
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

    public String toString() {
        StringBuffer tBuff = new StringBuffer("Image info:\n");
        tBuff.append("\tTile size; width: ");
        tBuff.append("" + _tileWidth);
        tBuff.append(", height: ");
        tBuff.append(_tileHeight);
        tBuff.append("\n\tZoomlevels: ");
        tBuff.append("" + _zoomLevels);
        tBuff.append("\n\t * Sizes:");
        tBuff.append(_sizes.size());
        tBuff.append("\n\t * ScaleFactors:");
        tBuff.append(_scaleFactors);
        return tBuff.toString();
    }
}
