import { useEffect } from "react";
import { useShelter } from "./useShelter";
import { useShelterTypeState } from "../store/shelterStore";
import { useCurrentLocation } from "./useCurrentLocation";
import { useRouteStore } from "../store/routeStore";
import { handleEmergencyPush } from "../utils/handleEmergencyPush";

export function usePushMessage() {
    const { getLocation } = useCurrentLocation();
    const { fetchShelterById } = useShelter(0);
    const { setShelterType } = useShelterTypeState();
    const { setRouteCoords } = useRouteStore();

    useEffect(() => {
        if (!("serviceWorker" in navigator)) return;

        const run = (channel: string, pushType: string) =>
            handleEmergencyPush({
                channel,
                pushType,
                getLocation,
                fetchShelterById,
                setRouteCoords,
                setShelterType,
            });

        // 포그라운드: postMessage
        const handler = (event: MessageEvent<any>) => {
            const { channel, pushType } = event.data ?? {};
            if (channel && pushType) run(channel, pushType);
        };

        navigator.serviceWorker.addEventListener("message", handler);

        // 백그라운드: URL 딥링크
        const params = new URLSearchParams(window.location.search);
        const channel = params.get("channel");
        const pushType = params.get("pushType");

        if (channel && pushType) {
            run(channel, pushType);
            window.history.replaceState({}, "", "/");
        }

        return () => {
            navigator.serviceWorker.removeEventListener("message", handler);
        };
    }, []);
}
