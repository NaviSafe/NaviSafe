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
        data: {
            channel: resultData.channel,
            type: resultData.type
        },
    };

    e.waitUntil(
        self.registration.showNotification(notificationTitle, notificationOptions),
    );
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    const { channel, type } = event.notification.data;

    event.waitUntil(
        self.clients.matchAll({
            type: "window",
            includeUncontrolled: true,
        }).then(async (clients) => {

            // 이미 열려 있는 페이지가 있으면 → 메시지 전달
            for (const client of clients) {
                client.postMessage({
                    channel,
                    pushType: type,
                });
                return client.focus();
            }

            const url = `/?channel=${channel}&pushType=${type}`;
            await self.clients.openWindow(url);
        })
    );
});