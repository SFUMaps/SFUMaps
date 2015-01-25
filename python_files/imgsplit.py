import sys, image_slicer
print sys.argv
image_slicer.slice(sys.argv[1], float(sys.argv[2]))
