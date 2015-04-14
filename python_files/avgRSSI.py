from pprint import pprint
import sys, sqlite3, time

def getTableData(tableName):
    cur.execute("SELECT level FROM "+tableName)
    return cur.fetchall()

con = sqlite3.connect("WIFI_DATA")


with con:

    cur = con.cursor()

    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = [i[0] for i in cur.fetchall()[1:]]

    rssis=[int(i[0]) for table in tables for i in getTableData(table)]

    avg = sum(rssis) / float(len(rssis))

    # pprint(rssis)
