# Installation 

## Prerequisites

## Google Cloud Platform Account
Sign up for a free trial at https://cloud.google.com/support/signup/

### App Engine SDK
Download the appropriate App Engine SDK at https://cloud.google.com/appengine/downloads

### Atlassian Cloud Account
Sign up for a free trial at https://www.atlassian.com/ondemand/signup/

### Google Translate API Key for languagecheck project
* Login to Google Cloud Plaform Developer Console at https://console.developers.google.com
* Create a Project for your App
* Wait for project to be done being created
* Click on Project
* Expand API's & Auth
* Click on Credentials
* Under Public API access choose Create New Key
* Choose server key
* Hit Ok

### Place API key in appropriate file
* For Go - place in config/config.gcfg
* For Java - place in war/WEB-INF/classes/config.properties 
* For PHP - place in php.ini
* For Python - place in config/apikey.json

## Development

### Launch Dev Server

#### Go
* Fire up a terminal in the root folder of the app you want to run: helloworld or languagecheck
* Run command <code>goapp server</code>

#### Java
* Fire up Eclipse
* Follow instructions for creating a new project at https://cloud.google.com/appengine/docs/java/tools/eclipse#Creating_a_Project
* Run app as web applicaiton that you try: helloworld or languagecheck

#### PHP & Python
* Fire up a terminal in the root folder of the app you want to run: helloworld or languagecheck
* Run command <code>dev_appserver.py app.yaml</code>

### Connect to this Add on.
* Login to local JIRA install
* Go to Settings -> Add-ons
* From Atlassian Marketplace choose "Manage Add-ons"
* Click "Upload add-on"
* In From this URL enter <code>http://localhost:8080/atlassian-connect.json</code>
* Click Ok on success message

### Test Add on
* Refresh Page
* Click "Greeting" or "Language Check" in top menu. (Will depend on which application you chose.)

## Production - as a Private Listing

### Prepare for Production
* Update string [your project ID] to match your project id without [] in all app.yaml files and appengine-web.xml files
* Update location for atlassian-connect/all.js to point to public JIRA install wherever in your front end application is is called
* Update urls in your atlassian-connect.json file to point to your production url
* Make sure those urls are written with secure  <code>https://</code> prefix

### Deploy to Production

#### PHP & Python
* Fire up a terminal in the root folder of the app you want to run: helloworld or languagecheck
* Run <code>appcfg.py update . --oauth2</code>

#### Go
* Fire up a terminal in the root folder of the app you want to run: helloworld or languagecheck
* Run <code>goapp deploy --oauth</code>

#### Java
* Fire up Eclipse
* Click Google Button in toolbar
* Choose "Deploy to App Engine"

### Signup for Atlassian Marketplace
* Follow directions here: https://developer.atlassian.com/static/connect/docs/latest/developing/cloud-installation.html

### Add Plugin to Private Add's in Atlassian 

### Connect to this Add on.
* Login to public JIRA install
* Go to Settings -> Add-ons
* From Atlassian Marketplace choose "Manage Add-ons"
* Scroll down to bottom of list
* Click settings
* Check Enable private listings
* Click "Upload add-on"
* In From this URL enter the installation URL you get by going to the Private Listing page of your Marketplace page for your plugin. (Done when you follow directions for "Signup for Atlassian Marketplace")  
* Click Ok on success message

### Test Add on
* Refresh Page
* Click "Greeting" or "Language Check" in top menu. (Will depend on which application you chose.)
