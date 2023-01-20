# A-QuB AccessServices

Set of services for accessing a Virtuoso triplestore. Supported services:
- running SPARQL queries ('Query')
- importing data ('Import')
- exporting data ('Export')
- updating data ('Update')

The triplestore details (url, username, password, etc.) are provided in a properties file (config.properties).

The services are used by [A-QuB-2](https://github.com/isl/A-QuB-2) for enabling the communication of this application to a Virtuoso triplestore. 
