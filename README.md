[![Build Status](https://travis-ci.org/glenrobson/iiif-tiler.svg?branch=master)](https://travis-ci.org/glenrobson/iiif-tiler)

# iiif-tiler
A Java based static IIIF tile generator. Static IIIF Tiles can be stored on a web service and provide a zoomable service with a compatible IIIF image viewers. This is known as a `level 0` image service. 

## Usage:
By default and without configuration the iiif-tiler will generate version 2.0 image tiles in the iiif directory. To run this program do the following:

 1. Download the `iiif-tiler.jar` from the [releases page](https://github.com/glenrobson/iiif-tiler/releases). 
 2. Save the `iiif-tiler.jar` to a directory where you would like to store the image tiles
 3. Drag an image on to the iiif-tiler.jar in the Finder or Windows interface.
 4. This will generate the IIIF tiles in a `iiif` directory. 

Alternatively from the command line you can run the following:

```
java -jar target/iiif-tiler.jar images/67352ccc-d1b0-11e1-89ae-279075081939.jpg
```

## Options

This generates tiles that are compatible with [IIIF Version 2](https://iiif.io/api/image/2.1/) and [Version 3.0](https://iiif.io/api/image/3.0/). This can be configured from the command line by passing `-version 3` parameter. A full list of parameters is below:

```
$ java -jar target/iiif-tiler.jar -help
usage: iiif-tiler
 -help                Show this help message
 -output <arg>        Directory where the IIIF images are generated. Default: iiif
 -identifier <arg>    The root of the identifier in the info.json. The filename directory is appended. Default: `http://localhost:8887/iiif/`
 -tile_size <arg>     set the tile size. Default is 1024
 -version <arg>       set the IIIF version. Default is 2.1.1 and options are 2 or 3
 -zoom_levels <arg>   set the number of zoom levels for this image. The default is 5
```

## Releases

The releasable jar file with all of the dependencies is in `target/iiif-tiler.jar`. The basic code without dependencies is in `target/iiif-tiler-$version.jar`. 

To release a new version to maven central create a new release on Github or run `mvn clean deploy`.
