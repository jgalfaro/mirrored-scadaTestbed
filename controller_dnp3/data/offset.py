#!/usr/bin/python

import sys

offset= None
with open(sys.argv[1]) as f:
    for line in f:
	col = line.split()
	if(offset == None):
		offset = col[0]
	newval = float(col[0]) - float(offset)
	print str(newval) + " " + col[1]
	
