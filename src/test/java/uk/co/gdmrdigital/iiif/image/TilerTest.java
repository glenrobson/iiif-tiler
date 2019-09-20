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

/**
 * Unit test IIIF Tiler.
 */
public class TilerTest {
    @Rule
    public TemporaryFolder _tmp = new TemporaryFolder();
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

}
