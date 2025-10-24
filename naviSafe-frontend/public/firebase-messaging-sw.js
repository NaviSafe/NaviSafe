self.addEventListener("install", function () {
    self.skipWaiting();
});

self.addEventListener("activate", function () {
    console.log("fcm sw activate..");
});

self.addEventListener("push", function (e) {
    if (!e.data.json()) return;
    const payload = e.data.json();
    const resultData = payload.notification || payload.data || payload;

    const notificationTitle = resultData.title;
    const notificationOptions = {
        body: resultData.body,
        data: resultData.data,
    };

    e.waitUntil(
        self.registration.showNotification(notificationTitle, notificationOptions),
    );
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();
    const urlToOpen = event.notification.data;
    event.waitUntil(self.clients.openWindow(urlToOpen));
});