# iiif-tiler
A Java based static IIIF tile generator. Static IIIF Tiles can be stored on a web service and provide a zoomable service with a compatible IIIF image viewers. This is known as a `level 0` image service. 

## Usage:
By default and without configuration the iiif-tiler will generate version 2.0 image tiles in the iiif directory. To run this program do the following:

 1. Download the `iiif-tiler.jar` from the releases page. 
 2. Save the `iiif-tiler.jar` to a directory where you would like to store the image tiles
 3. Drag an image on to the iiif-tiler.jar in the Finder or Windows interface.
 4. This will generate the IIIF tiles in a `iiif` directory. 

Alternatively from the command line you can run the following:

```
java -jar target/iiif-tiler.jar images/67352ccc-d1b0-11e1-89ae-279075081939.jpg
```

## Options

This generates tiles that are compatible with [IIIF Version 2](https://iiif.io/api/image/2.1/) and [Version 3.0](https://iiif.io/api/image/3.0/). Currently this is hard coded in Tiler.java. 
