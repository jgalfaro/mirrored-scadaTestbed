#!/usr/bin/python

import plotly.plotly as py
import plotly.graph_objs as go
import numpy as np
import sys

gtotal = np.loadtxt(sys.argv[1], usecols=(1,))
time = np.loadtxt(sys.argv[1], usecols=(0,))

trace = go.Scatter(
	x = time,
	y = gtotal
)
data = [trace]
py.sign_in('juanro', 'bndxzextmu')
py.iplot(data, filename=sys.argv[1])