#!/usr/bin/env python2

import json
import sqlite3
import copy
from wsgiref.simple_server import make_server

from cgi import parse_qs, escape

def application (environ, start_response):
	try:
		request_body_size = int(environ.get('CONTENT_LENGTH', 0))
	except (ValueError):
		request_body_size = 0

	print(environ.get('CONTENT_TYPE',0))

	request_body = environ['wsgi.input'].read(request_body_size)
	d = json.loads(request_body)

	con = sqlite3.connect('gcm.db')
	cur = con.cursor()
	cur.execute('CREATE TABLE IF NOT EXISTS registration_ids(registration_id TEXT) ')
	con.commit()

	response = {}

	if 'action' in d.keys():
		if d['action'] == 'register': # register new device
			if 'registrationID' in d.keys():
				regid = d['registrationID']
				cur.execute('INSERT INTO registration_ids VALUES (?)', (regid,))
				con.commit()
				response['regID'] = regid
				response['status'] = 'success'

		if d['action'] == 'send': # send message
			cur.execute('SELECT registration_id FROM registration_ids')
			res = cur.fetchall()
			response['recp'] = [r[0] for r in res]
			response['status'] = 'success'
			msg = copy.deepcopy(d)
			del msg['action']
			if 'registrationID' in d.keys():
				del msg['registrationID']
			response['message'] = msg
			# TODO send message to GCM-Server

	con.commit()
	con.close()

	response_body = json.dumps(response)

	status = '200 OK'
	response_headers = [('Content-Type','application/json'),
		('Content-Length',str(len(response_body)))]
	start_response(status,response_headers)
	return [response_body]

if __name__ == "__main__":
	httpd = make_server('localhost', 8080, application)
	httpd.serve_forever()