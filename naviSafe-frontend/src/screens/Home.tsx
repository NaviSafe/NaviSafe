import { useEffect } from "react";
import { KakaoMap } from "../components/KakaoMap";
import { useGpsStore } from "../store/gpsStore";
import { useOutbreakOccurState } from "../store/outbreakOccurStore";
import type { GpsItem } from "../store/gpsStore";
import type { OutbreakOccur } from "../store/outbreakOccurStore";
import proj4 from "proj4";
import axios from "axios";

// 좌표계 정의
proj4.defs("EPSG:2097", "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43");
proj4.defs("EPSG:4326", "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");

const GRS80 = "EPSG:2097";
const WGS84 = "EPSG:4326";


export const Home = () => {
  const setGpsList = useGpsStore((state) => state.setGpsList);
  const setOutbreakOccurList = useOutbreakOccurState((state) => state.setOutbreakOccurList);

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const res = await axios.get<OutbreakOccur[]>(
          `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/accInfo`
        );
        setOutbreakOccurList(res.data);

        // GRS80 → WGS84 변환
        const converted : GpsItem[] = res.data.map((item) => {
          const [x, y] = proj4(GRS80, WGS84, [item.grs80tmX, item.grs80tmY]);
          return { acc_id : item.accId , x : x, y : y };
        });
        console.log(converted);
        setGpsList(converted);
      } catch (err) {
        console.error("초기 데이터 로드 실패:", err);
      }
    };

    fetchInitialData();
  }, [setGpsList]);
  

  useEffect(() => {
    const ws = new WebSocket("ws://localhost:8080/ws/gps");

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if(message.type === "gps_batch"){
        // GRS80 → WGS84 변환
        const converted : GpsItem[] = message.data.map((item: GpsItem) => {
          const [x, y] = proj4(GRS80, WGS84, [item.x, item.y]);

          return {
            ...item,
            x,
            y,
          };
        });

        setGpsList(converted);
      }
    };

    return () => ws.close();
  }, [setGpsList])

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-4 text-center">
      <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-blue-500 mb-4">
        TailwindCSS 테스트 성공! 🎉
      </h1>
      <p className="text-gray-600 text-sm sm:text-base md:text-lg">
        반응형으로 잘 보이죠? 📱💻
      </p>

      <KakaoMap />
    </div>
  );
}
