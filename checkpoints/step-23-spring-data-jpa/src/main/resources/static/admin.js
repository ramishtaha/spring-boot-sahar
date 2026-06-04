// Sahar admin (step 08). Vanilla JS, no framework. Same-origin fetches to the REST API.

const $ = (id) => document.getElementById(id);

function toast(msg, kind) {
    const t = $('toast');
    t.textContent = msg;
    t.className = 'toast show ' + (kind || '');
    setTimeout(() => { t.className = 'toast ' + (kind || ''); }, 2200);
}

// A thin fetch wrapper: returns { ok, status, body }. Parses JSON when present.
async function api(method, path, body) {
    const opts = { method, headers: {} };
    if (body !== undefined) {
        opts.headers['Content-Type'] = 'application/json';
        opts.body = JSON.stringify(body);
    }
    const resp = await fetch(path, opts);
    let parsed = null;
    const text = await resp.text();
    if (text) {
        try { parsed = JSON.parse(text); } catch { parsed = text; }
    }
    return { ok: resp.ok, status: resp.status, body: parsed };
}

// Pull a human message out of an RFC 9457 ProblemDetail response (or fall back).
function errorText(res) {
    const b = res.body || {};
    if (Array.isArray(b.errors)) return b.errors.join('; '); // ProblemDetail validation: our "errors" array
    if (b.detail) return b.detail;                           // ProblemDetail "detail"
    if (Array.isArray(b.messages)) return b.messages.join('; '); // legacy shape
    if (b.message) return b.message;
    if (typeof res.body === 'string' && res.body) return res.body;
    return 'HTTP ' + res.status;
}

// ---- load everything into the forms ----
async function load() {
    const res = await api('GET', '/api/config');
    if (!res.ok) { toast('Could not load config', 'err'); return; }
    const cfg = res.body;
    $('month').value = cfg.month || '';
    const p = cfg.prayerTimes || {};
    ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'].forEach(k => { $(k).value = p[k] || ''; });
    $('methodNote').value = p.methodNote || '';
    $('blockLabel').textContent = (cfg.block && cfg.block.label) || '';
    renderWeeks(cfg.block ? cfg.block.weeks : []);
    renderDietPlan(cfg.dietPlan || []);
    initLocation(cfg.location || { placeName: '', lat: 19.22, lng: 72.98, tzOffset: 330 });
}

// ---- month ----
$('saveMonth').addEventListener('click', async () => {
    const res = await api('PUT', '/api/month', { month: $('month').value });
    toast(res.ok ? 'Month saved ✓' : 'Save failed: ' + errorText(res), res.ok ? 'ok' : 'err');
});

// ---- prayer times ----
$('savePrayer').addEventListener('click', async () => {
    $('prayerErr').textContent = '';
    const payload = {
        fajr: $('fajr').value, sunrise: $('sunrise').value, dhuhr: $('dhuhr').value,
        asr: $('asr').value, maghrib: $('maghrib').value, isha: $('isha').value,
        methodNote: $('methodNote').value
    };
    const res = await api('PUT', '/api/prayer-times', payload);
    if (res.ok) {
        toast('Prayer times saved ✓', 'ok');
    } else {
        $('prayerErr').textContent = errorText(res);
        toast('Validation failed', 'err');
    }
});

// ---- prayer times: calculate from device location ----
$('locatePrayer').addEventListener('click', () => {
    const note = $('prayerCalcNote');
    if (!navigator.geolocation) { note.textContent = 'Geolocation is not available in this browser.'; return; }
    note.textContent = 'Locating… allow the permission prompt.';
    navigator.geolocation.getCurrentPosition(async (pos) => {
        const lat = pos.coords.latitude, lng = pos.coords.longitude;
        const tz = -new Date().getTimezoneOffset();           // minutes east of UTC
        const date = new Date().toISOString().slice(0, 10);
        const res = await api('GET', `/api/prayer-times/calculate?lat=${lat}&lng=${lng}&tz=${tz}&date=${date}`);
        if (!res.ok) { note.textContent = 'Calculation failed: ' + errorText(res); return; }
        const p = res.body;
        ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'].forEach(k => { $(k).value = p[k] || ''; });
        $('methodNote').value = p.methodNote || '';
        note.textContent = 'Filled from your location — review, then Save prayer times to keep them.';
        toast('Calculated ✓ review & save', 'ok');
    }, (err) => {
        note.textContent = err.code === 1
            ? 'Location permission denied — you can still type the times in by hand.'
            : 'Could not get your location.';
    }, { timeout: 10000, maximumAge: 600000 });
});

// ---- block ----
function renderWeeks(weeks) {
    const host = $('weeks');
    host.innerHTML = '';
    (weeks || []).forEach(w => {
        const card = document.createElement('div');
        card.className = 'week' + (w.deload ? ' deload' : '');
        card.style.marginBottom = '0.8rem';
        card.innerHTML = `
            <div class="week-head">
                <span class="week-no">W${w.ordinal}</span>
                ${w.deload ? '<span class="pill">deload</span>' : ''}
            </div>
            <div class="grid-2">
                <div class="field"><label>Name</label><input data-f="name" value="${attr(w.name)}"></div>
                <div class="field"><label>Start</label><input data-f="startDate" value="${attr(w.startDate)}"></div>
                <div class="field"><label>End</label><input data-f="endDate" value="${attr(w.endDate)}"></div>
            </div>
            <div class="field"><label>Training focus</label><input data-f="trainingFocus" value="${attr(w.trainingFocus)}"></div>
            <div class="field"><label>Backend focus</label><input data-f="backendFocus" value="${attr(w.backendFocus)}"></div>
            <div class="nav">
                <button data-act="saveWeek">Save week ${w.ordinal}</button>
                <button data-act="dropWeek" class="secondary">Drop</button>
            </div>`;
        card.querySelector('[data-act="saveWeek"]').addEventListener('click', () => saveWeek(w.ordinal, card));
        card.querySelector('[data-act="dropWeek"]').addEventListener('click', () => dropWeek(w.ordinal));
        host.appendChild(card);
    });
}

function attr(s) { return String(s == null ? '' : s).replace(/"/g, '&quot;'); }

async function saveWeek(ordinal, card) {
    const get = (f) => card.querySelector(`[data-f="${f}"]`).value;
    const payload = {
        name: get('name'), startDate: get('startDate'), endDate: get('endDate'),
        trainingFocus: get('trainingFocus'), backendFocus: get('backendFocus')
    };
    const res = await api('PUT', '/api/block/weeks/' + ordinal, payload);
    if (res.ok) { toast('Week ' + ordinal + ' saved ✓', 'ok'); afterBlock(res.body); }
    else { $('blockErr').textContent = errorText(res); toast('Save failed', 'err'); }
}

async function dropWeek(ordinal) {
    const res = await api('DELETE', '/api/block/weeks/' + ordinal);
    if (res.ok) { toast('Dropped week ' + ordinal + ' ✓', 'ok'); afterBlock(res.body); }
    else { $('blockErr').textContent = errorText(res); toast(errorText(res), 'err'); }
}

$('addWeek').addEventListener('click', async () => {
    const res = await api('POST', '/api/block/weeks');
    if (res.ok) { toast('Week added ✓', 'ok'); afterBlock(res.body); }
    else { $('blockErr').textContent = errorText(res); toast(errorText(res), 'err'); }
});

$('rollForward').addEventListener('click', async () => {
    if (!confirm('Roll the block forward to a fresh template for next month?')) return;
    const res = await api('POST', '/api/block/roll-forward');
    if (res.ok) { toast('Rolled forward ✓', 'ok'); afterBlock(res.body); }
    else { toast('Failed: ' + errorText(res), 'err'); }
});

// The block endpoints return the new block; re-render from it (no extra GET needed).
function afterBlock(block) {
    $('blockErr').textContent = '';
    if (block && block.weeks) { $('blockLabel').textContent = block.label || ''; renderWeeks(block.weeks); }
}

// ---- diet plan ----
function renderDietPlan(days) {
    const host = $('dietPlan');
    host.innerHTML = '';
    (days || []).forEach(m => {
        const row = document.createElement('div');
        row.className = 'week';
        row.style.marginBottom = '0.7rem';
        row.innerHTML = `
            <div class="week-head"><span class="week-no">${attr(m.day)}</span></div>
            <div class="field"><label>Lunch</label><input data-f="lunch" value="${attr(m.lunch)}"></div>
            <div class="field"><label>Dinner</label><input data-f="dinner" value="${attr(m.dinner)}"></div>
            <div class="field"><label>Note</label><input data-f="note" value="${attr(m.note)}"></div>
            <button data-act="saveMeal">Save ${attr(m.day)}</button>`;
        row.querySelector('[data-act="saveMeal"]').addEventListener('click', () => saveMealDay(m.day, row));
        host.appendChild(row);
    });
}

async function saveMealDay(day, row) {
    $('dietErr').textContent = '';
    const get = (f) => row.querySelector(`[data-f="${f}"]`).value;
    const res = await api('PUT', '/api/diet-plan/' + encodeURIComponent(day),
        { lunch: get('lunch'), dinner: get('dinner'), note: get('note') });
    if (res.ok) { toast(day + ' meals saved ✓', 'ok'); }
    else { $('dietErr').textContent = errorText(res); toast('Save failed', 'err'); }
}

// ---- location: search / device / map pin ----
let map = null, marker = null;

function initLocation(loc) {
    $('locName').value = loc.placeName || '';
    $('locLat').value = loc.lat;
    $('locLng').value = loc.lng;
    $('locTz').value = loc.tzOffset;
    if (window.L && !map) {
        map = L.map('map').setView([loc.lat, loc.lng], 11);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
            { maxZoom: 19, attribution: '© OpenStreetMap contributors' }).addTo(map);
        marker = L.marker([loc.lat, loc.lng], { draggable: true }).addTo(map);
        marker.on('dragend', () => { const ll = marker.getLatLng(); setCoords(ll.lat, ll.lng); });
        map.on('click', (e) => { marker.setLatLng(e.latlng); setCoords(e.latlng.lat, e.latlng.lng); });
        setTimeout(() => map.invalidateSize(), 120); // ensure tiles lay out once the card is visible
    }
}

function setCoords(lat, lng) {
    $('locLat').value = (+lat).toFixed(5);
    $('locLng').value = (+lng).toFixed(5);
    reverseName(lat, lng);
}

async function reverseName(lat, lng) {
    const res = await api('GET', `/api/geocode/reverse?lat=${lat}&lng=${lng}`);
    if (res.ok && res.body && res.body.name) $('locName').value = res.body.name;
}

function moveMap(lat, lng, zoom) {
    if (map && marker) { map.setView([lat, lng], zoom || map.getZoom()); marker.setLatLng([lat, lng]); }
}

async function doLocSearch() {
    const q = $('locSearch').value.trim();
    if (!q) return;
    const host = $('locResults');
    host.innerHTML = '<div class="loc-result muted">Searching…</div>';
    const res = await api('GET', '/api/geocode?q=' + encodeURIComponent(q));
    host.innerHTML = '';
    if (!res.ok || !Array.isArray(res.body) || !res.body.length) {
        host.innerHTML = '<div class="loc-result muted">No matches (you may be offline).</div>';
        return;
    }
    res.body.forEach(r => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'loc-result';
        item.textContent = r.name;
        item.addEventListener('click', () => {
            $('locName').value = r.name;
            $('locLat').value = (+r.lat).toFixed(5);
            $('locLng').value = (+r.lng).toFixed(5);
            moveMap(r.lat, r.lng, 11);
            host.innerHTML = '';
        });
        host.appendChild(item);
    });
}
$('locSearchBtn').addEventListener('click', doLocSearch);
$('locSearch').addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); doLocSearch(); } });

$('locGeo').addEventListener('click', () => {
    if (!navigator.geolocation) { $('locErr').textContent = 'Geolocation is not available in this browser.'; return; }
    $('locErr').textContent = 'Locating… allow the permission prompt.';
    navigator.geolocation.getCurrentPosition((pos) => {
        const lat = pos.coords.latitude, lng = pos.coords.longitude;
        $('locLat').value = lat.toFixed(5);
        $('locLng').value = lng.toFixed(5);
        $('locTz').value = -new Date().getTimezoneOffset();
        moveMap(lat, lng, 12);
        reverseName(lat, lng);
        $('locErr').textContent = '';
    }, (err) => {
        $('locErr').textContent = err.code === 1 ? 'Location permission denied.' : 'Could not get your location.';
    }, { timeout: 10000, maximumAge: 600000 });
});

function readLoc() {
    return {
        placeName: $('locName').value,
        lat: parseFloat($('locLat').value),
        lng: parseFloat($('locLng').value),
        tzOffset: parseInt($('locTz').value || '0', 10)
    };
}

$('locSave').addEventListener('click', async () => {
    const res = await api('PUT', '/api/location', readLoc());
    if (res.ok) { $('locErr').textContent = ''; toast('Location saved ✓', 'ok'); }
    else { $('locErr').textContent = errorText(res); toast('Save failed', 'err'); }
});

$('locCalc').addEventListener('click', async () => {
    const l = readLoc();
    const date = new Date().toISOString().slice(0, 10);
    const res = await api('GET', `/api/prayer-times/calculate?lat=${l.lat}&lng=${l.lng}&tz=${l.tzOffset}&date=${date}`);
    if (!res.ok) { $('locErr').textContent = errorText(res); return; }
    const p = res.body;
    ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'].forEach(k => { $(k).value = p[k] || ''; });
    $('methodNote').value = p.methodNote || '';
    $('locErr').textContent = '';
    toast('Prayer times calculated ✓ review & save below', 'ok');
});

load();
