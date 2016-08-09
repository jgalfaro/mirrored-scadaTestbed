import matplotlib.pyplot as plt
import numpy as np

gtotal = np.loadtxt("gtotal_untouch_replayattack1470756838049", usecols=(0,1))
distance = np.loadtxt("gtotal_untouch_replayattack1470756838049", usecols=(0,2))
plt.plot(gtotal)
#plt.plot(distance)
plt.show()
