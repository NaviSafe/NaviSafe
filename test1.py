import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

url = "https://www.safetydata.go.kr/V2/api/DSSP-IF-00247"
serviceKey = "724DRNLWJ92N6KC0"
payloads = {
    "serviceKey": serviceKey,
    "returnType": "json",
    "pageNo": "1",
    "numOfRows": "5",
    
}

response = requests.get(url, params=payloads)

print(response.json())   