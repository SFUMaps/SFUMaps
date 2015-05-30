import sqlite3

import matplotlib.pyplot as plt
import numpy as np
import scipy.fftpack


conn = sqlite3.connect("wifi_data.db")

with conn:
    cur = conn.cursor()
    cur.execute("select level from apsdata_test where ssid='SHAW-BD8CD9-5G'")
    data = cur.fetchall()
    x = [i for i in range(len(data))]
    y = [int(i[0]) for i in data]

    plt.plot(x,y, color='red')

    cur.execute("select level from apsdata_test where ssid='SHAW-BD8CD9'")
    data = cur.fetchall()
    x = [i for i in range(len(data))]
    y = [int(i[0]) for i in data]

    plt.plot(x,y, color='blue')

    plt.show()
