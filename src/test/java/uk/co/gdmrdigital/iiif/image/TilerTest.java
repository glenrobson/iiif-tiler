package uk.co.gdmrdigital.iiif.image;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import uk.co.gdmrdigital.iiif.image.Tiler;
import uk.co.gdmrdigital.iiif.image.IIIFImage;
import uk.co.gdmrdigital.iiif.image.ImageInfo;
import uk.co.gdmrdigital.iiif.image.InfoJson;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Unit test IIIF Tiler.
 */
public class TilerTest {
    @Rule
    public TemporaryFolder _tmp = new TemporaryFolder();
    private static final Logger _logger = LogManager.getLogger();
    /**
     * 
     */
    @Test
    public void testVersion2() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/67352ccc-d1b0-11e1-89ae-279075081939.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage);
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/", tVersion);
        Map tInfoJson = tInfo.toJson();
        assertTrue("Expected @id in info.json", tInfoJson.containsKey("@id"));
        assertEquals("Unexpected ID", "http://localhost:8887/iiif/67352ccc-d1b0-11e1-89ae-279075081939", (String)tInfoJson.get("@id"));

        // Test conical sizes
        for (Map<Integer, Integer> tSize : (List<Map<Integer,Integer>>)tInfoJson.get("sizes")) {
            File tSizeImage = new File(tOutputDir, "67352ccc-d1b0-11e1-89ae-279075081939/full/" + tSize.get("width") + ",/0/default.jpg");
            assertTrue("Size mentioned in the info.json is missing: " + tSizeImage.getPath(), tSizeImage.exists());
        }
    }

    @Test
    public void testVersion3() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/67352ccc-d1b0-11e1-89ae-279075081939.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage);
        String tVersion = InfoJson.VERSION3;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/", tVersion);
        Map tInfoJson = tInfo.toJson();
        assertTrue("Expected @id in info.json", tInfoJson.containsKey("id"));
        assertEquals("Unexpected ID", "http://localhost:8887/iiif/67352ccc-d1b0-11e1-89ae-279075081939", (String)tInfoJson.get("id"));

        // Test conical sizes
        // https://iiif.io/api/image/3.0/change-log/#113-change-canonical-form-of-size-parameter-to-wh
        for (Map<Integer, Integer> tSize : (List<Map<Integer,Integer>>)tInfoJson.get("sizes")) {
            File tSizeImage = new File(tOutputDir, "67352ccc-d1b0-11e1-89ae-279075081939/full/" + tSize.get("width") + "," + tSize.get("height") + "/0/default.jpg");
            assertTrue("Size mentioned in the info.json is missing: " + tSizeImage.getPath(), tSizeImage.exists());
        }
    }

    @Test
    public void testExactTileMatch() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/exact_tiles.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage);
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/", tVersion);

        File imgDir = new File(tOutputDir, "exact_tiles"); 

        List<String> expectedFiles = Arrays.asList("0,0,1024,1024", "0,0,2048,2048", "0,1024,1024,1024", "1024,0,1024,1024", "1024,1024,1024,1024", "full");

        Collections.sort(expectedFiles);
        List<String> tFiles = Arrays.asList(imgDir.list());
        Collections.sort(tFiles);
        assertEquals("Unexpected files from exact tile match image", expectedFiles, tFiles); 
    }

    @Test
    public void testCheckCount() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/67352ccc-d1b0-11e1-89ae-279075081939.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 256, 256, 4);

        int tPredictedCount = tImageInfo.calculateFileCount();
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        List<String> tFiles = this.countFiles(tOutputDir);
        //printLevels(tFiles); 
        int tActualCount = tFiles.size() + 1; // add info.json

        assertEquals("Predicted number of files is different to the actual.", tPredictedCount, tActualCount);
    }

    @Test
    public void testCheckCountLarge() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/van.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 512, 512, 4);

        int tPredictedCount = tImageInfo.calculateFileCount();
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);
        List<String> tFiles = this.countFiles(tOutputDir);
       // printLevels(tFiles); 
        int tActualCount = tFiles.size() + 1; // add info.json

        assertEquals("Predicted number of files is different to the actual.", tPredictedCount, tActualCount);
    }
    @Test
    public void testSmallerZoomLevel() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/67352ccc-d1b0-11e1-89ae-279075081939.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 256, 256, 3);

        int tPredictedCount = tImageInfo.calculateFileCount();
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);
        List<String> tFiles = this.countFiles(tOutputDir);
       // printLevels(tFiles); 
        int tActualCount = tFiles.size() + 1; // add info.json
        _logger.debug("Predicted " + tPredictedCount + " actual " + tActualCount);
        assertEquals("Predicted number of files is different to the actual.", tPredictedCount, tActualCount);
    }    

    @Test
    public void testLimitTo100() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/67352ccc-d1b0-11e1-89ae-279075081939.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 256, 256, 4);
        tImageInfo.fitToMaxFileNo(100);
        _logger.debug("Managed to fit 100 files by choosing: " + tImageInfo);

        int tPredictedCount = tImageInfo.calculateFileCount();
        _logger.debug("Predicted file count: " + tPredictedCount);
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        List<String> tFiles = this.countFiles(tOutputDir);
        /* for (String tFile : tFiles){ 
            System.out.println(tFile);
        }*/
        int tActualCount = tFiles.size() + 1; // add info.json
        _logger.debug("File count " + tActualCount);

        assertEquals("Predicted number of files is different to the actual.", tPredictedCount + 1, tActualCount);
        assertTrue("Requested less than 100 files but got more", tActualCount < 100);
    }

    @Test
    public void testLimitTo100Large() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/van.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 256, 256, 4);
        tImageInfo.fitToMaxFileNo(100);
        _logger.debug("Managed to fit 100 files by choosing: " + tImageInfo);

        int tPredictedCount = tImageInfo.calculateFileCount();
        _logger.debug("Predicted file count: " + tPredictedCount);
        String tVersion = InfoJson.VERSION211;

        Tiler tTiler = new Tiler(tImageInfo, tVersion);
        tTiler.generateTiles(tOutputDir);

        List<String> tFiles = this.countFiles(tOutputDir);
        /* for (String tFile : tFiles){ 
            System.out.println(tFile);
        }*/
        int tActualCount = tFiles.size() + 1; // add info.json
        _logger.debug("File count " + tActualCount);

        assertEquals("Predicted number of files is different to the actual.", tPredictedCount, tActualCount);
        assertTrue("Requested less than 100 files but got more", tActualCount < 100);
    }

    @Test
    public void testZoomLimit() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/van.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 256, 256, 5);

        List<Point> estimatedSizes = new ArrayList<Point>();
        for (int i = 5; i >= 0; i--) {
            int ratio = (int)Math.pow(2, i);
            estimatedSizes.add(new Point((int)Math.ceil((double)tImage.getWidth() / ratio), (int)Math.ceil((double)tImage.getHeight() / ratio)));
        }

        for (int i = 0 ; i < tImageInfo.getSizes().size(); i++) {
            Point tPublishedSize = tImageInfo.getSizes().get(i);

            assertEquals("Size " + i + " wasn't expected",  estimatedSizes.get(i), tPublishedSize);
        }

        Tiler tTiler = new Tiler(new ImageInfo(tImage, 1024, 1024, 5), InfoJson.VERSION211);
        tTiler.generateTiles(tOutputDir);
        BufferedImage tEdgeTile = ImageIO.read(new File(tOutputDir, "van/3072,2048,960,976/960,/0/default.jpg"));

        assertEquals("Expected edge tile to be 996 pixels wide.", 960, tEdgeTile.getWidth());
    }

    @Test
    public void testRounding() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/tractor.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 1024, 1024, 5);

        Tiler tTiler = new Tiler(tImageInfo, InfoJson.VERSION211);
        tTiler.generateTiles(tOutputDir);

        assertTrue("Rounded down instead of UP. Found tractor/full/503,/0/default.jpg expected tractor/full/504,/0/default.jpg", !new File(tOutputDir, "tractor/full/503,/0/default.jpg").exists());
        assertTrue("Correct rounding should exist. didn't find tractor: /full/504,/0/default.jpg", new File(tOutputDir, "tractor/full/504,/0/default.jpg").exists());
        //assertTrue("Correct rounding should exist. Didn't find tractor: /2048,0,1983,2048/992,/0/default.jpg", new File(tOutputDir, "tractor/2048,0,1983,2048/992,/0/default.jpg").exists());
    }   

    /**
     * Test issue where the rounded scale exactly goes into the tile width. This can only happen 
     * if the width of the source image is 2*tileWidth + 1
     * @throws IOException
     */
    @Test
    public void testOddSizedImage() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/odd-sized.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 512, 512, 1);

        Tiler tTiler = new Tiler(tImageInfo, InfoJson.VERSION211);
        tTiler.generateTiles(tOutputDir);

        assertTrue("Missed bottom 1 pixel tile. Did not find odd-sized/0,3072,1024,1/512,/0/default.jpg", new File(tOutputDir, "odd-sized/0,3072,1024,1/512,/0/default.jpg").exists());
    }

    @Test
    public void testSmallTiles() throws IOException {
        File tOutputDir = _tmp.newFolder("iiif");
        File tImageFile = new File("images/brazil.jpg");

        IIIFImage tImage = new IIIFImage(tImageFile);

        ImageInfo tImageInfo = new ImageInfo(tImage, 1024, 1024, 5);

        Tiler tTiler = new Tiler(tImageInfo, InfoJson.VERSION211);
        tTiler.generateTiles(tOutputDir);

        String[] tSmallTiles = {
            "0,6144,2048,3/1024,/0/default.jpg",
            "2048,6144,2048,3/1024,/0/default.jpg",
            "4096,6144,2048,3/1024,/0/default.jpg",
            "6144,6144,2048,3/1024,/0/default.jpg",
            "8192,6144,1288,3/644,/0/default.jpg"
        };
        for (int i = 0; i < tSmallTiles.length; i++) {
            assertTrue("Expected tile: " + tOutputDir.getPath() + "/brazil/" + tSmallTiles[i] + " to exist", new File(tOutputDir, "brazil/" + tSmallTiles[i]).exists());
        }
    }

    protected void printLevels(final List<String> pFiles) {
        Map<Integer, List<String>> tLevels = new java.util.HashMap<Integer, List<String>>();
        List<String> tFull = new ArrayList<String>();
        for (String tFile : pFiles) {
            if (tFile.contains("default.jpg") && !tFile.contains("full")) {
               // System.out.println(tFile);
                String[] tSplitFile = tFile.split("/");
                String tRange = tSplitFile[tSplitFile.length - 4];
                String tSize = tSplitFile[tSplitFile.length - 3];
                //System.out.println("Range: " + tRange + " size " + tSize);
                int tRangeWidth = Integer.parseInt(tRange.split(",")[2]);
                int tSizeWidth = Integer.parseInt(tSize.split(",")[0]);
                //System.out.println("Range Width " + tRangeWidth + " sizeWidth " + tSizeWidth + " zoomlevel " + ((int)(tRangeWidth / tSizeWidth)));
                int tLevel = (int)(tRangeWidth / tSizeWidth);
                if (!tLevels.containsKey(tLevel)) {
                    tLevels.put(tLevel, new ArrayList<String>());
                }
                tLevels.get(tLevel).add(tFile);
            } else if (tFile.contains("full")) {
                tFull.add(tFile);
            } else {
                //System.out.println(tFile);
            }
        }
        int totalTiles = 0;
        for (int tKey : tLevels.keySet()) {
            System.out.println("Level " + tKey + " tiles " + tLevels.get(tKey).size());
            totalTiles += tLevels.get(tKey).size();
        }
        System.out.println("Total tiles " + (totalTiles * 4));
        System.out.println("Total full " + tFull.size());
    }


    protected List<String> countFiles(final File pDirectory) {
       // if (pDirectory.listFiles().length > 1) {
         //   System.out.println("Multiple " + pDirectory.getPath());
        //}
        List<String> tFiles = new ArrayList<String>();
        for (final File fileEntry : pDirectory.listFiles()) {
            tFiles.add(fileEntry.getPath());
            
            if (fileEntry.isDirectory()) {
                tFiles.addAll(countFiles(fileEntry));
            }
        }

        return tFiles;
    }

}
