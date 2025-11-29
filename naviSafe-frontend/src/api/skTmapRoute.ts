import axios from "axios";

export const getPedestrianRoute = async (
        start: { lat: number; lon: number },
        end: { lat: number; lon: number }
    ) => {
    try {
        const res = await axios.post(
            "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1",
            {
                startX: start.lon,
                startY: start.lat,
                endX: end.lon,
                endY: end.lat,
                startName: "시작위치",
                endName: "도착위치",
                searchOption: 10 // 최단거리
            },
            {
                headers : {
                    accept: 'application/json',
                    appKey: `${import.meta.env.VITE_TMAP_API_KEY}`,
                    'content-type': 'application/json',
                }
            }
        )
    
        const data = await res.data;
        const coords: { lat: number; lon: number }[] = [];
    
        data.features?.forEach((feature: any) => {
            if (feature.geometry.type === "LineString") {
            feature.geometry.coordinates.forEach(([lon, lat]: number[]) => {
                coords.push({ lat, lon });
            });
            }
        });
    
        return coords;
    } catch (err) {
        console.error("Tmap 도보 API 오류:", err);
        return [];
    }
};
