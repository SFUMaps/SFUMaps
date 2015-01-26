"""
slice image into n tiles and organize
them into their folders for app to use
"""

import os, sys, math, image_slicer

numTiles = sys.argv[2]
zoomlvl = int(math.log(float(numTiles), 4))
numcols = 2**zoomlvl
basedir = os.path.dirname(sys.argv[1])
filename = os.path.basename(sys.argv[1])

# setup the root folder
basedir += "/"+str(zoomlvl)
os.mkdir(basedir)

tiles = image_slicer.slice(sys.argv[1], float(sys.argv[2]), save=False)
image_slicer.save_tiles(tiles, directory=basedir)

print "scanning",os.path.dirname(basedir)

for i in range(numcols): os.mkdir(basedir+"/"+str(i))

files = [file for r,d,files in os.walk(basedir) for file in files if file[0] != "." and file != os.path.basename(sys.argv[1])]

fileChunks = [files[i:i+numcols] for i in range(0, len(files), numcols)]

for i, chunk in enumerate(fileChunks):
    for j, file in enumerate(chunk):
        os.rename((basedir+"/"+file), (basedir+"/"+str(j)+"/"+str(i)+".png"))

print; print "DONE!";print
