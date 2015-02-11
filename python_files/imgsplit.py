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
    numTiles = sys.argv[2]
    zoomlvl = int(math.log(float(numTiles), 4))
    numcols = 2**zoomlvl

    if not dims[0]/256 == numcols:
        print (color.BOLD+'Please provide an image: '+color.DARKCYAN+'('+'{0} x {1}'+')').format(numcols*256, numcols*256)+color.END
        return

    basedir = os.path.dirname(sys.argv[1])
    filename = os.path.basename(sys.argv[1])

    # setup the root folder
    basedir += "/"+str(zoomlvl)
    os.mkdir(basedir)

    tiles = image_slicer.slice(sys.argv[1], float(sys.argv[2]), save=False)
    image_slicer.save_tiles(tiles, directory=basedir)

    for i in range(numcols): os.mkdir(basedir+"/"+str(i))

    files = [file for r,d,files in os.walk(basedir) for file in files if file[0] != "." and file != os.path.basename(sys.argv[1])]

    fileChunks = [files[i:i+numcols] for i in range(0, len(files), numcols)]

    for i, chunk in enumerate(fileChunks):
        for j, file in enumerate(chunk):
            os.rename((basedir+"/"+file), (basedir+"/"+str(j)+"/"+str(i)+".png"))


    print color.BOLD+('Took: '+color.DARKCYAN+str(time()-tm)+' seconds :)')+color.END
    print color.BOLD+'Saved to: '+color.DARKCYAN+basedir+color.END


if len(sys.argv) == 3:
    with open(sys.argv[1], 'rb') as f: data = f.read()
    if (data[:8] == '\211PNG\r\n\032\n'and (data[12:16] == 'IHDR')):
        main( get_image_info(data) )
    else:print color.BOLD+color.RED+color.UNDERLINE+'ERR:'+color.END+color.BOLD+color.DARKCYAN+' please provide a PNG file'+color.END
else:
    print color.BOLD+"usage: [ filepath ] [ #tiles ]"+color.END
