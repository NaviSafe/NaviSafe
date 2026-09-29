import { create } from "zustand";
import type { SearchResult } from "../type/Search";

interface SearchResultState {
    selectedResults: SearchResult[];
    selectedPlace: SearchResult | null;
    selectedListItem: SearchResult | null;

    setSelectedResults: (list: SearchResult[]) => void;
    setSelectedPlace: (place: SearchResult | null) => void;
    setSelectedListItem: (item: SearchResult|null)=>void;
}

export const useSearchResultStore = create<SearchResultState>((set) => ({
    selectedResults: [],
    selectedPlace: null,
    selectedListItem: null,

    setSelectedResults: (list) => set({ selectedResults: list }),
    setSelectedPlace: (place) => set({ selectedPlace: place }),
    setSelectedListItem: (item) => set({ selectedListItem: item }),
}));