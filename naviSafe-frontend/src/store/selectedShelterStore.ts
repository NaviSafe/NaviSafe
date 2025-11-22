import { create } from "zustand";
import type { ShelterInfo } from "../type/Shelter";

interface SelectedShelterState {
selectedShelter: ShelterInfo | null;
setSelectedShelter: (shelter: ShelterInfo | null) => void;
}

export const useSelectedShelter = create<SelectedShelterState>((set) => ({
    selectedShelter: null,
    setSelectedShelter: (shelter) => set({ selectedShelter: shelter }),
}));