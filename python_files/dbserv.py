import SimpleHTTPServer, SocketServer, time, os, sys, sqlite3, json
from pprint import pprint
from urlparse import urlparse, parse_qs
from ParseWiFiData import DataParser

# import plotly.plotly as py
# from plotly.graph_objs import *

server_addr, server_port = ('localhost', 8080)

class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):


    def do_GET(self):
        # show the dir list
        SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)

    def do_POST(self):
        try:
            content_length = int(self.headers['Content-Length'])
            content = self.rfile.read(content_length)
            post_data = parse_qs(urlparse(content).path)

            data=[]
            parser = DataParser("wifi_data.db")

            for i in post_data['tables[]']:
                parser.set_table("apsdata_SFU_BURNABY_AQ_3000_East_Street"+i)
                parser.parseData()
                data.append([parser.data, parser.startT, parser.endT, parser.totalMillis, parser.times])


            self.send_response(200)
            self.send_header('Content-type','application/json')
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps( data ))
        except:
            print "Unexpected error:"+ str(sys.exc_info())
        return

httpd = SocketServer.TCPServer((server_addr, server_port), ServerHandler)

print "Serving on port:", server_port

try: httpd.serve_forever()
except KeyboardInterrupt: httpd.shutdown()
