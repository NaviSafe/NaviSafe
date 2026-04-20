import requests
import os
from dotenv import load_dotenv

load_dotenv()

url = "https://dapi.kakao.com/v2/local/search/keyword.json"

headers = {
    "Authorization": f"KakaoAK {os.getenv('KAKAO_API_KEY')}"
}

params = {
    "query": "신림역"
}

res = requests.get(url, headers=headers, params=params)
print(res.json())
