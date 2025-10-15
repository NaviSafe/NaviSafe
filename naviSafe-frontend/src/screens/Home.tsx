import { useEffect } from "react";
import { KakaoMap } from "../components/KakaoMap";
import { useGpsStore } from "../store/gpsStore";


export const Home = () => {
  const setGpsList = useGpsStore((state) => state.setGpsList);

  useEffect(() => {
    const ws = new WebSocket("ws://localhost:8080/ws/gps");

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if(message.type === "gps_batch"){
        setGpsList(message.data);
      }
    };

    return () => ws.close();
  }, [setGpsList])

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-4 text-center">
      <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-blue-500 mb-4">
        TailwindCSS 테스트 성공! 🎉
      </h1>
      <p className="text-gray-600 text-sm sm:text-base md:text-lg">
        반응형으로 잘 보이죠? 📱💻
      </p>

      <KakaoMap />
    </div>
  );
}
