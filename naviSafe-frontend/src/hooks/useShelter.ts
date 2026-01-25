import { useEffect } from "react";
import axios from "axios";
import type { ShelterType } from "../store/shelterStore";
import { useShelterTypeState } from "../store/shelterStore";

export const useShelter = (initialId: number = 1) => {
    const shelterType = useShelterTypeState((state) => state.shelterType);
    const setShelterType = useShelterTypeState((state) => state.setShelterType);
    const resetShelterType = useShelterTypeState((state) => state.resetShelterType);

    // 초기 fetch
    useEffect(() => {
        if(initialId == 0){
            return;
        }

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
        // 똑같은 탭을 한번 더 누르면 초기화
        if(shelterType.shelterCode == id){
            resetShelterType();
            return;
        }

        try {
        const res = await axios.get<ShelterType>(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/shelter/${id}`
        );
        setShelterType(res.data);
        } catch (err) {
        console.error("Shelter 데이터 로드 실패:", err);
        }
    };
    
    // 즉시 반환전용
    const fetchShelterById = async (id: number): Promise<ShelterType | null> => {
        try {
            const res = await axios.get<ShelterType>(
                `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/shelter/${id}`
            );
            return res.data;
        } catch (err) {
            console.error("Shelter 데이터 로드 실패:", err);
            return null;
        }
    };

    return { shelterType, handleShelterClick, fetchShelterById };
};