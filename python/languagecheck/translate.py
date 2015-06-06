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
import urllib
import urllib2
import json
import logging
from google.appengine.api import memcache


class Translate():
	default_target = "en"
	baseurl = "https://www.googleapis.com/language/translate/v2"

	def __init__(self, key, target):
	    self.key = key
	    self.target = target
	    if len(target) == 0:
	    	self.target = default_target

	# Gets a languge identifier for a given string. Will try cache first.   
	def detect(self, key):

		data = memcache.get(key)

		if data is not None:
			logging.info("Cache hit")
		else:
			logging.info("Cache miss")
			data = self.detectFromAPI(str)
			memcache.set(key, data)

		label = self.getLanguageLabel(data)
		return {'data': data, 'label': label}


	# Sends a string to Google API and gets the language value for it. 
	def detectFromAPI(self, str):
		path = "/detect"

		str = str.encode("utf-8")


		params = urllib.urlencode({'q':str,'key':self.key})
		query = "?" + params
		url = self.baseurl + path + query;

		try:
			result = urllib2.urlopen(url)
			j = json.loads(result.read())
			return j['data']['detections'][0][0]['language']
		except urllib2.URLError, e:
			# You probably want to handle this error a little more completely. 
			print("Error:")
			print(e.reason)
			return "Error"

	# Gets a list of languages from Google API.
	def languagesFromAPI(self):
		path = "/languages"
		query = "?target=" + self.target + "&key=" + self.key
		url = self.baseurl + path + query;
		langs = {}

		try:
			result = urllib2.urlopen(url)
			j = json.loads(result.read())

			for item in j['data']['languages']:
				langs[ item['language']] = item['name']

			return langs
		except urllib2.URLError, e:
			# You probably want to handle this error a little more completely. 
			print("Error:")
			print(e.reason)
			return {"Error"}

	# Gets the label for a give language (lang) in target language (self) 
	# so getLanguageLabel("fr", "en") should yield "Anglais"
	#  Will try cache first.
	def getLanguageLabel(self, lang):
		key = "lang:" + self.target + ":" + lang

		data = memcache.get(key)

		if data is not None:
			logging.info("Cache hit")
			return data
		else:
			logging.info("Cache miss")
			langs = self.languagesFromAPI()

			for i in langs:
				tempkey = "lang:" + self.target +":" + i;
				memcache.set(tempkey, langs[i])

			data = langs[lang]
			return data

