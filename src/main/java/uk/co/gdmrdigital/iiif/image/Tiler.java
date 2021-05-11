package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.mortennobel.imagescaling.ResampleOp;

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
 * Class that converts Images to IIIF tiles. It has static methods createImage and createImages to create the IIIF images
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
        this.generateTiles(pImageDir, _image.getId());
    }

    public void generateTiles(final File pImageDir, final String pFilename) throws IOException {
        File tImgDir = new File(pImageDir, pFilename);
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
            ResampleOp resizeOp = new ResampleOp(tSize.x, tSize.y);
            BufferedImage tScaledImage = resizeOp.filter(_image.getImage(), null);
            String tSizeStr = tSize.x + ",";
            if (_version == InfoJson.VERSION3) {
                // canonical form changes in version 3
                tSizeStr = tSize.x + "," + tSize.y;
            }
            File tOuputFile = new File(pImageDir, "./full/" + tSizeStr + "/0/default.jpg");
            tOuputFile.mkdirs();
            ImageIO.write(tScaledImage, "jpg", tOuputFile);
            if (tSize.x == _image.getWidth() && tSize.y == _image.getHeight()) {
                if (_version == InfoJson.VERSION3) { 
                    tSizeStr = "max";
                } else {
                    tSizeStr = "full";
                }
                tOuputFile = new File(pImageDir, "./full/" + tSizeStr + "/0/default.jpg");
                tOuputFile.mkdirs();
                ImageIO.write(tScaledImage, "jpg", tOuputFile);
            }
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
                        tiledWidthCalc = (int)Math.ceil((double)scaledTileWidth / scale);
                    }
                    int scaledTileHeight = _image.getTileHeight() * scale;
                    int tiledHeightCalc = _image.getTileHeight();
                    if (tileY + scaledTileHeight > _image.getHeight()) {
                        scaledTileHeight = _image.getHeight() - tileY;
                        tiledHeightCalc = (int)Math.ceil((double)scaledTileHeight / scale);
                    }

                    String url = "./" + tileX + "," + tileY + "," + scaledTileWidth + "," + scaledTileHeight + "/" + tiledWidthCalc + ",/0/default.jpg";
                    if (_version == InfoJson.VERSION3) { 
                        url = "./" + tileX + "," + tileY + "," + scaledTileWidth + "," + scaledTileHeight + "/" + tiledWidthCalc + "," + tiledHeightCalc + "/0/default.jpg";
                    } 
                    //System.out.println("Zoom level: " + scale);
                    //System.out.println(url);
                    File tOuputFile = new File(pImageDir, url);
                    tOuputFile.mkdirs();

                    BufferedImage tTileImg = _image.getImage().getSubimage(tileX, tileY, scaledTileWidth, scaledTileHeight);
                    ResampleOp resizeOp = new ResampleOp(tiledWidthCalc, tiledHeightCalc);
                    BufferedImage tScaledImage = resizeOp.filter(tTileImg, null);

                    ImageIO.write(tScaledImage, "jpg", tOuputFile);
                }
            }
        }
    }

    /** 
     * Pass a file to convert it to a IIIF static image.
     * @param pImageFile the image file to convert
     * @param pOutputDir the output directory for the IIIF images. Note a sub directory will be created for each image 
     * @param pURI the identifier to use in the @id of the info.json. Note this method will add the identifier for the IIIF image to the end of this URL. So if the image file is a file called picture.jpg the URI could be http://localhost:8887/iiif and the identifier in the info.json would be http://localhost:8887/iiif/picture
     * @param pVersion either InfoJson.VERSION211 or InfoJson.VERSION3 
     * @return the directory that contains the IIIF image tiles
     * @throws IOException if there is an issue loading the source image or writing the IIIF image
     */
    public static File createImage(final File pImageFile, final File pOutputDir,  final String pURI, final String pVersion) throws IOException {
        IIIFImage tImage = new IIIFImage(pImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage);

        return createImage(tImageInfo, pOutputDir, pURI, pVersion);
    }

    /** 
     * Pass a ImageInfo to convert it to a IIIF static image. This method allows you to customise the zoom level and image idenfifier of the resulting IIIF image. To change the IIIF image idenifier use pImageFile.setId()
     * @param pImageFile the image file to convert
     * @param pOutputDir the output directory for the IIIF images. Note a sub directory will be created for each image 
     * @param pURI the identifier to use in the @id of the info.json. Note this method will add the identifier for the IIIF image to the end of this URL. So if the image file is a file called picture.jpg the URI could be http://localhost:8887/iiif and the identifier in the info.json would be http://localhost:8887/iiif/picture
     * @param pVersion either InfoJson.VERSION211 or InfoJson.VERSION3 
     * @return the directory that contains the IIIF image tiles
     * @throws IOException if there is an issue loading the source image or writing the IIIF image
     */
    public static File createImage(final ImageInfo pImageFile, final File pOutputDir,  final String pURI, final String pVersion) throws IOException {
        Tiler tTiler = new Tiler(pImageFile, pVersion);
        tTiler.generateTiles(pOutputDir);

        InfoJson tInfo = new InfoJson(pImageFile, pURI, pVersion);
        Map tInfoJson = tInfo.toJson();

        JsonUtils.writePrettyPrint(new FileWriter(new File(tTiler.getOutputDir(pOutputDir),"info.json")), tInfoJson);

        return tTiler.getOutputDir(pOutputDir);
    }

    /** 
     * Pass a list of files to convert to IIIF static images
     * @param pFiles a list of files to convert
     * @param pOutputDir the output directory for the IIIF images. Note a sub directory will be created for each image 
     * @param pZoomLevel the maximum amount of zoom levels to include in the IIIF image. A good value is 5 which works with Leaflet
     * @param pMaxFileNo if you want the number of tiles and info.json to fit into a maximum supply this variable. 
     * The number of zoom levels and tile sizes will be adjusted to try and fit the number of files under this limit. Set it to -1 to priortise the zoom level. 
     * @param pVersion either InfoJson.VERSION211 or InfoJson.VERSION3 
     * @throws IOException if there is an issue loading the source image or writing the IIIF image
     */
    public static void createImages(final List<File> pFiles, final File pOutputDir, final int pZoomLevel, final int pMaxFileNo, final String pVersion) throws IOException {
        for (File tInputFile : pFiles) {
            IIIFImage tImage = new IIIFImage(tInputFile);

            ImageInfo tImageInfo = new ImageInfo(tImage);
            tImageInfo.setZoomLevel(pZoomLevel);
            if (pMaxFileNo != -1) {
                tImageInfo.fitToMaxFileNo(pMaxFileNo);
            } else {
                tImageInfo.fitToZoomLevel();
            }

            File tImageOutput = createImage(tImageInfo, pOutputDir, "http://localhost:8887/iiif/", pVersion);
            System.out.println("Converted " + tInputFile.getPath() + " to " + tImageOutput.getPath());
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
        int tZoom = 5;
        int tMaxFileNo = -1;
        if (pArgs.length == 2) {
            tZoom = Integer.parseInt(pArgs[1]);
        }

        File tOutputDir = new File("iiif");
        System.out.println("Zoom level " + tZoom);

        String tVersion = InfoJson.VERSION211;
        //String tVersion = InfoJson.VERSION3;
        createImages(tInputFiles, tOutputDir, tZoom, tMaxFileNo, tVersion);
    }
}
