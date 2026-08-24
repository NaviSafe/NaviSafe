import axios from "axios";
import useSWRMutation from "swr/mutation";
import { useState, useEffect } from "react";

import { useNavigate } from "react-router-dom";
import { useLocationStore } from "../store/locationStore";
import { useRouteStore } from "../store/routeStore";
import { useSearchResultStore } from "../store/SearchResultStore";
import { useSearchOverlayStore } from "../store/SearchOverlayStore";

import { RecentRoute } from "../components/RecentRoute";
import { MdSwapVert } from "react-icons/md";

export const SrcAndDestination = () => {
    const navigate = useNavigate();
    const { sourceAddress, destAddress, setSourceAddress, setDestAddress } =
        useLocationStore();
    const {openSearch} = useSearchOverlayStore();
    const {setRoute} = useRouteStore();
    const {setSelectedResults, setSelectedPlace } = useSearchResultStore();
    const [recentRoutes, setRecentRoutes] = useState<
        {
            sourceAddress: {
                address: string;
                latitude: number;
                longitude: number;
            };
            destAddress: {
                address: string;
                latitude: number;
                longitude: number;
            };
        }[]
    >([]);

    useEffect(() => {
        const savedRoutes = localStorage.getItem("recentRoutes");

        if (savedRoutes) {
            setRecentRoutes(JSON.parse(savedRoutes));
        }
    }, []);
    
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
            const savedRoutes = localStorage.getItem("recentRoutes");
            const recentRoutes = savedRoutes
                ? JSON.parse(savedRoutes)
                : [];

            // 새로운 경로
            const newRoute = {
                sourceAddress,
                destAddress,
            };

            // 동일한 출발지 + 도착지 조합 제거
            const filteredRoutes = recentRoutes.filter(
                (route: any) =>
                    !(
                        route.sourceAddress.address === sourceAddress.address &&
                        route.destAddress.address === destAddress.address
                    )
            );

            // 새로운 경로를 맨 앞에 추가 + 최대 15개
            const updatedRoutes = [
                newRoute,
                ...filteredRoutes,
            ].slice(0, 15);

            // LocalStorage 저장
            localStorage.setItem(
                "recentRoutes",
                JSON.stringify(updatedRoutes)
            );

            const data = await trigger({
                fromLongitude: sourceAddress.longitude,
                fromLatitude: sourceAddress.latitude,
                toLongitude: destAddress.longitude,
                toLatitude: destAddress.latitude,
            });

            setRoute(data.points, data.distance);
            setSelectedPlace(null);    
            setSelectedResults([]);
            navigate("/");
        } catch (e) {
            console.error("경로 조회 실패", e);
        }
    }

    return (
        <div className="relative w-full h-screen bg-white overflow-hidden">
            {/* 경로 탐색 로딩 창 */}
            {isMutating && (
                <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
                    <div className="bg-white px-6 py-5 rounded-2xl shadow-md flex flex-col items-center gap-3">
                    {/* 스피너 */}
                    <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <div className="text-sm text-gray-600">
                        경로 탐색 중...
                    </div>
                    </div>
                </div>
            )}

            <div className="bg-blue-500 px-4 pt-4 pb-3">
                <div className="relative bg-white/10 rounded-md overflow-hidden">
                    {/* 출발지 */}
                    <div
                        onClick={() => {
                            setSelectedPlace(null);
                            openSearch();
                            navigate("/");
                        }}
                        className="
                            h-[42px]
                            px-3
                            flex
                            items-center
                            cursor-pointer
                            border-b border-white/10
                        "
                    >
                        <span
                            className={
                                sourceAddress
                                    ? "text-sm text-white"
                                    : "text-sm text-white/60"
                            }
                        >
                            {sourceAddress?.address || "출발지 입력"}
                        </span>
                    </div>

                    {/* 도착지 */}
                    <div
                        onClick={() => {
                            setSelectedPlace(null);
                            openSearch();
                            navigate("/");
                        }}
                        className="
                            h-[42px]
                            px-3
                            flex
                            items-center
                            cursor-pointer
                        "
                    >
                        <span
                            className={
                                destAddress
                                    ? "text-sm text-white"
                                    : "text-sm text-white/60"
                            }
                        >
                            {destAddress?.address || "도착지 입력"}
                        </span>
                    </div>

                    <button
                        className="
                            absolute
                            right-2
                            top-1/2
                            -translate-y-1/2
                            text-white
                        "
                    >
                        <MdSwapVert size={22} />
                    </button>
                </div>
            </div>

            {/* 최근 경로 */}
            <div className="flex-1 overflow-y-auto pb-20">
                {recentRoutes.length > 0 ? (
                    recentRoutes.map((route, index) => (
                        <RecentRoute
                            key={index}
                            from={route.sourceAddress}
                            to={route.destAddress}
                            onClick={() => {
                                setSourceAddress(route.sourceAddress);
                                setDestAddress(route.destAddress);
                            }}
                        />
                    ))
                ) : (
                    <div className="flex items-center justify-center py-20">
                        <span className="text-sm text-gray-400">
                            최근 경로가 없습니다.
                        </span>
                    </div>
                )}
            </div>


                {/* 안내 시작 */}
                <div
                className="
                        absolute
                        bottom-0
                        left-0
                        right-0
                        bg-white
                        border-t
                        border-gray-200
                        p-3
                        flex
                        gap-2
                    "
                >
                    {/* 취소 */}
                    <button
                        onClick={() => {
                            setSelectedPlace(null);
                            setSelectedResults([]);
                            navigate("/");
                        }}
                        className="
                            flex-[1]
                            py-3
                            rounded-xl
                            border
                            border-gray-300
                            bg-white
                            text-gray-600
                            font-medium
                            hover:bg-gray-50
                            transition
                        "
                    >
                        취소
                    </button>

                    {/* 안내 시작 */}
                    <button
                        onClick={selectSrcDest}
                        disabled={!sourceAddress || !destAddress || isMutating}
                        className="
                            flex-[3]
                            py-3
                            rounded-xl
                            bg-blue-500
                            text-white
                            font-semibold
                            hover:bg-blue-600
                            disabled:bg-gray-300
                            transition
                        "
                    >
                    {isMutating ? "경로 탐색 중..." : "안내 시작"}
                </button>
            </div>
        </div>
    );
};