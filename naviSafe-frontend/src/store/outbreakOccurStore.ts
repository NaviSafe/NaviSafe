import {create} from 'zustand';

export interface OutbreakOccur {
    accId: string;
    occrDate: string;
    expClrDate: string;
    accInfo: string;
    grs80tmX: number;
    grs80tmY: number,
    accTypeName: string,
    accDetailTypeName: string,
    roadName: string,
    startNodeName: string,
    endNodeName: string,
    mapDistance: number,
    speedLoadTraffic: number,
    travelTimeLoad: number,
    regionName: string
}

interface OutbreakOccurState {
    outbreakOccurList : OutbreakOccur[];
    setOutbreakOccurList: (list: OutbreakOccur[]) => void;
}

export const useOutbreakOccurState = create<OutbreakOccurState>((set) => ({
    outbreakOccurList: [],
    setOutbreakOccurList: (list) => set({outbreakOccurList: list}),
}))