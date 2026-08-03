import { MdArrowBack, MdPlace, MdDirectionsBus, MdSearch, MdCancel} from "react-icons/md";
import { useState, useEffect } from "react";
import axios from "axios";
import { useCurrentLocation } from "../hooks/useCurrentLocation";
import type { SearchResult } from "../type/Search";
import { useSearchResultStore } from "../store/SearchResultStore";

interface AddressSearchOverlayProps {
    open: boolean;
    onClose: () => void;
}

type HistoryType = "PLACE" | "STOP" | "SEARCH";

interface HistoryItem {
    id: number;
    name: string;
    type: HistoryType;
    date: string;
}

const mockRecentKeywords : HistoryItem[]= [
    {
        id: 1,
        name: "서울특별시 중구 서울역",
        type: "PLACE",
        date: "2026-07-06",
    },
    {
        id: 2,
        name: "강남역 2호선",
        type: "STOP",
        date: "2026-07-05",
    },
    {
        id: 3,
        name: "부산광역시 해운대구 해운대 해수욕장",
        type: "PLACE",
        date: "2026-07-05",
    },
    {
        id: 4,
        name: "인천국제공항",
        type: "PLACE",
        date: "2026-07-04",
    },
    {
        id: 5,
        name: "판교역 테크노밸리",
        type: "SEARCH",
        date: "2026-07-04",
    },
    {
        id: 6,
        name: "대구 동성로 중앙로역",
        type: "PLACE",
        date: "2026-07-03",
    },
    {
        id: 7,
        name: "광주 송정역",
        type: "STOP",
        date: "2026-07-03",
    },
    {
        id: 8,
        name: "제주국제공항 국내선",
        type: "PLACE",
        date: "2026-07-02",
    },
    {
        id: 9,
        name: "울산 남구 삼산동 현대백화점",
        type: "PLACE",
        date: "2026-07-02",
    },
    {
        id: 10,
        name: "수원역 AK플라자 쇼핑몰",
        type: "PLACE",
        date: "2026-07-01",
    },
    {
        id: 11,
        name: "대전 정부청사역",
        type: "STOP",
        date: "2026-07-01",
    },
    {
        id: 12,
        name: "송도 국제도시 센트럴파크",
        type: "PLACE",
        date: "2026-06-30",
    },
    {
        id: 13,
        name: "잠실역 롯데월드타워",
        type: "STOP",
        date: "2026-06-30",
    },
    {
        id: 14,
        name: "홍대입구역 9번 출구",
        type: "SEARCH",
        date: "2026-06-29",
    },
    {
        id: 15,
        name: "노량진 수산시장",
        type: "STOP",
        date: "2026-06-29",
    },
];

const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);

    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${month}.${day}`;
};

export const AddressSearchOverlay = ({
    open,
    onClose,
    }: AddressSearchOverlayProps) => {
    const [keyword, setKeyword] = useState("");

    const { getLocation } = useCurrentLocation();

    const [debouncedKeyword, setDebouncedKeyword] = useState("");
    const { setSelectedResults, setSelectedPlace, setSelectedListItem } = useSearchResultStore();
    const [searchList, setSearchList] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    // 닫힐 때 초기화
    useEffect(() => {
        if (!open) {
        setKeyword("");
        }
    }, [open]);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedKeyword(keyword);
        }, 400);
    
        return () => clearTimeout(timer);
    }, [keyword]);

    useEffect(() => {
        if (debouncedKeyword.trim().length < 2) {
            setSearchList([]);
            return;
        }
    
        const fetchAddress = async () => {
            try {
                setLoading(true);
    
                const currentLocation = await getLocation();
    
                const placeRes = await axios.post(
                    `${import.meta.env.VITE_API_BASE_URL}/api/address/search-place`,
                    {
                        lon: currentLocation.lon,
                        lat: currentLocation.lat,
                        keyword: debouncedKeyword,
                    }
                );
    
                if (placeRes.data?.documents?.length > 0) {
                    setSearchList(
                        placeRes.data.documents.map((item: any) => ({
                            type: "place",
                            name: item.place_name,
                            address: item.road_address_name || item.address_name,
                            lat: Number(item.y),
                            lng: Number(item.x),
                        }))
                    );
                    return;
                }
            } catch (e) {
                console.error(e);
                setSearchList([]);
            } finally {
                setLoading(false);
            }
        };
    
        fetchAddress();
    }, [debouncedKeyword]);

    if (!open) return null;

    const getTypeIcon = (type: HistoryType) => {
        switch (type) {
        case "PLACE":
            return <MdPlace className="text-blue-500" size={18} />;
        case "STOP":
            return <MdDirectionsBus className="text-green-500" size={18} />;
        case "SEARCH":
            return <MdSearch className="text-gray-500" size={18} />;
        }
    };

    const handleSelect = (item: SearchResult) => {
        setSelectedPlace(item);
        setSelectedResults(searchList);
        setSelectedListItem(item);
        onClose();
    };

    return (
        <div className="fixed inset-0 z-[100] bg-white flex flex-col">
        {/* Header */}
        <div className="flex-shrink-0 w-full max-w-md mx-auto px-2 pt-4">
            <div className="flex items-center rounded-2xl border border-gray-200 bg-white px-2 py-3 shadow-lg">
                <button
                    onClick={onClose}
                    className="mr-2 text-gray-500 hover:text-black"
                >
                    <MdArrowBack size={22} />
                </button>

                <input
                    autoFocus
                    type="text"
                    value={keyword}
                    placeholder="주소를 검색하세요"
                    className="flex-1 text-sm outline-none"
                    onChange={(e) => setKeyword(e.target.value)}
                />

                {keyword && (
                    <button
                        onClick={() => setKeyword("")}
                        className="ml-2 text-gray-400 hover:text-gray-600"
                    >
                        <MdCancel size={20} />
                    </button>
                )}
            </div>
        </div>

        {/* 최근 검색 */}
        <div className="flex-1 w-full max-w-md mx-auto px-2 mt-3 overflow-y-auto">
            {!keyword && (
            <>
                <div className="text-xs text-gray-700 mb-2 text-left">
                    <span className="inline-block px-2 py-1 border border-gray-400 rounded-full bg-white text-gray-800">
                        최근
                    </span>
                </div>

                {mockRecentKeywords.map((item) => (
                <div
                    key={item.id}
                    onClick={() => setKeyword(item.name)}
                    className="flex items-center gap-3 py-5 px-2 rounded hover:bg-gray-100 cursor-pointer border-b border-gray-200"
                >
                    {/* 아이콘 */}
                    <div className="flex-shrink-0">
                    {getTypeIcon(item.type)}
                    </div>

                    {/* 내용 */}
                    <div className="flex-1 min-w-0">
                        <div className="text-sm text-gray-800 truncate text-left">
                            {item.name}
                        </div>
                    </div>

                    {/* 날짜 */}
                    <div className="text-[11px] text-gray-400 whitespace-nowrap">
                    {formatDate(item.date)}
                    </div>
                </div>
                ))}
            </>
            )}

            {keyword && (
                <>
                    {loading ? (
                        <div className="p-4 text-sm text-gray-500">
                            검색 중...
                        </div>
                    ) : searchList.length > 0 ? (
                        searchList.map((item, idx) => (
                            <div
                                key={idx}
                                className="px-2 py-5 border-b border-gray-200 hover:bg-gray-100 cursor-pointer"
                                onClick={() => handleSelect(item)}
                            >
                                {item.type === "place" ? (
                                    <>
                                        <div className="text-sm text-left">
                                            {item.name}
                                        </div>
                                        <div className="text-xs text-gray-500 text-left">
                                            {item.address}
                                        </div>
                                    </>
                                ) : (
                                    <div className="text-sm text-left">
                                        {item.address}
                                    </div>
                                )}
                            </div>
                        ))
                    ) : (
                        <div className="p-4 text-sm text-gray-400">
                            검색 결과가 없습니다.
                        </div>
                    )}
                </>
            )}
        </div>
        </div>
    );
};