#INSTALL EXTRA DEPENDENCIES IF YOU DONT HAVE THEM

import sqlite3 as lite
import sys
import numpy as np
import matplotlib.pyplot as plt
from scipy.interpolate import spline


def splitList(lt, i_el):
    split = [0]
    outerList = []
    for x in range(len(lt)-1):
        if(lt[x][i_el] != lt[x+1][i_el]):
            innerList = []
            for j in range(x,split[-1]-1,-1):
                innerList.append(lt[j])

            outerList.append(innerList)

            split.append(x+1) #append index where value changes

    endList = []
    for x in range(len(lt)-1, split[-1]-1, -1):
        endList.append(lt[x])

    outerList.append(endList)
    return outerList

def raw_graph(a,b):
    plt.plot(a,b)
    # plt.show()

def smooth(a,b):
    a=[int(i) for i in a]
    b=[int(i) for i in b]

    b_smooth = np.linspace(a[0], a[-1], 3000)

    a_smooth = spline(a,b,b_smooth)

    print a_smooth

    plt.plot(b_smooth, a_smooth)

def getbssids(raw_data):
    ssid="SFUNET"
    ssid_data = [i for i in raw_data if i[1]==ssid]
    allbssids = [i[2] for i in ssid_data]

    dic = {}
    for i in allbssids:
       dic[i] = 1
    diff_bssids = dic.keys()

    print "len:",len(diff_bssids);
    print;print "====================";print

    for i in diff_bssids:
        print i



con = lite.connect('wifi_data')

with con:

    cur = con.cursor()
    cur.execute("SELECT * FROM apsdata_blussonhall")
    raw_data=[];

    while True:
        row = cur.fetchone()
        if row == None:
            break
        raw_data.append(row)

    getbssids(raw_data)


#     raw_data = sorted(raw_data, key=lambda x: x[1]) # sort by ssid
#
#
#     unsorted_data = splitList(raw_data,1)# split at every diff ssid
#
#     sorted_data = []
#
#     #sort each sublist using the id and add to 'sorted_data'
#     for i in unsorted_data: sorted_data.append(sorted(i, key=lambda x: x[0]))
#
#
#     for i in sorted_data:
#         draw = False; a=[]; b=[]; num=00;
#         for j in i:
#         # for j in i[0:num]:
#             if j[1]=='SFUNET-SECURE':
#             # if True:
#                 draw = True
#                 b.append(j[4])
#         if draw:
#             a=[i for i in range(0,len(i)*1000, 1000)]
#             # a=[i for i in range(0,num*1000, 1000)]
#             raw_graph(a,b)
#             # smooth(a,b) # USE WITH CAUTION - takes longer when lots of data
#
#
#
# plt.show()
