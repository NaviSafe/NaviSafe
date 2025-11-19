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
    const mapRef = useRef<any>(null);
    const clustererRef = useRef<any>(null);
    const [isMapLoaded, setIsMapLoaded] = useState<boolean>(false);

    const markersRef = useRef<any[]>([]); // 돌발상황 마커만 넣음

    useEffect(() => {
        const handleWindowResize = () => {
            setWindowHeightSize(window.innerHeight);
        };
        window.addEventListener("resize", handleWindowResize);
        return () => window.removeEventListener("resize", handleWindowResize);
    }, []);

    useEffect(() => {
        const script = document.createElement("script");
        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${
            import.meta.env.VITE_KAKAO_MAP_JAVASCRIPT_API_KEY
        }&autoload=false&libraries=clusterer`;
        script.async = true;

        script.onload = () => {
            window.kakao.maps.load(() => {
                const container = document.getElementById("map");
                const options = {
                    center: new window.kakao.maps.LatLng(37.5642135, 127.0016985),
                    level: 9,
                };

                mapRef.current = new window.kakao.maps.Map(container, options);

                // 대피소 클러스터러 생성
                clustererRef.current = new window.kakao.maps.MarkerClusterer({
                    map: mapRef.current,
                    averageCenter: true,
                    minLevel: 7, // 확대 시 클러스터 해제
                });

                setIsMapLoaded(true);
            });
        };

        document.head.appendChild(script);
    }, []);

    // 마커 업데이트
    useEffect(() => {
        if (!isMapLoaded || !mapRef.current) return;

        // 기존 돌발 마커 제거
        markersRef.current.forEach((m) => m.setMap(null));
        markersRef.current = [];

        // 클러스터 초기화
        if (clustererRef.current) {
            clustererRef.current.clear();
        }

        const imageSize = new window.kakao.maps.Size(35,35);
        const outboundOccurMarkerSrc = 'public/outboundOccur.png';
        const shelterMarkerSrc = 'public/shelterIcon.png';

        const outboundOccurMarkerImage = new window.kakao.maps.MarkerImage(outboundOccurMarkerSrc , imageSize);
        const shelterMarkerImage = new window.kakao.maps.MarkerImage(shelterMarkerSrc , imageSize);

        /* 
         * 돌발상황 마커 (클러스터링 X)
         */
        gpsList.forEach((item) => {
            const marker = new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(item.y, item.x),
                map: mapRef.current, // 바로 지도에 표시
                image: outboundOccurMarkerImage,
            });

            markersRef.current.push(marker);
        });

        /* 
         * 2) 대피소 마커 (클러스터링 O)
         */
        const shelterMarkers = shelterType.shelterGpsList.map((item) => {
            return new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(item.lat, item.lot),
                image: shelterMarkerImage,
            });
        });

        // 클러스터러에 넣기
        clustererRef.current.addMarkers(shelterMarkers);

    }, [gpsList, shelterType, isMapLoaded]);

    return (
        <div
            id="map"
            className="w-full rounded-2xl shadow-md border border-gray-200"
            style={{ height: `${windowHeightSize}px` }}
        />
    );
};