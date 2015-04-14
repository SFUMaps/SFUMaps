import sqlite3
from pprint import pprint
import matplotlib.pyplot as plt

# consts
SSIDS=["SFUNET", "SFUNET-SECURE", "eduroam"]

# database vars
con = sqlite3.connect("wifi_data.db")
cur = con.cursor()

# fetch data
cur.execute("select * from apsdata_SFU_BURNABY_AQ_3000_East_Street")
# sanitize the list //  also remove alien SSIDS
data = [( str(i[1]), str(i[2]), int(i[3]), int(i[4]), int(i[5]) ) for i in cur.fetchall() if i[1] in SSIDS]

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

# create array to store bssid group dicts
bssids=[]
# split data by bssid
for i in SSIDS:
    # sort by time in case we are outta order
    Tdata = sorted(data[i], key=lambda x: x[-1])
    # create a tmp dictionary for storing split values for each ssid
    tmpDict={}
    # append values to dict where each key is a unique BSSID
    for j in Tdata:
        try:
            tmpDict[j[1]].append(j)
        except:
            tmpDict[j[1]] = [j]
    bssids.append(tmpDict)

summ=0
# graph data
for i in bssids:
    for j in i:
        # pprint((j,i[j]))
        # extract rssi levels
        rssis = [k[3] for k in i[j]]
        if len(rssis) > 15:
            summ+=1
            # pprint(i[j])
            print "min:",max(rssis),"max:",min(rssis),i[j][0]
            # plt.plot(rssis)
print summ
plt.show()
