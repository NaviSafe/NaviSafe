import { useEffect, useState, useRef } from "react";
import { useGpsStore } from "../store/gpsStore";
import { useShelterTypeState } from "../store/shelterStore";
import { useSelectedShelter } from "../store/selectedShelterStore";

declare global {
    interface Window {
        kakao: any;
    }
}

export const KakaoMap = () => {
    const gpsList = useGpsStore((state) => state.gpsList);
    const shelterType = useShelterTypeState((state) => state.shelterType);
    const { setSelectedShelter } = useSelectedShelter();

    const [windowHeightSize, setWindowHeightSize] = useState<number>(window.innerHeight);
    const mapRef = useRef<any>(null);
    const clustererRef = useRef<any>(null);
    const [isMapLoaded, setIsMapLoaded] = useState<boolean>(false);

    const markersRef = useRef<any[]>([]); // 돌발상황 마커만 넣음
    const overlayRef = useRef<any>(null);

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
            const marker =  new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(item.lat, item.lot),
                image: shelterMarkerImage,
            });

            window.kakao.maps.event.addListener(marker, "click", () => {
                if (overlayRef.current) overlayRef.current.setMap(null);

                const content = `
                    <div class="overlay-box"
                        style="padding:8px 12px; 
                        background:white; 
                        border-radius:8px; 
                        box-shadow: 0 2px 6px rgba(0,0,0,0.3); 
                        border:1px solid #ddd;
                        pointer-events: auto;
                        ">
                        <div style="font-size:14px; font-weight:600; margin-bottom:4px;">
                            ${item.shelterName}
                        </div>
                        <div style="font-size:12px;">${item.shelterAddress}</div>
                    </div>
                    `;

                const overlay = new window.kakao.maps.CustomOverlay({
                    position: new window.kakao.maps.LatLng(item.lat, item.lot),
                    content: content,
                    yAnchor: 1.2,
                });

                overlay.setMap(mapRef.current);
                overlayRef.current = overlay;

                setSelectedShelter({
                    code: item.shelterCode,
                    name: item.shelterName,
                    lat : item.lat,
                    lot : item.lot
                });
            });

            return marker;
        });

        // 클러스터러에 넣기
        clustererRef.current.addMarkers(shelterMarkers);

    }, [gpsList, shelterType, isMapLoaded]);

    // 지도 클릭 시 오버레이 제거
    useEffect(() => {
        if (!isMapLoaded || !mapRef.current) return;
    
        const map = mapRef.current;
        const handleClick = () => {
            if (overlayRef.current) {
                overlayRef.current.setMap(null);
                overlayRef.current = null;
                setSelectedShelter(null);
            }
        };
    
        window.kakao.maps.event.addListener(map, "click", handleClick);
    
        return () => {
            window.kakao.maps.event.removeListener(map, "click", handleClick);
        };
    }, [isMapLoaded]);

    // 대피소 상태 변경 시 오버레이 제거
    useEffect(() => {
        if (overlayRef.current) {
            overlayRef.current.setMap(null);
            overlayRef.current = null;
            setSelectedShelter(null);
        }
    }, [shelterType]);

    return (
        <div
            id="map"
            className="w-full rounded-2xl shadow-md border border-gray-200"
            style={{ height: `${windowHeightSize}px` }}
        />
    );
};