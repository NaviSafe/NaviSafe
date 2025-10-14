import { useEffect, useState } from "react";

declare global {
  interface Window {
    kakao: any;
  }
}

export const KakaoMap = () => {
    const [windowHeightSize, setWindowHeightSize] = useState<number>(window.innerHeight);
    
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
            center: new window.kakao.maps.LatLng(37.5665, 126.9780), // 서울 좌표
            level: 8,
            };
            const map = new window.kakao.maps.Map(container, options);

            // ✅ 예시 마커
            const marker = new window.kakao.maps.Marker({
            position: new window.kakao.maps.LatLng(37.5665, 126.9780),
            map: map,
            });

            const info = new window.kakao.maps.InfoWindow({
            content: '<div style="padding:6px;text-align:center;">서울 시청 📍</div>',
            });
            info.open(map, marker);
        });
    };

    document.head.appendChild(script);
  }, [windowHeightSize]);

  return (
    <div
        id="map"
        className="w-full rounded-2xl shadow-md border border-gray-200"
        style={{ height: `${windowHeightSize}px` }} // 여기서 state를 px로 적용
        />
    );
};