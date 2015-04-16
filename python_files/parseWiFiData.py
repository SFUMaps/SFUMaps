"""
Import required modules
-----------------------
"""

import sqlite3, difflib, os
from pprint import pprint
from datetime import datetime

class DataParser:

    # Consts.
    SSIDS=["SFUNET", "SFUNET-SECURE", "eduroam"]

    # converts unix timestamp to a readable date time
    dt = lambda x: datetime.fromtimestamp(x)
    # returns the similarity ratio for two lists
    diff = lambda x,y: difflib.SequenceMatcher(None, x, y).ratio()


    # init this class for the specified database
    def __init__(self, database):
        self.cur = sqlite3.connect(database).cursor()

    # returns the list of all tables in the database
    def get_data_tables(self):
        self.cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
        return [str(i[0]) for i in self.cur.fetchall()[1:]]


    # get data
    def set_table(self, tableName):
        # fetch data from table
        self.cur.execute("select * from " + tableName)
        # sanitize the list //  also remove alien SSIDS
        self.data = [( str(i[1]), str(i[2]), int(i[3]), int(i[4]), int(i[5]) ) for i in self.cur.fetchall() if i[1] in DataParser.SSIDS]

        # set time vars
        self.startT, self.endT = self.data[0][-1], self.data[-1][-1]
        self.totalMillis = (self.data[-1][-1] - self.data[0][-1])

        # time values that data was recorded at to make sure
        # we have a constant rate
        self.times = [i[-1]-self.startT for i in self.data]


    # parse data
    def parseData(self):

        ### 1. Split DATA by SSID ---------------------------------------------

        # fill dict with keys
        tmpDict={i:[] for i in DataParser.SSIDS}
        # fill dict keys with data
        for i in self.data:
            tmpDict[i[0]].append(i)
        self.data = tmpDict

        ### 2. Group DATA by BSSID for each SSID -----------------------------

        # create dict to store bssid group dicts
        tmp_data={}
        # split data by bssid
        for i in DataParser.SSIDS:
            # grab the data chunk for this ssid
            Tdata = self.data[i]
            # create a tmp dictionary for storing split values for each ssid
            tmpDict={}
            # append values to dict where each key is a unique BSSID
            for j in Tdata:
                if j[3] > -80:
                    try:
                        tmpDict[j[1]].append(j)
                    except:
                        tmpDict[j[1]] = [j]

            # remove dict key if it has less than 15 items in it's array
            for k, v in tmpDict.items():
                if len([m[3] for m in v]) < 15:
                    del tmpDict[k]

            tmp_data[i] = tmpDict

        self.data = tmp_data

    def graphData(self):
        traces=[]
        for i in self.data:
            for j in self.data[i]:
                rssis = [k[3] for k in self.data[i][j]]
                xTimes = [k[-1] for k in self.data[i][j]]
                traces.append((xTimes, rssis))
        return traces



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
