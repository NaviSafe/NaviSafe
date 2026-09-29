export type SearchResult = {
    id : string;
    category : string;
    type: "place" | "address";
    name: string;
    address: string;
    lat: number;
    lng: number;
    date?: string;
};    