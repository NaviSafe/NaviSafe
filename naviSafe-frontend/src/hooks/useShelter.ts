import { useEffect } from "react";
import axios from "axios";
import type { ShelterType } from "../store/shelterStore";
import { useShelterTypeState } from "../store/shelterStore";

export const useShelter = (initialId: number = 1) => {
    const shelterType = useShelterTypeState((state) => state.shelterType);
    const setShelterType = useShelterTypeState((state) => state.setShelterType);

    // 초기 fetch
    useEffect(() => {
        const fetchInitialData = async () => {
        try {
            const res = await axios.get<ShelterType>(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/shelter/${initialId}`
            );
            setShelterType(res.data);
        } catch (err) {
            console.error("초기 Shelter 데이터 로드 실패:", err);
        }
        };

        fetchInitialData();
    }, [initialId]);

    // Shelter 변경 핸들러
    const handleShelterClick = async (id: number) => {
        try {
        const res = await axios.get<ShelterType>(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/shelter/${id}`
        );
        setShelterType(res.data);
        } catch (err) {
        console.error("Shelter 데이터 로드 실패:", err);
        }
    };

    return { shelterType, handleShelterClick };
};