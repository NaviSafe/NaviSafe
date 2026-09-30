import { create } from "zustand";
import { persist } from "zustand/middleware";

interface OutbreakFilterState {
    excludeAccTypeNames: string[];
    setExcludeAccTypeNames: (names: string[]) => void;
    toggleExcludeAccTypeName: (name: string) => void;
    clearExcludeAccTypeNames: () => void;
}

export const useOutbreakFilterStore = create<OutbreakFilterState>()(
    persist(
        (set) => ({
        excludeAccTypeNames: [],

        setExcludeAccTypeNames: (names) =>
            set({ excludeAccTypeNames: names }),

        toggleExcludeAccTypeName: (name) =>
            set((state) => ({
            excludeAccTypeNames: state.excludeAccTypeNames.includes(name)
                ? state.excludeAccTypeNames.filter((item) => item !== name)
                : [...state.excludeAccTypeNames, name],
            })),

        clearExcludeAccTypeNames: () =>
            set({ excludeAccTypeNames: [] }),
        }),
        {
        name: "outbreak-filter",
        }
    )
);