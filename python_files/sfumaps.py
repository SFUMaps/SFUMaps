#!/usr/bin/env python

import sys, sqlite3, time

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

tm = time.time()

db_name=""

if len(sys.argv)==2: db_name = sys.argv[1]
else:
    print color.BOLD+color.RED+"no database provided...!?"+color.END
    sys.exit()

RSSI_THRESHOLD = -65
WIFIS = ["SFUNET", "SFUNET-SECURE", "eduroam"]

con = sqlite3.connect(db_name)
N_con = sqlite3.connect('N_'+db_name)
N_cur = N_con.cursor()

def createTableAddData(tbl_name, data):
    createTableQuery = "CREATE TABLE "+tbl_name+" (_id INTEGER PRIMARY KEY, ssid TEXT, bssid TEXT, freq TEXT, level TEXT, rec_time TEXT)"
    N_cur.execute(createTableQuery)

    for i in data:
        for j in i:
            insertQuery = "INSERT INTO "+tbl_name+"(ssid, bssid, freq, level, rec_time) VALUES ('%s', '%s', '%s', '%s', '%s')" % (str(j[0]), str(j[1]), str(j[2]), str(j[3]), str(j[4]))
            N_cur.execute(insertQuery)


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
        tmpData = [j[1:] for j in data if j[1] == i] # remove id from tuple using [1:]
        tmpData = [j for j in getStrongestBssids(tmpData) if int(j[3]) > RSSI_THRESHOLD]
        tmpData = sorted(tmpData, key = lambda x:int(x[-1])) #sorting by time

        eachWifiData.append(tmpData)

    return eachWifiData


def getData(cur, table):
    cur.execute("SELECT * FROM "+table)

    aps = cur.fetchall()

    filtered_aps = getFilteredAPs(aps)

    createTableAddData(table, filtered_aps)


with con:

    cur = con.cursor()

    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = [i[0] for i in cur.fetchall()[1:]]

    for table in tables:
        getData(cur, table)



print color.BOLD+('Took: '+color.DARKCYAN+str(time.time()-tm)+' seconds :)')+color.END



"""

Split wifi data into levels
======================================

Main Level = M (AQ, Tasc1 Lvl9, ASB, etc...)
Levels below Main = M-n, where n is the number of levels below (Tasc1 Lvl8..7, Education Building Lvl7, etc...)
Levels above Main = M+n, where n is the number of levels above (AQ Levels 4000..5000..6000, etc...)

- There will be different databases for each floor
- Each database will have tables for its parts of areas

"""
