import {create} from 'zustand';

export type ShelterType = {
	shelterCode: number;
    shelterCodeName: String;
    shelterGpsList: ShelterGps[];
}

export type ShelterGps = {
    shelterName: String;
    shelterAddress: String;
    lot: number;
    lat: number;
    shelterCode: number;
} 

interface ShelterTypeState {
    shelterType : ShelterType;
    setShelterType: (shelter : ShelterType) => void;
    resetShelterType : () => void;
}

export const useShelterTypeState = create<ShelterTypeState>((set) => ({
    shelterType : {
        shelterCode: 0,
        shelterCodeName: "",
        shelterGpsList: []
    },
    setShelterType: (shelter) => set({shelterType: shelter}),
    resetShelterType : () => set({
        shelterType : {
            shelterCode: 0,
            shelterCodeName: "",
            shelterGpsList: []
        }
    })
}))