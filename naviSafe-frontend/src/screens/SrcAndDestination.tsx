import { useEffect, useState } from "react";
import axios from "axios";
import useSWRMutation from "swr/mutation";

import { useNavigate } from "react-router-dom";
import { useLocationStore } from "../store/locationStore";
import { useRouteStore } from "../store/routeStore";
import type { SearchResult } from "../type/Search";
import { useCurrentLocation } from "../hooks/useCurrentLocation";

type ActiveType = "source" | "dest" | null;

export const SrcAndDestination = () => {
    const navigate = useNavigate();
    const { sourceAddress, destAddress, setSourceAddress, setDestAddress } =
        useLocationStore();
    const {setRoute} = useRouteStore();
    const { getLocation } = useCurrentLocation();

    const [activeType, setActiveType] = useState<ActiveType>(null);
    const [inputText, setInputText] = useState("");
    const [debouncedText, setDebouncedText] = useState("");
    const [searchList, setSearchList] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedText(inputText);
        }, 400); // 400ms 후 실행
    
        return () => clearTimeout(timer); // 이전 타이머 취소
    }, [inputText]);

    useEffect(() => {
        if (inputText.length < 2) {
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
                    lon : currentLocation.lon,
                    lat : currentLocation.lat,
                    keyword : inputText
                },
            )

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

            const addrRes = await axios.get(
                `${import.meta.env.VITE_API_BASE_URL}/api/address/search-juso`,
                {
                    params: { keyword : inputText },
                }
            );
            const jusoList = addrRes.data?.results?.juso ?? [];
            setSearchList(
                jusoList.map((item: any) => ({
                    type: "address",
                    address: item.roadAddr,
                }))
            );
        } catch (e) {
            console.error("주소 검색 실패", e);
            setSearchList([]);
        } finally {
            setLoading(false);
        }
        };

        fetchAddress();
    }, [debouncedText]);

    const getCoordByAddress = (address: string) => {
        return new Promise<{ lat: number; lng: number }>((resolve, reject) => {
        const callbackName = `vworldCoord_${Date.now()}`;
        const script = document.createElement("script");
    
        script.src =
            `https://api.vworld.kr/req/address` +
            `?service=address` +
            `&request=GetCoord` +
            `&version=2.0` +
            `&crs=EPSG:4326` +
            `&type=ROAD` +
            `&address=${encodeURIComponent(address)}` +
            `&format=json` +
            `&errorformat=json` +
            `&callback=${callbackName}` +
            `&key=${import.meta.env.VITE_ROADADDR_TO_COORD_API_KEY}`;
    
            (window as any)[callbackName] = (data: any) => {
            try {
            const point = data?.response?.result?.point;
            if (!point) throw new Error("좌표 없음");
    
            resolve({
                lat: Number(point.y),
                lng: Number(point.x),
            });
            } catch (e) {
            reject(e);
            } finally {
            cleanup();
            }
        };
    
        script.onerror = () => {
            reject(new Error("좌표 변환 실패"));
            cleanup();
        };
    
        const cleanup = () => {
            delete (window as any)[callbackName];
            document.body.removeChild(script);
        };
    
        document.body.appendChild(script);
        });
    };
    


    const handleSelect = async (item: SearchResult) => {
        let data;
    
        if (item.type === "place") {
            // 카카오는 좌표 이미 있음
            data = {
                address: item.address,
                latitude: item.lat!,
                longitude: item.lng!,
            };
        } else {
            // 주소는 좌표 변환 필요
            const res = await getCoordByAddress(item.address);
            data = {
                address: item.address,
                latitude: res.lat,
                longitude: res.lng,
            };
        }
    
        if (activeType === "source") {
            setSourceAddress(data);
        }
    
        if (activeType === "dest") {
            setDestAddress(data);
        }
    
        setInputText("");
        setSearchList([]);
        setActiveType(null);
    };

    const fetchRoute = async (url: string, { arg }: { arg: any }) => {
        const res = await axios.post(url, arg);
        return res.data;
    };

    const { trigger, isMutating } = useSWRMutation(
        `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/myRootPath_v2`,
        fetchRoute
    );

    const selectSrcDest = async () => {
        if (!sourceAddress || !destAddress) return;

        try {
            const data = await trigger({
                fromLongitude: sourceAddress.longitude,
                fromLatitude: sourceAddress.latitude,
                toLongitude: destAddress.longitude,
                toLatitude: destAddress.latitude,
            });

            setRoute(data.points, data.distance);
            navigate("/");
        } catch (e) {
            console.error("경로 조회 실패", e);
        }
    }

    return (
        <div className="relative w-full h-screen flex flex-col gap-3 px-4 pt-4 pb-24">
            {/* 경로 탐색 로딩 창 */}
            {isMutating && (
                <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
                    <div className="bg-white px-6 py-5 rounded-2xl shadow-md flex flex-col items-center gap-3">
                    {/* 🔥 스피너 */}
                    <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <div className="text-sm text-gray-600">
                        경로 탐색 중...
                    </div>
                    </div>
                </div>
            )}

            {/* 출발지 */}
            <div className="bg-white rounded-lg px-4 py-3 shadow-sm">
                <div className="text-sm font-medium mb-1">출발지</div>
                <input
                value={
                    activeType === "source" ? inputText : sourceAddress?.address || ""
                }
                placeholder="출발지 입력"
                className="w-full text-sm outline-none"
                onFocus={() => {
                    setActiveType("source");
                    setInputText("");
                }}
                onChange={(e) => setInputText(e.target.value)}
                />
            </div>
        
            {/* 도착지 */}
            <div className="bg-white rounded-lg px-4 py-3 shadow-sm">
                <div className="text-sm font-medium mb-1">도착지</div>
                <input
                value={activeType === "dest" ? inputText : destAddress?.address || ""}
                placeholder="도착지 입력"
                className="w-full text-sm outline-none"
                onFocus={() => {
                    setActiveType("dest");
                    setInputText("");
                }}
                onChange={(e) => setInputText(e.target.value)}
                />
            </div>
        
            {/* 주소 리스트 */}
            {activeType && (
                <div className="bg-white rounded-lg shadow-md flex-1 overflow-y-auto mt-2">
                {loading && (
                    <div className="p-4 text-sm text-gray-500">검색 중...</div>
                )}
        
                {!loading && searchList.length === 0 && (
                    <div className="p-4 text-sm text-gray-400">검색 결과 없음</div>
                )}
        
                {searchList.map((item, idx) => (
                    <div
                        key={idx}
                        className="px-4 py-4 border-b cursor-pointer hover:bg-gray-100"
                        onClick={() => handleSelect(item)}
                    >
                    {/* 장소 검색 */}
                    {item.type === "place" ? (
                    <>
                        <div className="font-medium">{item.name}</div>
                        <div className="text-sm text-gray-500">{item.address}</div>
                    </>
                    ) : (
                    /* 주소 검색 */
                    <div className="font-medium">{item.address}</div>
                    )}
                </div>
                ))}
                </div>
            )}
            <div className="absolute bottom-4 left-0 w-full px-4 flex gap-2">
                {/* 취소 버튼 25% */}
                <button
                    className="flex-[1] bg-white text-gray-500 border border-gray-500 py-2 rounded-lg font-medium hover:bg-gray-100 transition"
                    onClick={() => navigate("/")} // 취소 시 메인 페이지
                >
                    취소
                </button>

                {/* 설정 완료 버튼 75% */}
                <button
                    className="flex-[3] bg-blue-500 text-white py-2 rounded-lg font-medium hover:bg-blue-600 transition disabled:opacity-50"
                    onClick={selectSrcDest} // 완료 시 메인 페이지
                    disabled={!sourceAddress || !destAddress}
                >
                    설정 완료
                </button>
            </div>
        </div>
    );
};