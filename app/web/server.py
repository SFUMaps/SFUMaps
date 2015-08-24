import SimpleHTTPServer, SocketServer, os, sys, json
from urlparse import urlparse, parse_qs

server_addr, server_port = ('localhost', 80)

class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):

    def do_GET(self):
        # server current directory
        SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)

    def do_POST(self):
        try:
            content_length = int(self.headers['Content-Length'])
            content = self.rfile.read(content_length)

            retrievedData = json.loads(content)
            returnData = {}

            if retrievedData.get('fetch_data') != None:
                with open("export.json") as json_file:
                    returnData = json.load(json_file)
            else:
                # savedJsonData = {}
                # # load saved data
                # with open("export.json") as json_file:
                #     savedJsonData = json.load(json_file)
                #
                # # add new data
                # savedJsonData['places'].append(retrievedData)

                # save new data
                with open('export.json', 'w') as json_file:
                    json_file.write(json.dumps(retrievedData))

                returnData = {'success': True}

            # send response back saying it's all good
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps(returnData))
        except:
            print "Unexpected error:"+ str(sys.exc_info())
        return

httpd = SocketServer.TCPServer((server_addr, server_port), ServerHandler)

print "Serving on port:", server_port
try: httpd.serve_forever()
except: pass

httpd.server_close()
