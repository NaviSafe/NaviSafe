import { useEffect } from "react";
import axios from "axios";
import proj4 from "proj4";
import { useGpsStore } from "../store/gpsStore";
import { useOutbreakOccurState } from "../store/outbreakOccurStore";
import type { GpsItem } from "../store/gpsStore";
import type { OutbreakOccur } from "../store/outbreakOccurStore";

// 좌표계 정의
proj4.defs(
    "EPSG:2097",
    "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43"
);
proj4.defs("EPSG:4326", "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");

const GRS80 = "EPSG:2097";
const WGS84 = "EPSG:4326";

export const useOutbreakOccur = () => {
    const setGpsList = useGpsStore((state) => state.setGpsList);
    const setOutbreakOccurList = useOutbreakOccurState((state) => state.setOutbreakOccurList);

    // 초기 데이터 fetch
    useEffect(() => {
        const fetchInitialData = async () => {
        try {
            const res = await axios.get<OutbreakOccur[]>(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/accInfo`
            );
            setOutbreakOccurList(res.data);

            // GRS80 → WGS84 변환
            const converted: GpsItem[] = res.data.map((item) => {
            const [x, y] = proj4(GRS80, WGS84, [item.grs80tmX, item.grs80tmY]);
            return { acc_id: item.accId, x, y };
            });
            setGpsList(converted);
        } catch (err) {
            console.error("초기 데이터 로드 실패:", err);
        }
        };

        fetchInitialData();
    }, [setGpsList, setOutbreakOccurList]);

  // WebSocket 실시간 업데이트
    useEffect(() => {
        const ws = new WebSocket("ws://localhost:8080/ws/gps");

        ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        if (message.type === "gps_batch") {
            const converted: GpsItem[] = message.data.map((item: GpsItem) => {
            const [x, y] = proj4(GRS80, WGS84, [item.x, item.y]);
            return { ...item, x, y };
            });

            setGpsList(converted);
        }
        };

        return () => ws.close();
    }, [setGpsList]);
};