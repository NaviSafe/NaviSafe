import { useRouteStore } from "../store/routeStore";

export const DistanceCard = () => {
    const { distance } = useRouteStore();

    if (!distance || distance <= 0) return null;

    const distanceText =
        distance < 1000
            ? `${Math.round(distance)} m`
            : `${(distance / 1000).toFixed(2)} km`;

    return (
        <div className="fixed bottom-24 right-4 w-[20%] min-w-[110px] max-w-[250px] z-50">
            <div className="bg-white rounded-2xl shadow-lg px-4 py-3 flex flex-col items-center text-center">
                <div className="text-sm text-gray-500 mb-1">
                총 거리
                </div>
                <div className="text-xl font-bold text-blue-600">
                {distanceText}
                </div>
            </div>
        </div>
    );
};