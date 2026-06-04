// Sahar service worker — makes the app installable and usable offline.
//
// Strategy:
//   - the app SHELL (html/css/js/icons) is cache-first, so the UI loads instantly and offline;
//   - GET /api/* is network-first, so your data is fresh when online but falls back to the last
//     cached response when you have no connection.
// Bump CACHE when you change the shell so old caches are cleared.

const CACHE = 'sahar-v2';
const SHELL = [
    '/', '/index.html', '/styles.css', '/app.js',
    '/admin.html', '/admin.js',
    '/icon.svg', '/manifest.json'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', (event) => {
    const req = event.request;
    if (req.method !== 'GET') return; // never cache writes (PUT/POST/DELETE)
    const url = new URL(req.url);

    if (url.pathname.startsWith('/api/')) {
        // network-first: fresh online, cached fallback offline
        event.respondWith(
            fetch(req)
                .then((res) => {
                    const copy = res.clone();
                    caches.open(CACHE).then((c) => c.put(req, copy));
                    return res;
                })
                .catch(() => caches.match(req))
        );
    } else {
        // cache-first for the static shell
        event.respondWith(
            caches.match(req).then((cached) =>
                cached || fetch(req).then((res) => {
                    const copy = res.clone();
                    caches.open(CACHE).then((c) => c.put(req, copy));
                    return res;
                })
            )
        );
    }
});
