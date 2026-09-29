import { create } from "zustand";

interface MapStore {
    moveToCurrentLocation: boolean;
    requestMoveToCurrentLocation: () => void;
    finishMoveToCurrentLocation: () => void;
}

export const useMapStore = create<MapStore>((set) => ({
    moveToCurrentLocation: false,

    requestMoveToCurrentLocation: () =>
        set({ moveToCurrentLocation: true }),

    finishMoveToCurrentLocation: () =>
        set({ moveToCurrentLocation: false }),
}));