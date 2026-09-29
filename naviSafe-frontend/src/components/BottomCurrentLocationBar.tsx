import { FaLocationCrosshairs } from "react-icons/fa6";
import { useMapStore } from "../store/mapStore";

export const BottomCurrentLocationBar = () => {
    const { requestMoveToCurrentLocation } = useMapStore();

    return (
        <div className="absolute bottom-0 left-0 w-full p-4 z-50 pointer-events-none">
            <div className="flex justify-end">
                <button
                    className="
                        pointer-events-auto
                        w-14 h-14
                        rounded-full
                        bg-white
                        shadow-lg
                        border border-gray-200
                        flex items-center justify-center
                        active:scale-95
                        transition
                    "
                    onClick={requestMoveToCurrentLocation}
                >
                    <FaLocationCrosshairs
                        size={24}
                        className="text-blue-500"
                    />
                </button>
            </div>
        </div>
    );
};