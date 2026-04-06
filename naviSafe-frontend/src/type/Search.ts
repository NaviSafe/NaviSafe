export type SearchResult = {
    type: "place" | "address";
    name?: string;
    address: string;
    lat?: number;
    lng?: number;
};    