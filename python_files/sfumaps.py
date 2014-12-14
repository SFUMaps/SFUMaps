"""

Split stuff into levels
======================================

Main Level = M (AQ, Tasc1 Lvl9, ASB, etc...)
Levels below Main = M-n, where n is the number of levels below (Tasc1 Lvl8..7, Education Building Lvl7, etc...)
Levels above Main = M+n, where n is the number of levels above (AQ Levels 4000..5000..6000, etc...)

- There will be different databases for each floor
- Each database will have tables for its parts of areas

"""


import sqlite3 as lite
import sys

con = lite.connect('wifi_data')

GOOD_RSSI_VAL = -65
WIFIS = ["SFUNET", "SFUNET-SECURE", "eduroam"]


def fetchTables(cur):
    TABLE_EXCEPTIONS = []
    TABLE_EXCEPTIONS = ["apsdata_asbtotasc1entrhallway", "apsdata_csiltotasc1mainlvl", "apsdata_csiltotascmainlvlRfromAQ", "apsdata_aqtoblussonhall", "apsdata_blussonhallREVERSE"]
    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = [i[0] for i in cur.fetchall()[1:] if ((i[0] not in TABLE_EXCEPTIONS) and ("tasc" not in i[0]))]
    return tables

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


def filterAPs(data):
    eachWifiData = []
    for i in WIFIS:
        # remove id from tuple with [1:]
        tmpData = [j[1:] for j in data if j[1] == i]
        tmpData = [j for j in getStrongestBssids(tmpData) if int(j[3]) > GOOD_RSSI_VAL]
        tmpData = sorted(tmpData, key = lambda x:int(x[-1]))
        eachWifiData.append(tmpData)


    for i in eachWifiData:
        print
        print "+ + + + + + "+i[0][0]+" + + + + + + +"
        print
        for j in i: print j


with con:

    cur = con.cursor()

    tables = fetchTables(cur)

    for i in tables:
        print; print; print """
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        """,i.upper(),"""
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        """;print

        cur.execute("SELECT * FROM "+i)

        aps = cur.fetchall()



        if "REVERSE" in i: filterAPs(aps[::-1])
        else: filterAPs(aps)








"""
         android_metadata                  | apsdata_tasclvl7rightnearREVERSE
         apsdata_aqmacmtoedbside           | apsdata_tasclvl7to8
         apsdata_aqmainedbside             | apsdata_tasclvl8rightfar
         apsdata_aqmainmacmsideREVERSE     | apsdata_tasclvl8rightnearREVERSE
         apsdata_aqtoblussonhall           | apsdata_tasclvl8to7
SFUNET - apsdata_asbtotasc1entrhallway     | apsdata_tasclvl8to9
         apsdata_blussonhallREVERSE        | apsdata_tasclvl9rightfar
         apsdata_csiltotasc1mainlvl        | apsdata_tasclvl9rigthnearREVERSE
         apsdata_csiltotascmainlvlRfromAQ  | apsdata_tasclvl9to8GOOD
         apsdata_tasclvl7rightnear         |

"""





#
