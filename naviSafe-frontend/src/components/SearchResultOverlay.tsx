import { useRef } from "react";
import { MdArrowBack } from "react-icons/md";
import { useSearchResultStore } from "../store/SearchResultStore";
import { useLocationStore } from "../store/locationStore";
import type { SearchResult } from "../type/Search";
import { useNavigate } from "react-router-dom";
import { useSearchOverlayStore } from "../store/SearchOverlayStore";


interface Props {
    onClose: () => void;
}

export const SearchResultOverlay = ({ onClose }: Props) => {

    const {
        selectedPlace,
        selectedResults,
        selectedListItem,
        setSelectedPlace,
        setSelectedResults,
        setSelectedListItem
    } = useSearchResultStore();

    const sheetRef = useRef<HTMLDivElement>(null);
    const itemRefs = useRef<(HTMLDivElement | null)[]>([]);
    const {setSourceAddress, setDestAddress } = useLocationStore();
    const {openSearch} = useSearchOverlayStore();
    const navigate = useNavigate();

    if (!selectedPlace) return null;

    const openAddressSearch = () => {
        openSearch();
        setSelectedPlace(null);
        setSelectedResults([]);
    }

    const handleScroll = () => {
        const sheet = sheetRef.current;
    
        if (!sheet) return;
    
        const sheetTop = sheet.scrollTop;
    
        let closestIndex = 0;
        let minDistance = Infinity;
    
        itemRefs.current.forEach((item, idx) => {
            if (!item) return;
    
            const itemTop =
                item.offsetTop;
    
            const distance = Math.abs(
                sheetTop - itemTop
            );
    
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = idx;
            }
        });
    
        setSelectedListItem(selectedResults[closestIndex]);
    };

    const handleClickItem = (item: SearchResult) => {
        const isSelected = item.name === selectedListItem?.name;
    
        if (isSelected) {
            return;
        }
    
        // 다른 항목 클릭 → 선택 변경
        setSelectedListItem(item);
    };

    return (
        <div className="fixed inset-0 z-[200] pointer-events-none">


            {/* Header */}
            <div className="absolute top-0 left-0 right-0 z-30 w-full max-w-md mx-auto px-2 pt-3 pb-3 pointer-events-auto">
                <div className="flex items-center rounded-2xl border border-gray-200 bg-white px-2 py-3 shadow-lg">
                    <button
                        onClick={onClose}
                        className="mr-2 text-gray-500 hover:text-black"
                    >
                        <MdArrowBack size={22} />
                    </button>
    
                    <input
                        readOnly
                        type="text"
                        value={selectedPlace.name}
                        placeholder="주소를 검색하세요"
                        className="flex-1 text-sm outline-none cursor-pointer"
                        onClick={openAddressSearch}
                    />
                </div>
            </div>



            {/* Map */}
            <div className="
                absolute inset-0
            ">
            </div>


            {/* Bottom Sheet */}
            <div
                ref={sheetRef}
                onScroll={handleScroll}
                className="absolute bottom-0 left-0 right-0 z-10 bg-white rounded-xl shadow-xl max-h-[35%] overflow-y-auto pointer-events-auto"
            >
                <div className="p-0">
                    <div className="divide-y">
                        {selectedResults.map((item, idx) => {
                            const isSelected = item.name === selectedListItem?.name;

                            return (
                                <div
                                    ref={(el) => {
                                        itemRefs.current[idx] = el;
                                    }}
                                    key={idx}
                                    onClick={() => handleClickItem(item)}
                                    className={`py-4 px-3 text-left rounded-xl transition
                                        ${
                                            isSelected
                                            ? "bg-blue-50 border"
                                            : "hover:bg-gray-50"
                                        }
                                    `}
                                >
                                    <div className="flex items-center justify-between gap-3">
                                        <div className="min-w-0 flex-1">
                                            <div className={`
                                                text-sm
                                                font-medium
                                                ${
                                                    isSelected
                                                    ? "text-blue-600"
                                                    : "text-gray-800"
                                                }
                                            `}>
                                                {item.name}
                                            </div>

                                            <div className="
                                                text-xs
                                                text-gray-500
                                                mt-1
                                            ">
                                                {item.address}
                                            </div>
                                        </div>


                                        {/* 선택된 항목에만 버튼 */}
                                        {isSelected && (
                                            <div className="flex gap-1 shrink-0">
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setSourceAddress({
                                                            address : selectedListItem.address, 
                                                            latitude : selectedListItem.lat,
                                                            longitude : selectedListItem.lng
                                                        })
                                                        navigate("/src-dest")
                                                        
                                                    }}
                                                    className="
                                                        px-3 py-2
                                                        rounded-2xl
                                                        bg-blue-500
                                                        text-white
                                                        text-xs
                                                        font-semibold
                                                    "
                                                >
                                                    출발
                                                </button>

                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setDestAddress({
                                                            address : selectedListItem.address, 
                                                            latitude : selectedListItem.lat,
                                                            longitude : selectedListItem.lng
                                                        })
                                                        navigate("/src-dest")
                                                    }}
                                                    className="
                                                        px-3 py-2
                                                        rounded-2xl
                                                        bg-blue-500
                                                        text-white
                                                        text-xs
                                                        font-semibold
                                                    "
                                                >
                                                    도착
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}

                    </div>
                </div>
            </div>
        </div>
    );
};