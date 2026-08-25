import { KakaoMap } from "../components/KakaoMap";
import { useOutbreakOccur } from "../hooks/useOutbreakOccur";
import type { ShelterInfo } from "../type/Shelter";
import { useShelter } from "../hooks/useShelter";
import { BottomCurrentLocationBar } from "../components/BottomCurrentLocationBar";
import { MdWarningAmber } from "react-icons/md";
import { useState } from "react";
import { DisasterLogList } from "../components/DisasterLogList";
import { AddressSearchOverlay } from "../components/AddressSearchOverlay";
import { useSearchResultStore } from "../store/SearchResultStore";
import { SearchResultOverlay } from "../components/SearchResultOverlay";
import { useSearchOverlayStore } from "../store/SearchOverlayStore";


export const Home = () => {
  useOutbreakOccur();
  
  const shelterMap: ShelterInfo[] = [
    { code: 1, name: "지진대피소" },
    { code: 2, name: "한파대피소" },
    { code: 3, name: "무더위 쉼터" },
    { code: 4, name: "미세먼지 대피소" },
  ];

  const { shelterType, handleShelterClick } = useShelter(0);
  const [openDisasterLog, setOpenDisasterLog] = useState<boolean>(false);
  const {openSearch} = useSearchOverlayStore();
  const { selectedPlace } = useSearchResultStore();

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-0 text-center">
      <div className="absolute top-4 left-1/2 z-50 w-full max-w-md -translate-x-1/2 px-2 flex flex-col gap-2">
        <div
          role="button"
          tabIndex={0}
          onClick={() => {
            openSearch();
          }}
          className="flex w-full cursor-text items-center rounded-2xl border border-gray-200 bg-white px-2 py-3 shadow-lg"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="mr-2 h-5 w-5 text-gray-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M21 21l-4.35-4.35m1.85-5.15a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <span className="text-sm text-gray-400 select-none">
            주소를 검색하세요
          </span>
        </div>
        <div className="flex gap-2">
          {!selectedPlace && shelterMap.map((map: ShelterInfo) => (
            <button
              key={map.code}
              onClick={() => handleShelterClick(map.code)}
              className={`flex-1 px-2 py-1 rounded-xl text-[11px] transition shadow-md whitespace-normal break-keep ${
                shelterType.shelterCode === map.code
                  ? "bg-blue-500 text-white"
                  : "bg-[#fff] text-gray-70"
              }`}
            >
              {map.name}
            </button>
          ))}
        </div>
        {!selectedPlace && (
            <div className="flex justify-end">
              <button
                className="
                  w-12 h-12
                  rounded-full
                  bg-white
                  shadow-lg
                  border border-gray-200
                  flex items-center justify-center
                  hover:bg-gray-50
                  active:scale-95
                  transition
                "
                onClick={() => setOpenDisasterLog(true)}
              >
              <MdWarningAmber size={28} className="text-red-500" />
              </button>
            </div>
          )
        }
      </div>
      {!selectedPlace && (
        <DisasterLogList
          open={openDisasterLog}
          onClose={() => setOpenDisasterLog(false)}
      />)
      }
      <AddressSearchOverlay/>

      {selectedPlace && (
        <SearchResultOverlay/>
      )}
      <KakaoMap />
      <BottomCurrentLocationBar/>
    </div>
  );
}
