// hooks/usePushMessage.ts
import { useEffect } from "react";
import { useShelter } from "./useShelter";
import { useShelterTypeState } from "../store/shelterStore";
import type { ShelterGps } from "../store/shelterStore";
import { useCurrentLocation } from "./useCurrentLocation";
import { getDistance } from "../utils/common/haversineDistance";
import { useRouteStore } from "../store/routeStore";

import { getPedestrianRoute } from "../api/skTmapRoute";

const PUSH_TYPE_TO_INDEX: Record<string, number> = {
    지진: 1,
    풍량: 2,
    한파: 2,
    강풍: 2,
    대설: 2,
    폭염: 3,
    미세먼지: 4,
    황사: 4,
};

export function usePushMessage() {
    const { getLocation } = useCurrentLocation();
    const { fetchShelterById } = useShelter(0);
    const { setShelterType } = useShelterTypeState();
    const { setRouteCoords } = useRouteStore();

    useEffect(() => {
        if (!("serviceWorker" in navigator)) return;

        const handler = async (event: MessageEvent<any>) => {
            const { channel, pushType } = event.data ?? {};

            if (channel !== "EMERGENCY_ALERT" || !pushType) return;
        
            const index = PUSH_TYPE_TO_INDEX[pushType];
            if (index === undefined) {
                return;
            }
            const shelterType = await fetchShelterById(index);

            if(shelterType === null){
                return;
            }

            // 내 위치랑 가장 가까운 대피소정보 가져오기
            const currentLocation = await getLocation();
            if(!currentLocation){
                alert("위치 정보를 불러올 수 없습니다.");
                return;
            }
            
            const shelterGpsList : ShelterGps[] = shelterType.shelterGpsList;

            let minDistance = Infinity;
            let nearestShelter: ShelterGps | null = null;

            for (const shelter of shelterGpsList) {
                const distance = getDistance(
                    currentLocation.lat,
                    currentLocation.lon,
                    shelter.lat,
                    shelter.lot
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestShelter = shelter;
                }
            }

            if (!nearestShelter) {
                console.log("가장 가까운 경로가 존재하지 않습니다.");
                return;
            }

            try{
                const coords = await getPedestrianRoute(currentLocation, {lat : nearestShelter.lat, lon: nearestShelter.lot});
    
                if (!coords || coords.length === 0) {
                    alert("도보 경로 좌표가 없습니다.");
                    return;
                }
    
                setRouteCoords(coords);
                setShelterType(
                    {
                        shelterCode: index,
                        shelterCodeName: "",
                        shelterGpsList: [nearestShelter]
                    }
                );
            } catch (err){
                alert("도보 경로를 불러올 수 없습니다.");
            }
        };

        navigator.serviceWorker.addEventListener("message", handler);

        return () => {
            navigator.serviceWorker.removeEventListener("message", handler);
        };
    }, []);
}
