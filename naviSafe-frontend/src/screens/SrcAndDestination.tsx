import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useLocationStore } from "../store/locationStore";
import { useRouteStore } from "../store/routeStore";

type ActiveType = "source" | "dest" | null;

export const SrcAndDestination = () => {
    const navigate = useNavigate();
    const { sourceAddress, destAddress, setSourceAddress, setDestAddress } =
        useLocationStore();
    const {setRouteCoords} = useRouteStore();

    const [activeType, setActiveType] = useState<ActiveType>(null);
    const [inputText, setInputText] = useState("");
    const [addressList, setAddressList] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (inputText.length < 2) {
        setAddressList([]);
        return;
        }

        const fetchAddress = async () => {
        try {
            setLoading(true);
            const res = await axios.get(
            `https://business.juso.go.kr/addrlink/addrLinkApi.do`,
            {
                params: {
                currentPage: 1,
                countPerPage: 10,
                keyword: inputText,
                resultType: "json",
                confmKey: `${import.meta.env.VITE_SEARCH_ADDRESS_API_KEY}`,
                },
            }
            );

            setAddressList(res.data?.results?.juso ?? []);
        } catch (e) {
            console.error("주소 검색 실패", e);
            setAddressList([]);
        } finally {
            setLoading(false);
        }
        };

        fetchAddress();
    }, [inputText]);

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
    


    const handleSelect = async (address: string) => {
        if (activeType === "source") {
            const res = await getCoordByAddress(address);
            setSourceAddress({
                address : address,
                latitude : res.lat,
                longitude : res.lng
            });
        }
        if (activeType === "dest") {
            const res = await getCoordByAddress(address);
            setDestAddress({
                address : address,
                latitude : res.lat,
                longitude : res.lng
            });
        }
        
        setInputText("");
        setAddressList([]);
        setActiveType(null);
    };

    const selectSrcDest = async () => {
        if (!sourceAddress || !destAddress) return;

        try {
            const res = await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/myRootPath`,
            {
                fromLongitude: sourceAddress.longitude,
                fromLatitude: sourceAddress.latitude,
                toLongitude: destAddress.longitude,
                toLatitude: destAddress.latitude,
            }
            );

            setRouteCoords(res.data);
            navigate("/");
        } catch (e) {
            console.error("경로 조회 실패", e);
        }
    }

    return (
        <div className="relative w-full h-screen flex flex-col gap-3 px-4 pt-4 pb-24">
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
        
                {!loading && addressList.length === 0 && (
                    <div className="p-4 text-sm text-gray-400">검색 결과 없음</div>
                )}
        
                {addressList.map((item, idx) => (
                    <div
                    key={idx}
                    className="px-4 py-4 border-b cursor-pointer hover:bg-gray-100"
                    onClick={() => handleSelect(item.roadAddr)}
                    >
                    {item.roadAddr}
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