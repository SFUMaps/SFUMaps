import SimpleHTTPServer, SocketServer, time, os, sys, sqlite3, json
from pprint import pprint

server_addr, server_port = ('localhost', 8080)

class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):

    def do_GET(self):
        # show the dir list
        SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)

    def do_POST(self):
        con, data = None, {}

        try:
            con = sqlite3.connect("N_wifi_data")

            with con:

                cur = con.cursor()

                cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
                tables = [i[0] for i in cur.fetchall()] # remove id form table list

                for i in tables:
                    cur.execute("SELECT * FROM "+i)
                    data[i] = cur.fetchall()

        except:
            print ("Unexpected error:", sys.exc_info())

        if con != None:
            con.close()

        self.send_response(200)
        self.send_header('Content-type','application/json')
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        # Send the html message
        self.wfile.write(json.dumps(data))
        return

httpd = SocketServer.TCPServer((server_addr, server_port), ServerHandler)

print "Serving on port:", server_port

try: httpd.serve_forever()
except KeyboardInterrupt: httpd.shutdown()
