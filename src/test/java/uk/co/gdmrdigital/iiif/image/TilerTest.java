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

        InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/");
        Map tInfoJson = tInfo.toJson(tVersion);
        assertTrue("Expected @id in info.json", tInfoJson.containsKey("@id"));
        assertEquals("Unexpected ID", "http://localhost:8887/iiif/67352ccc-d1b0-11e1-89ae-279075081939", (String)tInfoJson.get("@id"));
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

        InfoJson tInfo = new InfoJson(tImageInfo, "http://localhost:8887/iiif/");
        Map tInfoJson = tInfo.toJson(tVersion);
        assertTrue("Expected @id in info.json", tInfoJson.containsKey("id"));
        assertEquals("Unexpected ID", "http://localhost:8887/iiif/67352ccc-d1b0-11e1-89ae-279075081939", (String)tInfoJson.get("id"));
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

        assertEquals("Predicted number of files is different to the actual.", tPredictedCount, tActualCount);
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
