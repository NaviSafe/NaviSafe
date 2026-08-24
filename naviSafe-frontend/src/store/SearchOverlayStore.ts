import { create } from "zustand";

interface SearchOverlayState {
    isOpen: boolean;
    openSearch: () => void;
    closeSearch: () => void;
}

export const useSearchOverlayStore = create<SearchOverlayState>((set) => ({
    isOpen: false,

    openSearch: () => set({ isOpen: true }),
    closeSearch: () => set({ isOpen: false }),
}));