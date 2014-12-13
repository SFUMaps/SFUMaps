import sqlite3 as lite
import sys

con = lite.connect('wifi_data')

MIN_RSSI_VAL = -75
GOOD_RSSI_VAL = -67

WIFIS=["SFUNET", "SFUNET-SECURE", "eduroam"]


def getValidAPs(data=[], ssid=""):
    allbssids=[]
    for j in WIFIS:
        ssidData = [i for i in data if (i[1]==j and int(i[4]) > MIN_RSSI_VAL)]

        dic={}
        for id, ssid, bssid, freq, rssi, date in ssidData:
            if bssid not in dic: # we see it for the first time
                dic[bssid] = (id, ssid, bssid, freq, rssi, date)
        ssidData = dic.values()
        ssidData = [i for i in ssidData if int(i[4]) > GOOD_RSSI_VAL]
        ssidData = sorted(ssidData, key=lambda x: x[-1])
        allbssids.append(ssidData)




    for i in allbssids:
        print; print "+ + + + + + + + + (",len(i),"-",i[0][1],") + + + + + + + + + +"; print
        for j in i:
            print j



def fetchTables(cur):
    TABLE_EXCEPTIONS = ["apsdata_asbtotasc1entrhallway", "apsdata_csiltotasc1mainlvl", "apsdata_csiltotascmainlvlRfromAQ", "apsdata_aqtoblussonhall"]
    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = [i[0] for i in cur.fetchall()[1:] if ((i[0] not in TABLE_EXCEPTIONS) and ("tasc" not in i[0]))]
    return tables

with con:

    cur = con.cursor()

    # tables = fetchTables(cur)

    # for i in tables:
    #     print; print; print """
    #     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    #     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    #     """,i,"""
    #     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    #     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    #     """;print

    table="apsdata_aqmacmtoedbside"
    cur.execute("SELECT * FROM "+table)
    aps=[];

    print; print; print """
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    """,table,"""
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    """;print


    while True:
        row = cur.fetchone()
        if row == None:
            break
        aps.append(row)


    getValidAPs(aps[::-1])
    # for i in aps[::-1]:
        # print i











"""
         android_metadata                  | apsdata_tasclvl7rightnearREVERSE
         apsdata_aqmacmtoedbside           | apsdata_tasclvl7to8
         /apsdata_aqmainedbside             | apsdata_tasclvl8rightfar
         /apsdata_aqmainmacmsideREVERSE     | apsdata_tasclvl8rightnearREVERSE
         apsdata_aqtoblussonhall           | apsdata_tasclvl8to7
SFUNET - apsdata_asbtotasc1entrhallway     | apsdata_tasclvl8to9
         apsdata_blussonhallREVERSE        | apsdata_tasclvl9rightfar
         apsdata_csiltotasc1mainlvl        | apsdata_tasclvl9rigthnearREVERSE
         apsdata_csiltotascmainlvlRfromAQ  | apsdata_tasclvl9to8GOOD
         apsdata_tasclvl7rightnear         |

"""




#
# cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
