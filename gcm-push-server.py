#!/usr/bin/env python2

import json
import sqlite3
import copy
import os

from gcmclient import *
from wsgiref.simple_server import make_server

from cgi import parse_qs, escape

HERE = os.path.dirname(__file__)
DATABASE_FILE = os.path.join(HERE, "gcm.db")
CONFIG_FILE = os.path.join(HERE, "gcm-push-server.config")
CONFIG = json.load(open(CONFIG_FILE,'r'))

API_KEY = CONFIG["apikey"]
DRY_RUN = CONFIG["dryrun"]

def application (environ, start_response):
	try:
		request_body_size = int(environ.get('CONTENT_LENGTH', 0))
	except (ValueError):
		request_body_size = 0

	print(environ.get('CONTENT_TYPE',0))

	request_body = environ['wsgi.input'].read(request_body_size)
	d = json.loads(request_body)

	con = sqlite3.connect(DATABASE_FILE)
	cur = con.cursor()
	cur.execute('CREATE TABLE IF NOT EXISTS registration_ids(registration_id TEXT UNIQUE) ')
	con.commit()

	response = {}

	if 'action' in d.keys():
		if d['action'] == 'register': # register new device
			if 'registrationID' in d.keys():
				regid = d['registrationID']
				try:
					cur.execute('INSERT INTO registration_ids VALUES (?)', (regid,))
					con.commit()
					response['regID'] = regid
					response['status'] = 'success'
				except sqlite3.IntegrityError, e:
					print(e)
					response['status'] = 'id already registered'

		if d['action'] == 'send': # send message
			cur.execute('SELECT registration_id FROM registration_ids')
			res = cur.fetchall()
			recipients = [r[0] for r in res]
			response['recp'] = recipients
			msg = copy.deepcopy(d)
			del msg['action']
			if 'registrationID' in d.keys():
				del msg['registrationID']
			response['message'] = msg
			# TODO send message to GCM-Server
			response['status'] = send_message(recipients,msg,cur)

	con.commit()
	con.close()

	response_body = json.dumps(response)

	status = '200 OK'
	response_headers = [('Content-Type','application/json'),
		('Content-Length',str(len(response_body)))]
	start_response(status,response_headers)
	return [response_body]

def send_message(recipients,message,cursor):
	status = ""
	if API_KEY != "" :
		gcm = GCM(API_KEY)
		message = JSONMessage(recipients, message, dry_run = DRY_RUN)
		try:
			res = gcm.send(message)
			for reg_id, msg_id in res.success.items():
				print("Successfully sent %s as %s" % (reg_id, msg_id))

			for reg_id, new_reg_id in res.canonical.items():
				print("Replacing %s with %s in database" % (reg_id, new_reg_id))
				cursor.execute('DELETE FROM registration_ids WHERE (registration_id == ?)', (reg_id,))
				ur.execute('INSERT INTO registration_ids VALUES (?)', (new_reg_i,d))

			for reg_id in res.not_registered:
				print("Removing %s from database" % reg_id)
				cursor.execute('DELETE FROM registration_ids WHERE (registration_id == ?)', (reg_id,))

			# unrecoverably failed, these ID's will not be retried
			# consult GCM manual for all error codes
			for reg_id, err_code in res.failed.items():
				print("Removing %s because %s" % (reg_id, err_code))
				cursor.execute('DELETE FROM registration_ids WHERE (registration_id == ?)', (reg_id,))

			# if some registration ID's have recoverably failed
			if res.needs_retry():
				# construct new message with only failed regids
				retry_msg = res.retry()
				# you have to wait before attemting again. delay()
				# will tell you how long to wait depending on your
				# current retry counter, starting from 0.
				# TODO retry sending message
				print("Wait or schedule task after %s seconds" % res.delay(retry))
				# retry += 1 and send retry_msg again
		except GCMAuthenticationError:
			status = "Your Google API key is rejected"
		except ValueError, e:
			status = "Invalid message/option or invalid GCM response" + e.args[0]
		except Exception:
			status = "Something wrong with requests library"
	else:
		status = "Google API key is required"
	return status

# run locally for testing and development
if __name__ == "__main__":
	httpd = make_server('localhost', 8080, application)
	httpd.serve_forever()