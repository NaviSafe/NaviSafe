import { getToken } from "firebase/messaging";
import { messaging } from "../../core/notification/settingFcm";

export function registerServiceWorker() {
    navigator.serviceWorker
        .register("firebase-messaging-sw.js")
        .then(function (registration) {
            console.log("Service Worker 등록 성공:", registration);
        })
        .catch(function (error) {
            console.log("Service Worker 등록 실패:", error);
        });
}

export async function requestNotificationPermission(): Promise<boolean> {
    if (Notification.permission === "granted") return true;
    if (Notification.permission === "denied") return false;
    
    try {
        const permission = await Notification.requestPermission();
        const granted = permission === "granted";
        console.log("알림 권한 요청 결과:", permission);
        return granted;
    } catch (error) {
        console.error("알림 권한 요청 중 오류:", error);
        return false;
    }
}

export async function getFcmToken(): Promise<string> {
    const token = await getToken(messaging, {
    vapidKey: import.meta.env.VITE_APP_FCM_VAPID_KEY
    });
    return token;
}