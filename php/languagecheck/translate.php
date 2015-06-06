<!--
	Copyright 2015, Google, Inc.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<?php
/**
  * 
  * @package Translate
  * Encapsulates all the calls to Google Translation API
  */ 

class Translate { 
	var $baseurl = "https://www.googleapis.com/language/translate/v2";
	var $key = "";
	var $default_target = "";

	function __construct($key, $default_target="en") {
		$this->key = $key;
		$this->default_target = $default_target;
	}

	/**
	 * Calls Translate api to return a best guess at what language $str is written in.  
	 * @param string $str 
	 * @return string
	 */
	function detectFromAPI($str){
		$path = "/detect";
		$query = "?q=" . urlencode($str) . "&key=" . $this->key;
		$u = $this->baseurl . $path . $query;
		$response = file_get_contents($u, false);
		$result = json_decode($response);
		return $result->data->detections[0][0]->language;
	}

	/**
	 * Calls Translate api to return a hash of readable language labels keyed to language codes.   
	 * @return []
	 */
	function languagesFromAPI(){
		$path = "/languages";
		$query = "?target=" . $this->default_target . "&key=" . $this->key;
		$u = $this->baseurl . $path . $query;
		$response = file_get_contents($u, false);
		$result = json_decode($response);

		$langhash = [];

		foreach ($result->data->languages as &$value) {
		    $langhash[$value->language] = $value->name;
		}

		return $langhash;
	}
	
	/**
	 * Gets a language label from a language code. 
	 * Tries cache first, if not there, it populates cache. 
	 * @return string
	 */
	function getLanguageLabel($lang) {
		$key = "lang:" . $this->default_target .":" . $lang;
		$memcache = new Memcache;
		$data = $memcache->get($key);


		if ($data === false) {
			$hash = $this->languagesFromAPI();

			foreach ($hash as $key => $value) {
				$key = "lang:" . $this->default_target .":" . $key;
			    $memcache->set($key, $value);
			}

			$data = $hash[$lang];
		} 
		return $data;
	}

	/**
	 * Given a string returns the detected language and language code.
	 * Tries cache first, if not there, it populates cache.  
	 * @param string $str 
	 * @return obj
	 */
	function detect($str) {
		$key = $str;
		$memcache = new Memcache;
		$data = $memcache->get($key);

		if ($data === false) {
			$data = $this->detectFromAPI($str);
			$memcache->set($key, $data);
		}

		$label = $this->getLanguageLabel($data);
		$result = new stdClass(); 
		$result->label = $label;
		$result->data = $data;
		

		return json_encode($result);
	}

}

?>