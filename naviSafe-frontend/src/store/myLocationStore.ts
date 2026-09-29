import { create } from "zustand";

interface myPosition {
    lat: number;
    lon: number;
}

interface myLocationState {
    location: myPosition | null;
    setLocation: (pos: myPosition) => void;
}

export const useLocationStore = create<myLocationState>((set) => ({
    location: null,
    setLocation: (pos) => set({ location: pos }),
}));