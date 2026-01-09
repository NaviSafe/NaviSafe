import { create } from "zustand";

type AddressInfo = {
    address: string;
    latitude: number;
    longitude: number;
};

type LocationState = {
    sourceAddress: AddressInfo | null;
    destAddress: AddressInfo | null;

    setSourceAddress: (address: AddressInfo) => void;
    setDestAddress: (address: AddressInfo) => void;

    resetLocation: () => void;
};

export const useLocationStore = create<LocationState>((set) => ({
    sourceAddress: null,
    destAddress: null,

    setSourceAddress: (address) =>
        set({ sourceAddress: address }),

    setDestAddress: (address) =>
        set({ destAddress: address }),

    resetLocation: () =>
        set({
        sourceAddress: null,
        destAddress: null,
        }),
}));