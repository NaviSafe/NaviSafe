import { create } from "zustand";

interface RouteStore {
    routeCoords: { lat: number; lon: number }[];
    distance: number;
    setRoute: (coords: { lat: number; lon: number }[], distance: number) => void;
    resetRoute: () => void;
}

export const useRouteStore = create<RouteStore>((set) => ({
    routeCoords: [],
    distance: 0,
    setRoute: (coords, distance) => set({ routeCoords: coords, distance }),
    resetRoute: () => set({ routeCoords: [], distance: 0 }),
}));