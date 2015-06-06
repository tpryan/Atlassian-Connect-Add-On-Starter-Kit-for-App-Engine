#	Copyright 2015, Google, Inc.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2
import translate
import os
import json

class Check(webapp2.RequestHandler):

	# Answers the post request for check and installed tests for Atlassian Add on
	def post(self):
		self.response.headers['Content-Type'] = 'application/json'
		self.response.write("{}")

	# Acts as the API for the HTML front end. 
	def get(self):
		wd = os.getcwd()
		f = open('config/apikey.json', 'r')
		api_key = json.loads(f.read())
		f.close()

		q = self.request.get("q")
		target = self.request.get("target")

		if len(target) == 0:
			target = "en"

		t = translate.Translate(api_key, target)
		response = t.detect(q)

		self.response.headers['Content-Type'] = 'application/json'
		self.response.write(json.dumps(response))

app = webapp2.WSGIApplication([
	('/check', Check),
	('/installed', Check),
], debug=True)