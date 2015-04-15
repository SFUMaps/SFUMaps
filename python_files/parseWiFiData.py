"""
Import required modules
-----------------------
"""

import sqlite3, difflib
from pprint import pprint
from datetime import datetime
# import matplotlib.pyplot as plt

# plot.ly imports
import plotly.plotly as py
from plotly.graph_objs import *


"""
Load data, initialize vars, and define functions
------------------------------------------------
"""

# consts
SSIDS=["SFUNET", "SFUNET-SECURE", "eduroam"]

# database vars
con = sqlite3.connect("wifi_data.db")
cur = con.cursor()

# converts unix timestamp to human readable date time
dt = lambda x: datetime.fromtimestamp(x)

# returns the similarity ratio for two lists
diff = lambda x,y: difflib.SequenceMatcher(None, x, y).ratio()

# fetch data
cur.execute("select * from apsdata_SFU_BURNABY_AQ_3000_East_Street_1")
# sanitize the list //  also remove alien SSIDS
data = [( str(i[1]), str(i[2]), int(i[3]), int(i[4]), int(i[5]) ) for i in cur.fetchall() if i[1] in SSIDS]

# define time variables
startT, endT = data[0][-1], data[-1][-1]
totalMillis = (data[-1][-1] - data[0][-1])

# checking my movement w/ respect to time
times = [i[-1]-startT for i in data]

# traces for the plot.ly map
traces=[]


"""
The main program: >
-------------------
"""


# 1. Split DATA by SSID ---------------------------------------------

# fill dict with keys
tmpDict={i:[] for i in SSIDS}
# fill dict keys with data
for i in data:
    tmpDict[i[0]].append(i)
data = tmpDict


# 2. Group DATA by BSSID for each SSID -----------------------------

# create dict to store bssid group dicts
tmp_data={}
# split data by bssid
for i in SSIDS:
    # grab the data chunk for this ssid
    Tdata = data[i]
    # create a tmp dictionary for storing split values for each ssid
    tmpDict={}
    # append values to dict where each key is a unique BSSID
    for j in Tdata:
        try:
            tmpDict[j[1]].append(j)
        except:
            tmpDict[j[1]] = [j]

    tmp_data[i] = tmpDict
data = tmp_data

# 3. Graphing the data for APs ---------------------------------------

# create graph data for given ssid
def addToTrace(ssid):
    print "graphing:",ssid,"APs:",len(data[ssid])
    for j in data[ssid]:
        rssis, xTime = [],[]
        for k in data[ssid][j]:
            if k[3]>-80: # some rssi filtering
                rssis.append(k[3])
                xTime.append((k[-1]-startT)/1000.0)

        if len(rssis) > 15:
            # print rssis
            Ttrace = Scatter( x=xTime, y=rssis )
            traces.append(Ttrace)



for i in SSIDS:
    addToTrace(i)

data = Data(traces)

# data = Data([Scatter(y=times)])

unique_url = py.plot(data, filename = 'basic-line')


"""
NeXt Step:
--------------------------

Basically the plan for plotting the data accurately on the map is:
- take all diff RSSI levels of each BSSID (very large rssi values get filtered out)
- now we have each APs heatmap
- we know from our heatmap at what time the ap entered and left our line space
- the `totalMillis` is our whole line space
- we draw our heatmap inside this space, hopefully giving us good coverage of the area

- Try drawing the data on the map to get a better view of the data
  |--> If possible fade the rssi data rows by rssi (good rssi = darker elsewise lighter dots on map)

"""
