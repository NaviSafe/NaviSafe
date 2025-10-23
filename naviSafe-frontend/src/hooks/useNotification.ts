import { useEffect, useState } from "react";
import { registerServiceWorker, requestNotificationPermission, getFcmToken} from "../utils/common/notification";
import { registerDeviceToken } from "../api/deviceTokenPost";

export function useFirebaseNotification() {
    const [fcmToken, setFcmToken] = useState<string>("");
    const [permissionGranted, setPermissionGranted] = useState<boolean>(false);

    useEffect(() => {
        async function initFirebaseMessaging() {
            try {
                registerServiceWorker();

                const granted = await requestNotificationPermission();
                setPermissionGranted(granted);
                if (!granted) return;

                const token = await getFcmToken();
                if (token) setFcmToken(token);

                await registerDeviceToken(token);
            } catch (error) {
                console.error("푸시 알림 초기화 실패:", error);
            }
        }

        initFirebaseMessaging();
    }, []);

    return { fcmToken, permissionGranted };
}