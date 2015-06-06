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

// check.php
// Acts as the API for the Javascript front end. 

require_once 'translate.php';
// Storing the API key in php.ini. Not sharing that. 
$apikey = get_cfg_var( "apikey" );

// Default to English. 
if (strlen($_GET['target']) > 0){
	$target = $_GET['target'];
} else {
	$target = "en";
}

// Send the string to language detect
$service = new Translate($apikey,$target);
$result = $service->detect($_GET['q']);

// Display the results. 
header('Content-Type: application/json');
echo $result;
?>