import { useEffect, useState, useRef } from "react";
import { useGpsStore } from "../store/gpsStore";
import { useShelterTypeState } from "../store/shelterStore";

declare global {
    interface Window {
        kakao: any;
    }
}

export const KakaoMap = () => {
    const gpsList = useGpsStore((state) => state.gpsList);
    const shelterType = useShelterTypeState((state) => state.shelterType);

    const [windowHeightSize, setWindowHeightSize] = useState<number>(window.innerHeight);
    const mapRef = useRef<any>(null); // 지도 참조
    const markersRef = useRef<any[]>([]); // 마커 배열
    const [isMapLoaded, setIsMapLoaded] = useState<boolean>(false);

    useEffect(() => {
        const handleWindowResize = () => {
            setWindowHeightSize(window.innerHeight);
        };

        window.addEventListener("resize", handleWindowResize);

        return () => {
            window.removeEventListener("resize", handleWindowResize);
        };
    }, []);

    useEffect(() => {
        const script = document.createElement("script");
        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${
            import.meta.env.VITE_KAKAO_MAP_JAVASCRIPT_API_KEY
        }&autoload=false`;
        script.async = true;

        script.onload = () => {
            window.kakao.maps.load(() => {
                const container = document.getElementById("map");
                const options = {
                    center: new window.kakao.maps.LatLng(37.5642135, 127.0016985),
                    level: 9,
                };
                mapRef.current = new window.kakao.maps.Map(container, options);
                setIsMapLoaded(true);
            });
        };

        document.head.appendChild(script);
    }, []);

    // gpsList 변경 시 마커 업데이트
    useEffect(() => {
        if (!isMapLoaded || !mapRef.current) return;

        // 기존 마커 제거
        markersRef.current.forEach(marker => marker.setMap(null));
        markersRef.current = [];

        // 새 마커 추가
        gpsList.forEach(item => {
            const marker = new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(item.y, item.x),
                map: mapRef.current,
            });
            markersRef.current.push(marker);
        });


        // shelterGpsList 마커 추가
        shelterType.shelterGpsList.forEach(item => {
            const marker = new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(item.lat, item.lot),
                map: mapRef.current,
                image: new window.kakao.maps.MarkerImage(
                    "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png",
                    new window.kakao.maps.Size(24, 35)
                )
            });
            markersRef.current.push(marker);
        });
    }, [gpsList, shelterType, isMapLoaded]);


    return (
    <div
        id="map"
        className="w-full rounded-2xl shadow-md border border-gray-200"
        style={{ height: `${windowHeightSize}px` }} // 여기서 state를 px로 적용
        />
    );
};