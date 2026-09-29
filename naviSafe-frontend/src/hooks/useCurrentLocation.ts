import { useState } from "react";
import { useLocationStore } from "../store/myLocationStore";

interface Position {
    lat: number;
    lon: number;
}

interface UseCurrentLocationReturn {
    location: Position | null;
    loading: boolean;
    error: string | null;
    getLocation: () => Promise<Position>;
}

export const useCurrentLocation = () : UseCurrentLocationReturn => {
    const location = useLocationStore((s) => s.location);
    const setLocation = useLocationStore((s) => s.setLocation);
    
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getLocation = (): Promise<Position> => {
        setLoading(true);
        setError(null);

        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                setError("현재 브라우저에서는 위치 정보를 지원하지 않습니다.");
                setLoading(false);
                reject(new Error("Geolocation not supported"));
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const posObj = { lat: pos.coords.latitude, lon: pos.coords.longitude };
                    setLocation(posObj);
                    setLoading(false);
                    resolve(posObj);
                },
                (err) => {
                    setError("위치 정보를 불러올 수 없습니다.");
                    setLoading(false);
                    reject(err);
                },
                { enableHighAccuracy: true, timeout: 5000 }
            );
        });
    };

    return { location, loading, error, getLocation };
};
