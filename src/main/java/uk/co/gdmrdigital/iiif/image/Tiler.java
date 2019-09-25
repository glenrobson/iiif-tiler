package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FilenameFilter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.github.jsonldjava.utils.JsonUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 */
public class Tiler {
    private static final Logger _logger = LogManager.getLogger();
    protected ImageInfo _image = null;
    protected String _version = "";

    public Tiler(final ImageInfo pImage, final String pVersion) {
        _image = pImage;
        _version = pVersion;
    }

    public File getOutputDir(final File pImageDir) {
        return new File(pImageDir, _image.getId());
    }

    public void generateTiles(final File pImageDir) throws IOException {
        File tImgDir = this.getOutputDir(pImageDir);
        _logger.debug("Using image info " + _image);
        //System.out.println(pImageDir);
        //System.out.println(tImgDir);
        this.generateSizes(tImgDir);
        this.generateScaleTiles(tImgDir);
    }

    protected void generateSizes(final File pImageDir) throws IOException {
        //System.out.println(pImageDir);
        // Generate sizes
        for (Point tSize : _image.getSizes()) {
            BufferedImage tScaledImage = new BufferedImage(tSize.x, tSize.y, BufferedImage.TYPE_INT_RGB);
			Graphics2D tGraphics = tScaledImage.createGraphics();
			tGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			tGraphics.drawImage(_image.getImage(), 0, 0, tSize.x, tSize.y, null);
            
            String tSizeStr = tSize.x + ",";
            if (tSize.x == _image.getWidth() && tSize.y == _image.getHeight()) {
                if (_version == InfoJson.VERSION3) { 
                    tSizeStr = "max";
                } else {
                    tSizeStr = "full";
                }
            }
            File tOuputFile = new File(pImageDir, "./full/" + tSizeStr + "/0/default.jpg");
            tOuputFile.mkdirs();
            ImageIO.write(tScaledImage, "jpg", tOuputFile);
        }
    }

    protected void generateScaleTiles(final File pImageDir) throws IOException {
        for (int scale : _image.getScaleFactors()) {
            int tScaleLevelWidth = (int)(_image.getWidth() / scale);
            int tScaleLevelHeight = (int)(_image.getHeight() / scale);
            int tTileNumWidth = (int)(tScaleLevelWidth / _image.getTileWidth());
            int tTileNumHeight = (int)(tScaleLevelHeight / _image.getTileHeight());

            for (int x = 0; x < tTileNumWidth + 1; x++) {
                for (int y = 0; y < tTileNumHeight+ 1; y++) {
                    int tileX = x * _image.getTileWidth() * scale;
                    int tileY = y * _image.getTileHeight() * scale;
                    int scaledTileWidth = _image.getTileWidth() * scale;
                    int tiledWidthCalc = _image.getTileWidth();
                    if (tileX + scaledTileWidth > _image.getWidth()) {
                        scaledTileWidth = _image.getWidth() - tileX;
                        tiledWidthCalc = scaledTileWidth / scale;
                    }
                    int scaledTileHeight = _image.getTileHeight() * scale;
                    if (tileY + scaledTileHeight > _image.getHeight()) {
                        scaledTileHeight = _image.getHeight() - tileY;
                    }

                    String url = "./" + tileX + "," + tileY + "," + scaledTileWidth + "," + scaledTileHeight + "/" + tiledWidthCalc + ",/0/default.jpg";
                    //System.out.println("Zoom level: " + scale);
                    //System.out.println(url);
                    File tOuputFile = new File(pImageDir, url);
                    tOuputFile.mkdirs();

                    BufferedImage tTileImg = _image.getImage().getSubimage(tileX, tileY, scaledTileWidth, scaledTileHeight);
                    BufferedImage tScaledImage = new BufferedImage(_image.getTileWidth(), _image.getTileHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D tGraphics = tScaledImage.createGraphics();
                    tGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tGraphics.drawImage(tTileImg, 0, 0, _image.getTileWidth(), _image.getTileHeight(), null);
                    ImageIO.write(tScaledImage, "jpg", tOuputFile);
                }
            }
        }
    }

    public static void createImages(final List<File> pFiles, final File pOutputDir, final int pZoomLevel, final int pMaxFileNo, final String pVersion) throws IOException {
        for (File tInputFile : pFiles) {
            IIIFImage tImage = new IIIFImage(tInputFile);

            ImageInfo tImageInfo = new ImageInfo(tImage);
            tImageInfo.setZoomLevel(pZoomLevel);
            if (pMaxFileNo != -1) {
                tImageInfo.fitToMaxFileNo(pMaxFileNo);
            }    

            Tiler tTiler = new Tiler(tImageInfo, pVersion);
            tTiler.generateTiles(pOutputDir);

            InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/");
            Map tInfoJson = tInfo.toJson(pVersion);
            
            JsonUtils.writePrettyPrint(new FileWriter(new File(tTiler.getOutputDir(pOutputDir),"info.json")), tInfoJson);
            System.out.println("Converted " + tInputFile.getPath() + " to " + tTiler.getOutputDir(pOutputDir).getPath());
        }
    }

    public static void main(final String[] pArgs) throws IOException {
        if (pArgs.length > 2) {
            System.out.println("Usage:\n\tjava uk.org.gdmrdigital.iiif.image.Tiler [image] [zoom levels]");
            System.out.println("Images can be in the following format:");
            String[] imageFormats = ImageIO.getReaderFormatNames();
            for (int i = 0; i < imageFormats.length; i++) {
                System.out.println(" * " + imageFormats[i]);
            }
            System.exit(-1);
        }
        List<File> tInputFiles = new ArrayList<File>();
        if (pArgs.length > 0) {
            tInputFiles.add(new File(pArgs[0]));
        } else {
            // Look for supported files in the current directory
            File tCurrentDir = new File(System.getProperty("user.dir"));
            File[] tFiles = tCurrentDir.listFiles(new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    String[] imageFormats = ImageIO.getReaderFormatNames();
                    for (int i = 0; i < imageFormats.length; i++) {
                        if (name.endsWith(imageFormats[i])) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            for (int i = 0; i < tFiles.length; i++) {
                tInputFiles.add(tFiles[i]);
            }
        }
        int tZoom = 4;
        int tMaxFileNo = -1;
        if (pArgs.length == 2) {
            tZoom = Integer.parseInt(pArgs[1]);
        } else {
            tMaxFileNo = 100;
        }

        File tOutputDir = new File("iiif");

        String tVersion = InfoJson.VERSION211;
        //String tVersion = InfoJson.VERSION3;
        createImages(tInputFiles, tOutputDir, tZoom, tMaxFileNo, tVersion);
    }
}
