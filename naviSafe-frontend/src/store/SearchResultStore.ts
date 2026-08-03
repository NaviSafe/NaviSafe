import { create } from "zustand";
import type { SearchResult } from "../type/Search";

interface SearchResultState {
    selectedResults: SearchResult[];
    selectedPlace: SearchResult | null;

    setSelectedResults: (list: SearchResult[]) => void;
    setSelectedPlace: (place: SearchResult | null) => void;
}

export const useSearchResultStore = create<SearchResultState>((set) => ({
    selectedResults: [],
    selectedPlace: null,

    setSelectedResults: (list) => set({ selectedResults: list }),
    setSelectedPlace: (place) => set({ selectedPlace: place }),
}));