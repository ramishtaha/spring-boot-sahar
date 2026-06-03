// Sahar routine data - CLIENT SIDE (step 01 only).
//
// This is the "static site" version: the browser owns the data. It works, but
// it has a fatal flaw for an editable app - changing your routine means editing
// and redeploying a file, and every visitor just gets a hardcoded copy.
//
// In step 02 this file is DELETED. The same shape is served by the backend at
// GET /api/config, and index.html fetches it instead. That is the pivot from a
// static page to a server-driven app.
window.SAHAR = {
    title: "Sahar",
    tagline: "recover, build, fight",
    month: "June 2026",

    prayerTimes: {
        fajr: "04:37",
        sunrise: "05:59",
        dhuhr: "12:37",
        asr: "17:12",
        maghrib: "19:13",
        isha: "20:36",
        methodNote: "Karachi 18° Fajr/Isha, Hanafi Asr, computed for Thane (19.22N, 72.98E). Recheck monthly; drifts under ~10 min across a month."
    },

    block: {
        label: "4-week block · 8 Jun – 5 Jul (Foundation · Build · Peak · Deload)",
        weeks: [
            {
                ordinal: 1, name: "Foundation", startDate: "8 Jun", endDate: "14 Jun", deload: false,
                trainingFocus: "Moderate, no PM sessions; lock the schedule and the journaling habit.",
                backendFocus: "Spring Boot core: setup, dependency injection, controllers, a CRUD REST API."
            },
            {
                ordinal: 2, name: "Build", startDate: "15 Jun", endDate: "21 Jun", deload: false,
                trainingFocus: "Add PM strength Mon and Thu; deeper deep-work; volume climbs.",
                backendFocus: "Persistence: Spring Data JPA, Postgres, repositories, validation."
            },
            {
                ordinal: 3, name: "Peak", startDate: "22 Jun", endDate: "28 Jun", deload: false,
                trainingFocus: "Full volume, sharpest spar, deepest learning.",
                backendFocus: "Docker and DevOps: Dockerfile, compose, env config, basic CI."
            },
            {
                ordinal: 4, name: "Deload", startDate: "29 Jun", endDate: "5 Jul", deload: true,
                trainingFocus: "MMA down ~40%, light or skipped spar, weekday mains drilling only, more sleep.",
                backendFocus: "Ship it: deploy the container, refactor, docs."
            }
        ]
    },

    weeklyGrid: [
        { day: "Mon", discipline: "Muay Thai (technical)" },
        { day: "Tue", discipline: "Boxing" },
        { day: "Wed", discipline: "Wrestling / BJJ (light)" },
        { day: "Thu", discipline: "Muay Thai" },
        { day: "Fri", discipline: "Padwork + light drills (taper)" },
        { day: "Sat", discipline: "SPAR + BJJ / MMA rounds" },
        { day: "Sun", discipline: "Rest / mobility walk" }
    ]
};
