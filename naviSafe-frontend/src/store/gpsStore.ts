import {create} from 'zustand';

export interface GpsItem {
    acc_id: string;
    x: number;
    y: number;
}

interface GpsState {
    gpsList : GpsItem[];
    setGpsList: (list: GpsItem[]) => void;
    addGpsItem: (item:GpsItem) => void;
    clearGpsList: () => void;
}

export const useGpsStore = create<GpsState>((set) => ({
    gpsList: [],
    setGpsList: (list) => set({gpsList: list}),
    addGpsItem: (item) => set((state) => ({gpsList: [...state.gpsList, item]})) ,
    clearGpsList: () => set({gpsList: []})
}))