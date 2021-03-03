package uk.co.gdmrdigital.iiif.image;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.awt.Point;

/**
 * This class generates the IIIF info.json for an image
 */
public class InfoJson {
    public static String VERSION211 = "2.1.1";
    public static String VERSION3 = "3.0";

    protected ImageInfo _imageInfo = null;
    protected String _uri = "";

    public InfoJson(final ImageInfo pImageInfo, final String pURI) {
        _imageInfo = pImageInfo;
        _uri = pURI;
    }

    public Map toJson(final String pVersion) {
        Map tInfoJson = new HashMap();

        if (pVersion == InfoJson.VERSION3) { 
            tInfoJson.put("@context", "http://iiif.io/api/image/3/context.json");
            tInfoJson.put("id", _uri + _imageInfo.getId());
            tInfoJson.put("type", "ImageService3");
            tInfoJson.put("profile", "level0");
        } else {
            tInfoJson.put("@context", "http://iiif.io/api/image/2/context.json");
            tInfoJson.put("@id", _uri + _imageInfo.getId());
            tInfoJson.put("profile", "http://iiif.io/api/image/2/level0.json");
        }

        tInfoJson.put("protocol", "http://iiif.io/api/image");
        tInfoJson.put("width", _imageInfo.getWidth());
        tInfoJson.put("height", _imageInfo.getHeight());
        List tSizesJson = new ArrayList();
        for (Point tSize : _imageInfo.getSizes()) {
            Map tSizeMap = new HashMap();
            tSizeMap.put("width", tSize.x);
            tSizeMap.put("height", tSize.y);
            tSizesJson.add(tSizeMap);
        }
        tInfoJson.put("sizes", tSizesJson);

        List tTilesList = new ArrayList();
        Map tTilesMap = new HashMap();
        tTilesMap.put("width", _imageInfo.getTileWidth());
        tTilesMap.put("height", _imageInfo.getTileHeight());
        tTilesMap.put("scaleFactors", _imageInfo.getScaleFactors());

        tTilesList.add(tTilesMap);
        tInfoJson.put("tiles", tTilesList);

        return tInfoJson;
    }
}
