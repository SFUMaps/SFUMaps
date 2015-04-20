import sqlite3
from pprint import pprint


SSIDS = ["SFUNET", "SFUNET-SECURE", "eduroam"]

conn = sqlite3.connect("wifi_data.db")
Nconn = sqlite3.connect("WIFI_DATA")

def run_once(f):
    def wrapper(*args, **kwargs):
        if not wrapper.has_run:
            wrapper.has_run = True
            return f(*args, **kwargs)
    wrapper.has_run = False
    return wrapper

def get_data(table, conn):
    with conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM "+table)
        return cursor.fetchall()

def insert(tables, name, conn):
    with conn:
        cursor = conn.cursor()
        # create table
        createTable = "CREATE TABLE IF NOT EXISTS "+name+" (_id INTEGER PRIMARY KEY AUTOINCREMENT, ssid TEXT, bssid TEXT, freq INTEGER, level INTEGER, rec_time INTEGER)"
        cursor.execute(createTable)

        # insert data
        # format = (ssid, bssid, freq, level, rec_time)

        for data in tables:
            print "adding:",len(data),"rows"
            for i in data:
                ssid = str(i[1])
                bssid = str(i[2])
                freq = int(i[3])
                level = int(i[4])
                rec_time = int(i[5])

                if ssid not in SSIDS: continue

                INSERT = "INSERT INTO "+name+" (ssid, bssid, freq, level, rec_time) VALUES ('{0}', '{1}', '{2}', '{3}', '{4}')".format(ssid, bssid, freq, level, rec_time)
                cursor.execute(INSERT)
            header(name, cursor)

@run_once
def header(name, cur):
    # insert header
    INSERT = "INSERT INTO "+name+" (ssid, bssid, freq, level, rec_time) VALUES ('REVERSE', 'REVERSE', '-1', '-1', '-1')"
    cur.execute(INSERT)
    print 'added header'


table1 = get_data("apsdata_SFU_BURNABY_AQ_3000_East_Street_1", conn)
table2 = get_data("apsdata_SFU_BURNABY_AQ_3000_East_Street_R_1", conn)

insert([table1, table2], "apsdata_SFU_BURNABY_AQ_3000_East_Street", Nconn)
