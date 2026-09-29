import { MdArrowBack, MdPlace, MdDirectionsBus, MdSearch, MdCancel} from "react-icons/md";
import { useState, useEffect } from "react";
import axios from "axios";
import { useCurrentLocation } from "../hooks/useCurrentLocation";
import type { SearchResult } from "../type/Search";
import { useSearchResultStore } from "../store/SearchResultStore";
import { useSearchOverlayStore } from "../store/SearchOverlayStore";

type CategoryCode =
    | "MT1"
    | "CS2"
    | "PS3"
    | "SC4"
    | "AC5"
    | "PK6"
    | "OL7"
    | "SW8"
    | "BK9"
    | "CT1"
    | "AG2"
    | "PO3"
    | "AT4"
    | "AD5"
    | "FD6"
    | "CE7"
    | "HP8"
    | "PM9";

interface HistoryItem {
    id: string | number;
    name: string;
    category: CategoryCode;
    address: string;
    lat: number;
    lng: number;
    date: string;
}
    
const categoryInfo: Record<
    CategoryCode,
    {
        name: string;
        icon: React.ReactNode;
    }
> = {
    MT1: {
        name: "대형마트",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    CS2: {
        name: "편의점",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    PS3: {
        name: "어린이집·유치원",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    SC4: {
        name: "학교",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    AC5: {
        name: "학원",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    PK6: {
        name: "주차장",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    OL7: {
        name: "주유소·충전소",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    SW8: {
        name: "지하철역",
        icon: <MdDirectionsBus className="text-green-500" size={18} />,
    },
    BK9: {
        name: "은행",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    CT1: {
        name: "문화시설",
        icon: <MdPlace className="text-purple-500" size={18} />,
    },
    AG2: {
        name: "중개업소",
        icon: <MdPlace className="text-gray-500" size={18} />,
    },
    PO3: {
        name: "공공기관",
        icon: <MdPlace className="text-blue-500" size={18} />,
    },
    AT4: {
        name: "관광명소",
        icon: <MdPlace className="text-yellow-500" size={18} />,
    },
    AD5: {
        name: "숙박",
        icon: <MdPlace className="text-purple-500" size={18} />,
    },
    FD6: {
        name: "음식점",
        icon: <MdPlace className="text-orange-500" size={18} />,
    },
    CE7: {
        name: "카페",
        icon: <MdPlace className="text-amber-700" size={18} />,
    },
    HP8: {
        name: "병원",
        icon: <MdPlace className="text-red-500" size={18} />,
    },
    PM9: {
        name: "약국",
        icon: <MdPlace className="text-green-500" size={18} />,
    },
};

const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);

    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${month}.${day}`;
};

export const AddressSearchOverlay = () => {
    const { isOpen, closeSearch } = useSearchOverlayStore();
    const [recentSearches, setRecentSearches] = useState<SearchResult[]>([]);

    const [keyword, setKeyword] = useState("");

    const { getLocation } = useCurrentLocation();

    const [debouncedKeyword, setDebouncedKeyword] = useState("");
    const { setSelectedResults, setSelectedPlace, setSelectedListItem } = useSearchResultStore();
    const [searchList, setSearchList] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    // 닫힐 때 초기화
    useEffect(() => {
        if (!isOpen) {
        setKeyword("");
        }
    }, [open]);

    useEffect(() => {
        if (isOpen) {
            const stored = localStorage.getItem("recentSearches");
    
            if (stored) {
                try {
                    setRecentSearches(JSON.parse(stored));
                } catch (e) {
                    console.error("최근 검색 기록 불러오기 실패:", e);
                    setRecentSearches([]);
                }
            } else {
                setRecentSearches([]);
            }
        }
    }, [isOpen]);

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
                            id : item.id,
                            category: item.category_group_code,
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

    if (!isOpen) return null;

    // 카테고리 아이콘
    const getCategoryIcon = (category: string) => {
        // category가 없거나 빈 문자열이면 돋보기
        if (!category) {
            return (
                <MdSearch
                    className="text-gray-500"
                    size={18}
                />
            );
        }

        if (category in categoryInfo) {
            return categoryInfo[
                category as CategoryCode
            ].icon;
        }
    
        return (
            <MdSearch
                className="text-gray-500"
                size={18}
            />
        );
    };

    const handleSelect = (item: SearchResult) => {
        const stored = localStorage.getItem("recentSearches");
        const recentSearches: SearchResult[] = stored
            ? JSON.parse(stored)
            : [];

        // 중복 제거
        const filtered = recentSearches.filter(
            (recent) =>
                recent.address !== item.address ||
                recent.lat !== item.lat ||
                recent.lng !== item.lng
        );

        const now = new Date().toISOString();

        // 현재 검색한 시각을 date로 저장
        const historyItem = {
            ...item,
            date: now,
        };

        // 가장 최근 검색을 맨 앞에 추가
        const updated = [
            historyItem,
            ...filtered,
        ].slice(0, 15);

        // localStorage 저장
        localStorage.setItem("recentSearches", JSON.stringify(updated));

        setSelectedPlace(item);
        setSelectedResults(searchList);
        setSelectedListItem(item);
        closeSearch();
    };

    return (
        <div className="fixed inset-0 z-[100] bg-white flex flex-col">
        {/* Header */}
        <div className="flex-shrink-0 w-full max-w-md mx-auto px-2 pt-4">
            <div className="flex items-center rounded-2xl border border-gray-200 bg-white px-2 py-3 shadow-lg">
                <button
                    onClick={closeSearch}
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

                {recentSearches.length > 0 ? (
                    recentSearches.map((item) => (
                        <div
                            key={item.id}
                            onClick={() => setKeyword(item.name)}
                            className="flex items-center gap-3 py-5 px-2 rounded hover:bg-gray-100 cursor-pointer border-b border-gray-200"
                        >
                            {/* 아이콘 */}
                            <div className="flex-shrink-0">
                                {getCategoryIcon(item.category)}
                            </div>

                            {/* 내용 */}
                            <div className="flex-1 min-w-0">
                                <div className="text-sm text-gray-800 truncate text-left">
                                    {item.name}
                                </div>
                            </div>

                            {/* 날짜 */}
                            <div className="text-[11px] text-gray-400 whitespace-nowrap">
                                {formatDate(item.date || "")}
                            </div>
                        </div>
                    ))
                ) : (
                    <div className="p-4 text-sm text-gray-400 text-center">
                        최근 검색어가 없습니다.
                    </div>
                )}
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