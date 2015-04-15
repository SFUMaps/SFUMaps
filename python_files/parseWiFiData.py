import sqlite3, json, datetime, math
from pprint import pprint
# import matplotlib.pyplot as plt

# plot.ly imports
import plotly.plotly as py
from plotly.graph_objs import *

# consts
SSIDS=["SFUNET", "SFUNET-SECURE", "eduroam"]

# database vars
con = sqlite3.connect("wifi_data.db")
cur = con.cursor()

# converts unix timestamp to human readable date time
dt = lambda x: datetime.datetime.fromtimestamp(x)

# fetch data
cur.execute("select * from apsdata_SFU_BURNABY_AQ_3000_East_Street")
# sanitize the list //  also remove alien SSIDS
data = [( str(i[1]), str(i[2]), int(i[3]), int(i[4]), int(i[5]) ) for i in cur.fetchall() if i[1] in SSIDS]

# define time variables
startT, endT = data[0][-1], data[-1][-1]
totalMillis = (data[-1][-1] - data[0][-1])

"""
Split DATA by SSID
--------------------
"""

# fill dict with keys
tmpDict={i:[] for i in SSIDS}
# fill dict keys with data
for i in data:
    tmpDict[i[0]].append(i)
data = tmpDict

"""
Group DATA by BSSID with it's rssi levels and other info
---------------------------------------------------------
"""

# create dict to store bssid group dicts
bssids={}
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

    bssids[i] = tmpDict


traces=[]

for i in SSIDS:
    # pprint(bssids[i])
    for j in bssids[i]:

        rssis, xTime = [],[]
        for k in bssids[i][j]:
            if k[3]>-80: # some rssi filtering
                rssis.append(k[3])
                xTime.append((k[-1]-startT)/1000.0)

        if len(rssis) > 15:
            print rssis
            Ttrace = Scatter( x=xTime, y=rssis )
            traces.append(Ttrace)
    # break

data = Data(traces)
unique_url = py.plot(data, filename = 'basic-line')


"""
For after the Final Exams:
--------------------------

Basically the plan for plotting the data accurately on the map is:
- Take all diff RSSI levels of each BSSID (filter ones with very large values)
- now we have each APs heatmap
- we know from our heatmap at what time the ap entered and went away
- the `totalMillis` is our line space
- we draw our heatmap inside this space, hopefully giving us really good coverage of the area

- Try drawing the data on the map to get a better view of the data
  |--> If possible fade the rssi data rows by rssi (good rssi = darker elsewise lighter dots on map)

"""
