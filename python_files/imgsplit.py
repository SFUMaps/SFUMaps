#!/usr/bin/env python

"""
slice image into n tiles and organize
them into their folders for app to use
"""

import os, sys, math, struct, image_slicer
from time import time
from color import color

tm = time()

def get_image_info(data):
    w, h = struct.unpack('>LL', data[16:24])
    width, height = int(w), int(h)
    return width, height

def main( dims ):
    zoomlvl = int(sys.argv[1])
    numTiles = 4**zoomlvl
    numcols = 2**zoomlvl

    if not dims[0]/256 == numcols:
        print (color.BOLD+'Please provide an image: '+color.DARKCYAN+'('+'{0} x {1}'+')').format(numcols*256, numcols*256)+color.END
        return

    imgpath = sys.argv[2]


    basedir = os.path.dirname(imgpath)
    filename = os.path.basename(imgpath)


    # setup the root folder
    basedir += "/"+str(zoomlvl)
    os.mkdir(basedir)

    tiles = image_slicer.slice(imgpath, float(numTiles), save=False)
    image_slicer.save_tiles(tiles, directory=basedir)

    for i in range(numcols): os.mkdir(basedir+"/"+str(i))

    files = [file for r,d,files in os.walk(basedir) for file in files if file[0] != "." and file != os.path.basename(imgpath)]

    fileChunks = [files[i:i+numcols] for i in range(0, len(files), numcols)]

    for i, chunk in enumerate(fileChunks):
        for j, file in enumerate(chunk):
            os.rename((basedir+"/"+file), (basedir+"/"+str(j)+"/"+str(i)+".png"))


    print color.BOLD+('Took: '+color.DARKCYAN+color.UNDERLINE+str(time()-tm)+' seconds :)')+color.END
    print color.BOLD+'Saved to --> '+color.UNDERLINE+color.DARKCYAN+basedir+color.END


if len(sys.argv) == 3:
    with open(sys.argv[2], 'rb') as f: data = f.read()
    if (data[:8] == '\211PNG\r\n\032\n'and (data[12:16] == 'IHDR')):
        main( get_image_info(data) )
    else:print color.BOLD+color.RED+color.UNDERLINE+'ERR:'+color.END+color.BOLD+color.DARKCYAN+' please provide a PNG file'+color.END
elif len(sys.argv) == 2:
    print (color.UNDERLINE+color.BOLD+'Please provide an image: '+color.DARKCYAN+'('+'{0} x {1}'+')').format((2**int(sys.argv[1]))*256, (2**int(sys.argv[1]))*256)+color.END
else:
    print color.BOLD+"usage: [ filepath ] [ zoom-level ]"+color.END
