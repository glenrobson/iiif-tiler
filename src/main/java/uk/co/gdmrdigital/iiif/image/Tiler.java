package uk.co.gdmrdigital.iiif.image;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.commons.cli.Options;

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

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

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
            if (tScaleLevelWidth % _image.getTileWidth() != 0) {
                // width doesn't divide exactly into tiles so add 1 to get the last tile
                tTileNumWidth++;
            }

            if (tScaleLevelHeight % _image.getTileHeight() != 0) {
                tTileNumHeight++;
            }

            for (int x = 0; x < tTileNumWidth; x++) {
                for (int y = 0; y < tTileNumHeight; y++) {
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
                    tOuputFile.getParentFile().mkdirs();

                    BufferedImage tTileImg = _image.getImage().getSubimage(tileX, tileY, scaledTileWidth, scaledTileHeight);
                    BufferedImage tScaledImage = null;
                    if (tTileImg.getWidth(null) == tiledWidthCalc && tTileImg.getHeight(null) == tiledHeightCalc) {
                        tScaledImage = tTileImg;
                    } else if (tiledWidthCalc > 3 && tiledHeightCalc > 3) {
                        try {
                            ResampleOp resizeOp = new ResampleOp(tiledWidthCalc, tiledHeightCalc);
                            tScaledImage = resizeOp.filter(tTileImg, null);
                        } catch (RuntimeException tExcpt) {
                            System.out.println("Tile: " + tOuputFile + " (width: " + scaledTileWidth + ", height: " + scaledTileHeight + ")");
                            System.out.println("Tile Image: width: " + tTileImg.getWidth(null) + " height " + tTileImg.getHeight(null));
                            System.out.println("Calculated width: " + tiledWidthCalc + " height: " + tiledHeightCalc);
                            throw tExcpt;
                        }
                    } else {
                        tScaledImage = new BufferedImage(tiledWidthCalc, tiledHeightCalc, tTileImg.getType());

                        Image tSmallImage = tTileImg.getScaledInstance(tiledWidthCalc, tiledHeightCalc, Image.SCALE_SMOOTH);

                        Graphics2D graphics2D = tScaledImage.createGraphics();
                        graphics2D.drawImage(tSmallImage, 0, 0, null);
                        graphics2D.dispose();
                    }

                    boolean tSuccess = ImageIO.write(tScaledImage, "jpg", tOuputFile);
                    if (!tSuccess) {
                        System.out.println("Failed to write " + tOuputFile);
                    }
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
     * @param pTileSize the width and heigh of the tile. Note tiles can only be square in this implmentation. Use -1 for default of 1024.
     * @param pVersion either InfoJson.VERSION211 or InfoJson.VERSION3 
     * @throws IOException if there is an issue loading the source image or writing the IIIF image
     */
    public static void createImages(final List<File> pFiles, final File pOutputDir, final String pIdentifier, final int pZoomLevel, final int pMaxFileNo, final int pTileSize, final String pVersion) throws IOException {
        for (File tInputFile : pFiles) {
            IIIFImage tImage = new IIIFImage(tInputFile);

            ImageInfo tImageInfo = new ImageInfo(tImage);
            tImageInfo.setZoomLevel(pZoomLevel);
            if (pMaxFileNo != -1) {
                tImageInfo.fitToMaxFileNo(pMaxFileNo);
            } else {
                tImageInfo.fitToZoomLevel();
            }

            if (pTileSize != -1) {
                tImageInfo.setTileWidth(pTileSize);
                tImageInfo.setTileHeight(pTileSize);
            }

            File tImageOutput = createImage(tImageInfo, pOutputDir, pIdentifier, pVersion);
            System.out.println("Converted " + tInputFile.getPath() + " to " + tImageOutput.getPath());
        }
    }

    public static void main(final String[] pArgs) throws IOException {
        int tZoom = 5;
        String tVersion = InfoJson.VERSION211;
        int tTilesize = 1024;
        String outputDir = "iiif";
        String identifier_root = "http://localhost:8887/iiif/";

        Options tOptions = new Options();
        tOptions.addOption("identifier", true, "Set the identifier in the info.json. The default is " + identifier_root);
        tOptions.addOption("zoom_levels", true, "set the number of zoom levels for this image. The default is " + tZoom);
        tOptions.addOption("version", true, "set the IIIF version. Default is " + tVersion + " and options are 2 or 3");
        tOptions.addOption("tile_size", true, "set the tile size. Default is " + tTilesize);
        tOptions.addOption("output", true, "Directory where the IIIF images are generated. Default: " + outputDir);
        tOptions.addOption("help", false, "Show this help message");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine tCmd = null;
        try {
            // parse the command line arguments
            tCmd = parser.parse(tOptions, pArgs);
        } catch(ParseException exp ) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage() );
            System.out.println(exp.getMessage());
            formatter.printHelp("iiif-tiler", tOptions);
        }

        if (tCmd.hasOption("help")) {
            formatter.printHelp("iiif-tiler", tOptions);
            System.exit(1);
        }

        List<File> tInputFiles = new ArrayList<File>();
        if (!tCmd.getArgList().isEmpty()) {
            for (String tFile : tCmd.getArgList()) {
                tInputFiles.add(new File(tFile));
            }
        } else {
            System.out.println("Looking for images in current directory");
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
            if (tInputFiles.size() == 0) {
                System.err.println("Failed to find any images to process");
                System.err.println("Exiting....");
                System.exit(-1);
            }
            System.out.println("Found " + tInputFiles.size() + " image files.");
        }
        if (tCmd.hasOption("identifier")) {
            identifier_root = tCmd.getOptionValue("identifier");
        }
        int tMaxFileNo = -1;
        if (tCmd.hasOption("zoom_levels")) {
            tZoom = Integer.parseInt(tCmd.getOptionValue("zoom_levels"));
        }

        if (tCmd.hasOption("version")) {
            if (tCmd.getOptionValue("version").contains("2")) {
                tVersion = InfoJson.VERSION211;
            } else if  (tCmd.getOptionValue("version").contains("3")) {
                tVersion = InfoJson.VERSION3;
            } else {
                System.err.println("Unrecognised version '" + tCmd.getOptionValue("version") + "' value can either be 2 or 3.");

                formatter.printHelp("iiif-tiler", tOptions);
            }
        }

        if (tCmd.hasOption("tile_size")) {
            tTilesize = Integer.parseInt(tCmd.getOptionValue("tile_size"));
        }

        System.out.println("Zoom level " + tZoom);
        File tOutputDir = new File(outputDir);
        if (tCmd.hasOption("output")) {
            tOutputDir = new File(tCmd.getOptionValue("output"));
        }

        //String tVersion = InfoJson.VERSION3;
        createImages(tInputFiles, tOutputDir, identifier_root, tZoom, tMaxFileNo, tTilesize, tVersion);
    }
}
