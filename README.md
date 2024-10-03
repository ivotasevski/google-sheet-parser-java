## Description
The project is a REST API with a single endpoint that is used to parse data from Google Sheet and return it as a JSON structure.

## Setup 
In order to be able to read data from Google Sheet, the following steps must be completed.

### Register Google Account
You must register a Google account if you do not have one. This account can but it is NOT MANDATORY 
to be the same as the one owning the Sheet document.

### Setup a project in Google Developer Console 
1. Go to Google Developer Console (https://console.developers.google.com/project) and create a project.  
2. You need to enable Google Sheets API for the project.  
3. You need create a service account for the project (under Credentials tab). The service account will be assigned a Gmail address.
4. Once the Service Account is created, you need to generate a Key for it. The key is a JSON file that can be downloaded only once. Save it.
5. In application.properties you need to specify the Base64 (with padding) encoded value of the JSON credentials file. You can use Notepad++ to convert the file contents into Base64 string.

### Share the Google Sheet document
In order to be able to read the document the service account must be given "Viewer" access.
To do this, open the targeted Sheets document from an account that has Owner access and share the document to the service account (by providing the Gmail address of the service account).

## Testing the endpoint
In order to test the endpoint, you can use the following CURL snippet:
```
curl --location 'http://localhost:8080/parse' \
--header 'Content-Type: application/json' \
--data '{
    "sheetId": "SHEET_ID_HERE",    
    "includeNullValuesInOutput": false,    
    "subSheets": [        
        {
            "name": "Sheet1",
            "headerRowPosition": 2
        },
        {
            "name": "Sheet2",
            "headerRowPosition": 2
        }
    ]
}'
```
Do not forget to adjust the SheetId and subSheets in order to read the information properly from the file.  
Only the sub-sheets specified in the list will be read. If the sub-sheed has a header row specified, the cell values from that row will be taken as property names.  
If a particular cell has a value but does not belong to a column defined with the header row (or there no header row defined), a generic name for the property "Column_[i]" will be used, where "i" is the order of the column.


