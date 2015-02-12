#!/usr/bin/env python

"""
slice image into n tiles and organize
them into their folders for app to use
"""

import os, sys, math, struct, image_slicer
from time import time
from color import color
from PIL import Image

Image.MAX_IMAGE_PIXELS = None # disable Pillow Decompression bomb warning

tm = time()

def get_image_info(data):
    w, h = struct.unpack('>LL', data[16:24])
    width, height = int(w), int(h)
    return width, height

def tileImage(imgpath, zoom):
    numTiles = 4**zoom
    numCols = 2**zoom

    basedir = os.path.dirname(imgpath)
    filename = os.path.basename(imgpath)

    # setup the root folder
    basedir += "/"+str(zoom)
    os.mkdir(basedir)

    tiles = image_slicer.slice(imgpath, float(numTiles), save=False)
    image_slicer.save_tiles(tiles, directory=basedir)

    for i in range(numCols): os.mkdir(basedir+"/"+str(i))

    files = [file for r,d,files in os.walk(basedir) for file in files if file[0] != "." and file != os.path.basename(imgpath)]

    fileChunks = [files[i:i+numCols] for i in range(0, len(files), numCols)]

    for i, chunk in enumerate(fileChunks):
        for j, file in enumerate(chunk):
            os.rename((basedir+"/"+file), (basedir+"/"+str(j)+"/"+str(i)+".png"))

    return

def main( dims ):

    zoom = sys.argv[1]
    imgpath = sys.argv[2]

    zoomlvls=[]

    if len(zoom) > 1: zoomlvls = [int(i) for i in zoom.split(',')][::-1]
    else: zoomlvls = [int(zoom)]

    mxZoom = max(zoomlvls)

    if not (dims[0]/256.0 == float(2**mxZoom)):
        print (color.BOLD+'Please provide an image: '+color.DARKCYAN+'('+'{0} x {1}'+')').format((2**mxZoom)*256, (2**mxZoom)*256)+color.END
        return

    Img = Image.open(imgpath)
    fname = os.path.basename(imgpath)
    basedir = os.path.dirname(imgpath)
    for i in zoomlvls:
        thisImgPath=imgpath
        sz = (2**i)*256
        if sz != dims[0]:
            rImg = Img.resize( (sz, sz) )
            thisImgPath = basedir+'/'+str(sz)+'_'+fname
            out = file(thisImgPath, "w")
            try:
                rImg.save(out)
            finally:
                out.close()

        tileImage(thisImgPath, i)
        print color.BOLD+'Saved to --> '+color.UNDERLINE+color.DARKCYAN+os.path.dirname(thisImgPath)+'/'+str(i)+color.END


    print color.BOLD+('Took: '+color.DARKCYAN+color.UNDERLINE+str(time()-tm)+' seconds :)')+color.END


if len(sys.argv) == 3:
    with open(sys.argv[2], 'rb') as f: data = f.read()
    if (data[:8] == '\211PNG\r\n\032\n'and (data[12:16] == 'IHDR')):
        main( get_image_info(data) )
    else:print color.BOLD+color.RED+color.UNDERLINE+'ERR:'+color.END+color.BOLD+color.DARKCYAN+' please provide a PNG file'+color.END

elif len(sys.argv) == 2:
    print (color.UNDERLINE+color.BOLD+'Please provide an image: '+color.DARKCYAN+'('+'{0} x {1}'+')').format((2**int(sys.argv[1]))*256, (2**int(sys.argv[1]))*256)+color.END

else:
    print color.BOLD+"usage: \t[ zoom-level: n ] [ filepath ]\n\t[ zoom-level: (n,n-1,...,n-k) ] [ filepath ]"+color.END
