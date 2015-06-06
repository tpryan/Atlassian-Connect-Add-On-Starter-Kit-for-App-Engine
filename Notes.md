Both Java and Python versions suffered from problem where the Atlassian Marketplace Plugin was calling the base url "/" as "//".  Caused a lot of issues.  Try and track down. 

Make sure you look at any url that atlassian-connect.json is referring to. 
You must have a route in app.yaml open to it, or uploading the extention will fail. 