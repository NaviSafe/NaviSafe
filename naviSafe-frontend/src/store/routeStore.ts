import { create } from "zustand";

interface RouteStore {
    routeCoords: { lat: number; lon: number }[];
    setRouteCoords: (coords: { lat: number; lon: number }[]) => void;
}

export const useRouteStore = create<RouteStore>((set) => ({
    routeCoords: [],
    setRouteCoords: (coords) => set({ routeCoords: coords }),
}));