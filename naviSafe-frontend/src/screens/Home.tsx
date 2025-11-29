import { KakaoMap } from "../components/KakaoMap";
import { useOutbreakOccur } from "../hooks/useOutbreakOccur";
import type { ShelterInfo } from "../type/Shelter";
import { useShelter } from "../hooks/useShelter";
import { BottomBar } from "../components/BottomBar";

export const Home = () => {
  useOutbreakOccur();

  const shelterMap: ShelterInfo[] = [
    { code: 1, name: "지진대피소" },
    { code: 2, name: "옥외 지진대피소" },
    { code: 3, name: "무더위 쉼터" },
    { code: 4, name: "미세먼지 대피소" },
  ];

  const { shelterType, handleShelterClick } = useShelter(1);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-0 text-center">
      <div className="absolute top-4 left-1/2 transform -translate-x-1/2 z-50 flex w-full max-w-md gap-2 px-2">
        {shelterMap.map((map: ShelterInfo) => (
          <button
            key={map.code}
            onClick={() => handleShelterClick(map.code)}
            className={`flex-1 px-2 py-2 rounded-xl font-medium text-[12px] transition shadow-md ${
              shelterType.shelterCode === map.code
                ? "bg-blue-500 text-white"
                : "bg-[#fff] text-gray-70"
            }`}
          >
            {map.name}
          </button>
        ))}
      </div>
      <KakaoMap />

      <BottomBar />
    </div>
  );
}
