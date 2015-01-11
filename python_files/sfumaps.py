from time import ctime
import sqlite3

DB_NAME = "wifi_data"
RSSI_THRESHOLD = -65
WIFIS = ["SFUNET", "SFUNET-SECURE", "eduroam"]

class color:
   PURPLE = '\033[95m'
   CYAN = '\033[96m'
   DARKCYAN = '\033[36m'
   BLUE = '\033[94m'
   GREEN = '\033[92m'
   YELLOW = '\033[93m'
   RED = '\033[91m'
   BOLD = '\033[1m'
   UNDERLINE = '\033[4m'
   END = '\033[0m'

con = sqlite3.connect(DB_NAME)


"""
for every data set we need to get the tuple
with min rssi val for each unique BSSID
"""

def getStrongestBssids(d):
    # sort by rssi then take out dups and
    # we'll get the bssids with better rssi
    uniqueDataSet = sorted(d, key = lambda x: x[3])

    dic = {}
    for ssid, bssid, freq, rssi, time in uniqueDataSet:
        if (str(bssid)) not in dic:
            dic[(str(bssid))] = (ssid, bssid, freq, rssi, time)

    uniqueDataSet = dic.values()

    return uniqueDataSet


def getFilteredAPs(data):
    eachWifiData = []
    for i in WIFIS:
        # maybe modify id here ...
        tmpData = [j[1:] for j in data if j[1] == i] # remove id from tuple using [1:]
        tmpData = [j for j in getStrongestBssids(tmpData) if int(j[3]) > RSSI_THRESHOLD]
        tmpData = sorted(tmpData, key = lambda x:int(x[-1])) #sorting by time

        tmpData = [((i+1),)+j for i,j in enumerate(tmpData)] # add id to each tuple
        tmpData = [i+(ctime(int(i[-1])/1000),) for i in tmpData] # add readable time

        eachWifiData.append(tmpData)

    return eachWifiData


with con:

    cur = con.cursor()

    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = [i[0] for i in cur.fetchall()[1:]]

    cur.execute("SELECT * FROM "+tables[3])

    print;print color.BOLD+"TABLE = "+color.DARKCYAN+tables[3][8:]+color.END

    aps = cur.fetchall()

    filtered_aps = getFilteredAPs(aps)


    for ap in filtered_aps:
        print;print;
        for j in ap: print j





"""

Split wifi data into levels
======================================

Main Level = M (AQ, Tasc1 Lvl9, ASB, etc...)
Levels below Main = M-n, where n is the number of levels below (Tasc1 Lvl8..7, Education Building Lvl7, etc...)
Levels above Main = M+n, where n is the number of levels above (AQ Levels 4000..5000..6000, etc...)

- There will be different databases for each floor
- Each database will have tables for its parts of areas

"""
