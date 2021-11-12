from http.server import HTTPServer, BaseHTTPRequestHandler
import json
from io import BytesIO
from asn1crypto._ffi import null
import os

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'hi')

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        body = self.rfile.read(content_length)
        y = json.loads(body.decode('utf-8'))
        if "import" in y:
            with open('/home/sid/refjson.py', 'w') as f:
                f.write(y)
        else:
            print(y)    
        try:
            # generate x at R 
            if "import" not in y:
                os.system(y)
                message = {   
            "message":"success"
            }
            else:
                os.system('python3 /home/sid/refjson.py')
                message = {   
            "message":"success"
            }
        except rpy2.rinterface.RRuntimeError as e:
            self.send_response(404)
            self.end_headers()
            message = {   
            "message":str(e)
            }
        else:
            self.send_response(200)
            self.end_headers()
             
        y = json.dumps(message)         
        self.wfile.write(y.encode(encoding='utf_8', errors='strict'))

with open('config.json') as f:
          data = json.load(f)
httpd = HTTPServer((data['url'], int(data['port'])), SimpleHTTPRequestHandler)
httpd.serve_forever()