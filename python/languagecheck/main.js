/*
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
*/
var baselangauge = "en";
var api_url = "/check";

getIssues();

function getIssues() {
 AP.require(['request'], function(request) {
     request({
         url: '/rest/api/latest/search/',
         success: function(response) {
             // Convert the string response to JSON
             response = JSON.parse(response);
            for (var i=0; i <response.issues.length; i++) {
            	var issue = response.issues[i];
            	detectLanguage(issue);
            }
         },
         error: function(response) {
             console.log("Error loading API (" + uri + ")");
             console.log(arguments);
         },
         contentType: "application/json"
     });
 });
}

function detectLanguage(issue){
	 
	$.get( api_url, { q: issue.fields.summary, target: baselangauge}, 
    function(data){
      lang = data;
      if (lang.data != baselangauge){

        addIssueToList(issue, lang)
      }
  });

}

function addIssueToList(issue, language){
	var baseUrl = getQueryVariable('xdm_e') + getQueryVariable('cp');
    baseUrl = htmlEscape(baseUrl);
    alert(baseUrl);
    var link = $("<a>", {href: baseUrl + '/browse/' + issue.key, target:"_top"}).append( issue.fields.summary );
    var li = $("<li>");
    li.append(link);
    li.append( " (" + language.label + ")");
  $("#issues-list").append(li);
}

function getQueryVariable(variable)
{
 var query = window.location.search.substring(1);
 var vars = query.split("&");
 for (var i=0;i<vars.length;i++) {
         var pair = vars[i].split("=");
         if(pair[0] == variable){return decodeURIComponent(pair[1]);}
 }
 return(false);
}

function htmlEscape(text) {
   return text.replace(/&/g, '&amp;').
     replace(/</g, '&lt;').  // it's not neccessary to escape >
     replace(/"/g, '&quot;').
     replace(/'/g, '&#039;');
}