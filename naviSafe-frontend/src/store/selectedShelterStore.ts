import { create } from "zustand";

type ShelterDetailInfo = {
    code: number;
    name: String;
    lat : number;
    lot : number;
}

interface SelectedShelterState {
selectedShelter: ShelterDetailInfo | null;
setSelectedShelter: (shelter: ShelterDetailInfo | null) => void;
}

export const useSelectedShelter = create<SelectedShelterState>((set) => ({
    selectedShelter: null,
    setSelectedShelter: (shelter) => set({ selectedShelter: shelter }),
}));