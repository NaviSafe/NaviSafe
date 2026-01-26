// utils/handleEmergencyPush.ts
import type { ShelterGps } from "../store/shelterStore";
import { getDistance } from "./common/haversineDistance";
import { getPedestrianRoute } from "../api/skTmapRoute";

export async function handleEmergencyPush({
    channel,
    pushType,
    getLocation,
    fetchShelterById,
    setRouteCoords,
    setShelterType,
}: any) {
    if (channel !== "EMERGENCY_ALERT") return;

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

    const index = PUSH_TYPE_TO_INDEX[pushType];
    if (index === undefined) return;

    const shelterType = await fetchShelterById(index);
    if (!shelterType) return;

    const currentLocation = await getLocation();
    if (!currentLocation) return;

    let nearest: ShelterGps | null = null;
    let min = Infinity;

    for (const shelter of shelterType.shelterGpsList) {
        const d = getDistance(
            currentLocation.lat,
            currentLocation.lon,
            shelter.lat,
            shelter.lot
        );
        if (d < min) {
            min = d;
            nearest = shelter;
        }
    }

    if (!nearest) return;

    const coords = await getPedestrianRoute(
        currentLocation,
        { lat: nearest.lat, lon: nearest.lot }
    );

    if (!coords?.length) return;

    setRouteCoords(coords);
    setShelterType({
        shelterCode: index,
        shelterCodeName: "",
        shelterGpsList: [nearest],
    });
}
