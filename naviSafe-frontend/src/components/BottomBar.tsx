import { getPedestrianRoute } from "../api/skTmapRoute";
import { useCurrentLocation } from "../hooks/useCurrentLocation";
import { useRouteStore } from "../store/routeStore";
import { useSelectedShelter } from "../store/selectedShelterStore";

export const BottomBar = () => {
    const { selectedShelter } = useSelectedShelter();
    const { getLocation } = useCurrentLocation();
    const {setRouteCoords} = useRouteStore();

    if (!selectedShelter) return null;

    const fontSize =
        selectedShelter.name.length > 10
        ? "12px"
        : selectedShelter.name.length > 5
        ? "14px"
        : "16px";

    const handleNavigate = async () => {
        const currentLocation = await getLocation();
        if(!currentLocation){
            alert("위치 정보를 불러올 수 없습니다.");
            return;
        }

        // actual my current coords
        // const startCoords = { lat : currentLocation.lat, lon : currentLocation.lon};
        // for testing = 서울시청
        const startCoords = { lat : 37.5663, lon : 126.9779};
        
        const destination = { lat : selectedShelter.lat, lon : selectedShelter.lot};
        
        try{
            const coords = await getPedestrianRoute(startCoords, destination);

            if (!coords || coords.length === 0) {
                alert("도보 경로 좌표가 없습니다.");
                return;
            }

            setRouteCoords(coords);
        } catch (err){
            alert("도보 경로를 불러올 수 없습니다.");
        }
    };

    return (
        <div className="absolute bottom-0 left-0 w-full bg-white p-4 flex justify-between items-center shadow-lg z-50">
        <span className="font-medium" style={{ fontSize }}>
            {selectedShelter.name}
        </span>

        <button
            className="bg-blue-500 text-white px-4 py-2 rounded-lg"
            onClick={handleNavigate}
        >
            여기로 이동
        </button>
        </div>
    );
};