#!/usr/bin/python
#./extractcol col1 col2 filename
import sys
from math import sqrt

peakchop = 1000
colnum = 4

max = 0
min = 100000000
total = 0
count = 0
totalv = 0
with open(sys.argv[3]) as f:
    for line in f:
	col = line.split()
	number = float(col[colnum])
	#lat = float(col[4])
	#gtotal = float(col[1])
	#time = float(col[0])
	#number = gtotal/lat
	if ( number < peakchop):
		#distance = float(col[2])
		#if( number > .9 and distance > 45 and distance < 180): print("NG: %s \tDistance: %s \tTime: %s  \t\tL: %s -- G: %s" % (number, distance,time,lat,gtotal))	
		total = total + number
		if( number < min): min = number
		if( number > max): max = number
		totalv = totalv + number*number
		count += 1


mean = total/float(count)
print("Mean: " + str(mean))	
print("Max: " + str(max))
print("Min: " + str(min))
var = totalv/float(count)
print("Variance: " + str(var))
print("S Deviation: " + str(sqrt(var)))
print("Data set size: " + str(count))
