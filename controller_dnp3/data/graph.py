import plotly.plotly as py
import plotly.graph_objs as go
import numpy as np

gtotal = np.loadtxt("cyberphysicalattack1", usecols=(1,))
time = np.loadtxt("cyberphysicalattack1", usecols=(0,))

trace = go.Scatter(
	x = time,
	y = gtotal
)
data = [trace]
py.sign_in('juanro', 'bndxzextmu')
py.iplot(data, filename='basic-line')

py.iplot(data, filename='basic-line')