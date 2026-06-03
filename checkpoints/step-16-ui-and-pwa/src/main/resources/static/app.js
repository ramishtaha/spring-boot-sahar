// Sahar home page — interactive renderer. Vanilla JS, no framework.
// Pulls GET /api/config once, renders, then keeps the prayer countdown live.

// ---------- tiny DOM helpers ----------
const $ = (id) => document.getElementById(id);
function el(tag, className, html) {
    const n = document.createElement(tag);
    if (className) n.className = className;
    if (html != null) n.innerHTML = html;
    return n;
}
function card(title, actionsNode) {
    const s = el('section', 'card fade-in');
    if (title) {
        const h = el('div', 'card-head');
        h.appendChild(el('h2', null, title));
        if (actionsNode) h.appendChild(actionsNode);
        s.appendChild(h);
    }
    return s;
}
function foldable(title, hint) {
    const d = el('details', 'card fold fade-in');
    d.appendChild(el('summary', null, '<span>' + esc(title) + '</span>' + (hint ? '<span class="hint">' + hint + '</span>' : '')));
    return d;
}
function esc(s) {
    return String(s == null ? '' : s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// ---------- time helpers ----------
const DOW = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const PRAYERS = [['Fajr', 'fajr'], ['Dhuhr', 'dhuhr'], ['Asr', 'asr'], ['Maghrib', 'maghrib'], ['Isha', 'isha']];
function toMin(hhmm) { const p = String(hhmm).split(':'); return (+p[0]) * 60 + (+p[1]); }
function nowMin() { const d = new Date(); return d.getHours() * 60 + d.getMinutes(); }
function fmtDur(mins) {
    const h = Math.floor(mins / 60), m = mins % 60;
    return (h > 0 ? h + 'h ' : '') + m + 'm';
}

let CONFIG = null;
let countdownTimer = null;

// ---------- render ----------
function render(cfg) {
    CONFIG = cfg;
    $('title').textContent = cfg.title || 'Sahar';
    $('tagline').textContent = cfg.tagline || '';
    $('month').textContent = cfg.month || '';
    document.title = (cfg.title || 'Sahar') + ' — ' + (cfg.month || '');

    const app = $('app');
    app.innerHTML = '';

    renderPrayer(app, cfg);
    renderWeek(app, cfg);
    renderBlock(app, cfg);
    renderTimeline(app, cfg);
    renderReference(app, cfg);

    startCountdown();
}

// 1) Prayer card: live next-prayer banner, highlighted strip, "use my location"
function renderPrayer(app, cfg) {
    if (!cfg.prayerTimes) return;
    const p = cfg.prayerTimes;
    const loc = el('button', 'mini-btn', '📍 my location');
    loc.title = 'Recalculate prayer times from your device location';
    loc.onclick = useMyLocation;
    const s = card('Prayer times', loc);

    s.appendChild(el('div', 'next-prayer', '<span id="np-label">—</span><span id="np-count"></span>'));

    if (cfg.location && cfg.location.placeName) {
        s.appendChild(el('p', 'loc-line', '📍 ' + esc(cfg.location.placeName) + ' · <a href="/admin.html">change</a>'));
    }

    const row = el('div', 'prayer-row');
    [['Fajr', p.fajr, 'fajr'], ['Sunrise', p.sunrise, 'sunrise'], ['Dhuhr', p.dhuhr, 'dhuhr'],
     ['Asr', p.asr, 'asr'], ['Maghrib', p.maghrib, 'maghrib'], ['Isha', p.isha, 'isha']]
        .forEach(([name, time, key]) => {
            if (time == null) return;
            const c = el('div', 'prayer' + (name === 'Sunrise' ? ' muted' : ''));
            c.dataset.key = key;
            c.appendChild(el('span', 'prayer-name', esc(name)));
            c.appendChild(el('span', 'prayer-time', esc(time)));
            row.appendChild(c);
        });
    s.appendChild(row);

    if (p.methodNote) {
        const note = el('details', 'inline-note');
        note.appendChild(el('summary', null, 'method'));
        note.appendChild(el('p', 'note', esc(p.methodNote)));
        s.appendChild(note);
    }
    app.appendChild(s);
}

// 2) "This week" board: training + meals, today highlighted, click for detail
function renderWeek(app, cfg) {
    if (!cfg.weeklyGrid) return;
    const today = DOW[new Date().getDay()];
    const s = card('This week');
    const breakfast = (cfg.diet || []).find(d => /breakfast/i.test(d.title));
    if (breakfast) s.appendChild(el('p', 'subtle', '🍳 Every morning · ' + esc(breakfast.body)));
    const meals = {};
    (cfg.dietPlan || []).forEach(m => { meals[m.day] = m; });
    const board = el('div', 'week-board');
    cfg.weeklyGrid.forEach(g => {
        const m = meals[g.day];
        const isToday = g.day === today;
        const c = el('button', 'cal-day' + (isToday ? ' today' : ''));
        c.onclick = () => openDayModal(g, m, isToday);
        c.innerHTML =
            '<div class="cal-dow">' + esc(g.day) + (isToday ? ' <span class="pill-today">today</span>' : '') + '</div>' +
            '<div class="cal-train">' + esc(g.discipline) + '</div>' +
            (m ? '<div class="cal-meals"><div class="cal-meal"><b>L</b> ' + esc(m.lunch) + '</div>' +
                 '<div class="cal-meal"><b>D</b> ' + esc(m.dinner) + '</div>' +
                 (m.note ? '<div class="cal-note">' + esc(m.note) + '</div>' : '') + '</div>' : '');
        board.appendChild(c);
    });
    s.appendChild(board);
    s.appendChild(el('p', 'subtle small', 'Tap a day for detail · heavy lunch, light dinner · full plan in docs/diet-plan.md'));
    app.appendChild(s);
    // bring today into view on small screens
    requestAnimationFrame(() => { const t = board.querySelector('.today'); if (t && board.scrollWidth > board.clientWidth) t.scrollIntoView({ inline: 'center', block: 'nearest' }); });
}

// 3) Training block
function renderBlock(app, cfg) {
    if (!cfg.block || !cfg.block.weeks) return;
    const s = card('Training block');
    if (cfg.block.label) s.appendChild(el('p', 'subtle', esc(cfg.block.label)));
    const grid = el('div', 'weeks');
    cfg.block.weeks.forEach(w => {
        const c = el('div', 'week' + (w.deload ? ' deload' : ''));
        c.innerHTML =
            '<div class="week-head"><span class="week-no">W' + esc(w.ordinal) + '</span>' +
            '<span class="week-name">' + esc(w.name) + '</span>' + (w.deload ? '<span class="pill">deload</span>' : '') + '</div>' +
            (w.startDate ? '<div class="week-dates">' + esc(w.startDate) + ' – ' + esc(w.endDate) + '</div>' : '') +
            (w.trainingFocus ? '<p class="week-line">' + esc(w.trainingFocus) + '</p>' : '');
        grid.appendChild(c);
    });
    s.appendChild(grid);
    app.appendChild(s);
}

// 4) Daily timeline (collapsed), with a "now" marker
function renderTimeline(app, cfg) {
    if (!cfg.schedule || !cfg.schedule.length) return;
    const now = nowMin();
    let nowIdx = -1;
    cfg.schedule.forEach((it, i) => { if (toMin(it.time) <= now) nowIdx = i; });
    const current = nowIdx >= 0 ? cfg.schedule[nowIdx] : null;
    const d = foldable('Daily timeline', current ? 'now · ' + esc(current.title) : esc(cfg.schedule[0].time) + ' → ' + esc(cfg.schedule[cfg.schedule.length - 1].time));
    const tl = el('div', 'timeline');
    cfg.schedule.forEach((it, i) => {
        const c = el('div', 'tl-item cat-' + esc(it.category || 'admin') + (i === nowIdx ? ' now' : '') + (toMin(it.time) < now && i !== nowIdx ? ' past' : ''));
        c.innerHTML = '<span class="tl-time">' + esc(it.time) + '</span>' +
            '<div class="tl-body"><span class="tl-title">' + esc(it.title) + '</span>' +
            (it.detail ? '<span class="tl-detail">' + esc(it.detail) + '</span>' : '') + '</div>' +
            '<span class="tag">' + esc(it.category || '') + '</span>';
        tl.appendChild(c);
    });
    d.appendChild(tl);
    app.appendChild(d);
}

// 5) Reference content (collapsed)
function renderReference(app, cfg) {
    if (cfg.supplements && cfg.supplements.length) {
        const d = foldable('Supplements', cfg.supplements.length + '');
        cfg.supplements.forEach(s => {
            d.appendChild(el('div', 'list-row',
                '<span class="list-when">' + esc(s.timeLabel) + '</span>' +
                '<div class="list-body"><span class="list-name">' + esc(s.name) + (s.dose ? ' · ' + esc(s.dose) : '') + '</span>' +
                (s.purpose ? '<span class="list-sub">' + esc(s.purpose) + '</span>' : '') +
                (s.note ? '<span class="list-sub muted">' + esc(s.note) + '</span>' : '') + '</div>'));
        });
        app.appendChild(d);
    }
    if (cfg.diet && cfg.diet.length) {
        const d = foldable('Nutrition principles', 'the rules behind the plan');
        cfg.diet.forEach(x => d.appendChild(el('div', 'diet', '<h3>' + esc(x.title) + '</h3><p>' + esc(x.body) + '</p>')));
        app.appendChild(d);
    }
    if (cfg.journal) {
        const d = foldable('Bullet journal', 'morning + night prompts');
        const two = el('div', 'two-col');
        [['Morning · 04:50', cfg.journal.morning], ['Night · 21:00', cfg.journal.night]].forEach(([label, items]) => {
            if (!items) return;
            const col = el('div', 'col', '<h3>' + esc(label) + '</h3>');
            const ul = el('ul', 'ticks');
            items.forEach(i => ul.appendChild(el('li', null, esc(i))));
            col.appendChild(ul);
            two.appendChild(col);
        });
        d.appendChild(two);
        app.appendChild(d);
    }
    if (cfg.threeRules && cfg.threeRules.length) {
        const d = foldable('The three rules', '');
        const ol = el('ol', 'rules');
        cfg.threeRules.forEach(r => ol.appendChild(el('li', null, esc(r))));
        d.appendChild(ol);
        app.appendChild(d);
    }
    if (cfg.weekend) {
        const d = foldable('Weekend protocol', 'Sat hard body · Sun deep brain');
        if (cfg.weekend.saturday) d.appendChild(el('p', null, '<b>Saturday.</b> ' + esc(cfg.weekend.saturday)));
        if (cfg.weekend.sunday) d.appendChild(el('p', null, '<b>Sunday.</b> ' + esc(cfg.weekend.sunday)));
        app.appendChild(d);
    }
    if (cfg.notes && cfg.notes.length) {
        const d = foldable('Sleep, deload & autoregulation', '');
        const ul = el('ul', 'ticks');
        cfg.notes.forEach(n => ul.appendChild(el('li', null, esc(n))));
        d.appendChild(ul);
        app.appendChild(d);
    }
}

// ---------- live prayer countdown ----------
function startCountdown() {
    if (countdownTimer) clearInterval(countdownTimer);
    updateCountdown();
    countdownTimer = setInterval(updateCountdown, 20000);
}
function updateCountdown() {
    if (!CONFIG || !CONFIG.prayerTimes) return;
    const p = CONFIG.prayerTimes, now = nowMin();
    let next = null;
    for (const [name, key] of PRAYERS) {
        if (p[key] && toMin(p[key]) > now) { next = { name, key, min: toMin(p[key]), tomorrow: false }; break; }
    }
    if (!next && p.fajr) next = { name: 'Fajr', key: 'fajr', min: toMin(p.fajr), tomorrow: true };
    const label = $('np-label'), count = $('np-count');
    if (next && label) {
        const remaining = next.tomorrow ? (1440 - now + next.min) : (next.min - now);
        label.innerHTML = '<span class="np-dot"></span> Next · <b>' + esc(next.name) + '</b> ' + esc(p[next.key]);
        count.textContent = 'in ' + fmtDur(remaining);
        document.querySelectorAll('.prayer').forEach(c => {
            c.classList.remove('is-next', 'is-past');
            const k = c.dataset.key;
            if (k === next.key && !next.tomorrow) c.classList.add('is-next');
            else if (k && k !== 'sunrise' && p[k] && toMin(p[k]) < now) c.classList.add('is-past');
        });
    }
}

// ---------- geolocation -> calculate prayer times ----------
function useMyLocation() {
    if (!navigator.geolocation) { toast('Geolocation is not available in this browser', 'err'); return; }
    toast('Locating… allow the permission prompt');
    navigator.geolocation.getCurrentPosition(async (pos) => {
        const lat = pos.coords.latitude, lng = pos.coords.longitude;
        const tz = -new Date().getTimezoneOffset();          // minutes east of UTC
        const date = new Date().toISOString().slice(0, 10);  // local-ish yyyy-mm-dd
        try {
            // best-effort place name, then remember this location server-side
            let name = lat.toFixed(3) + ', ' + lng.toFixed(3);
            try {
                const rev = await (await fetch(`/api/geocode/reverse?lat=${lat}&lng=${lng}`)).json();
                if (rev && rev.name) name = rev.name;
            } catch (ignore) { /* offline: keep coords as the name */ }
            await fetch('/api/location', {
                method: 'PUT', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ placeName: name, lat, lng, tzOffset: tz })
            });
            const res = await fetch(`/api/prayer-times/calculate?lat=${lat}&lng=${lng}&tz=${tz}&date=${date}`);
            if (!res.ok) throw new Error('HTTP ' + res.status);
            showCalcResult(await res.json(), name);
        } catch (e) { toast('Could not calculate: ' + e.message, 'err'); }
    }, (err) => {
        toast(err.code === 1 ? 'Location permission denied — you can still edit times by hand' : 'Could not get location', 'err');
    }, { timeout: 10000, maximumAge: 600000 });
}

function showCalcResult(pt, name) {
    const body = el('div');
    body.appendChild(el('p', 'subtle', 'Computed for ' + esc(name || 'your location') + ' (Karachi 18°, Hanafi Asr):'));
    const row = el('div', 'prayer-row');
    [['Fajr', pt.fajr], ['Sunrise', pt.sunrise], ['Dhuhr', pt.dhuhr], ['Asr', pt.asr], ['Maghrib', pt.maghrib], ['Isha', pt.isha]]
        .forEach(([n, t]) => row.appendChild(el('div', 'prayer', '<span class="prayer-name">' + n + '</span><span class="prayer-time">' + esc(t) + '</span>')));
    body.appendChild(row);
    const actions = el('div', 'modal-actions');
    const save = el('button', null, 'Save to my routine');
    save.onclick = async () => {
        try {
            const r = await fetch('/api/prayer-times', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(pt) });
            if (!r.ok) throw new Error('HTTP ' + r.status);
            closeModal();
            toast('Saved ✓', 'ok');
            const cfg = await (await fetch('/api/config')).json();
            render(cfg);
        } catch (e) { toast('Save failed: ' + e.message, 'err'); }
    };
    const cancel = el('button', 'secondary', 'Not now');
    cancel.onclick = closeModal;
    actions.appendChild(save); actions.appendChild(cancel);
    body.appendChild(actions);
    openModal('Prayer times from your location', body);
}

// ---------- day detail modal ----------
function openDayModal(g, m, isToday) {
    const body = el('div');
    body.appendChild(el('p', 'modal-sub', '🥊 ' + esc(g.discipline)));
    body.appendChild(el('p', null, '🍳 <b>Breakfast</b> · eggs in ghee + pumpkin seeds'));
    if (m) {
        body.appendChild(el('p', null, '🍽️ <b>Lunch</b> · ' + esc(m.lunch)));
        body.appendChild(el('p', null, '🐟 <b>Dinner</b> · ' + esc(m.dinner)));
        if (m.note) body.appendChild(el('p', 'subtle', esc(m.note)));
    }
    if (isToday && CONFIG && CONFIG.schedule) {
        const h = el('p', 'modal-sub', 'Today’s timeline');
        body.appendChild(h);
        const tl = el('div', 'timeline');
        CONFIG.schedule.forEach(it => tl.appendChild(el('div', 'tl-item cat-' + esc(it.category || 'admin'),
            '<span class="tl-time">' + esc(it.time) + '</span><div class="tl-body"><span class="tl-title">' + esc(it.title) + '</span></div>')));
        body.appendChild(tl);
    }
    openModal(g.day + (isToday ? ' · today' : ''), body);
}

// ---------- modal + toast ----------
function openModal(title, contentNode) {
    closeModal();
    const overlay = el('div', 'modal-overlay');
    overlay.id = 'modal';
    const panel = el('div', 'modal-panel');
    const head = el('div', 'modal-head', '<h2>' + esc(title) + '</h2>');
    const x = el('button', 'modal-x', '✕'); x.onclick = closeModal; head.appendChild(x);
    panel.appendChild(head);
    panel.appendChild(contentNode);
    overlay.appendChild(panel);
    overlay.onclick = (e) => { if (e.target === overlay) closeModal(); };
    document.body.appendChild(overlay);
    document.addEventListener('keydown', escClose);
}
function closeModal() { const m = $('modal'); if (m) m.remove(); document.removeEventListener('keydown', escClose); }
function escClose(e) { if (e.key === 'Escape') closeModal(); }

function toast(msg, kind) {
    let t = $('toast');
    if (!t) { t = el('div', 'toast'); t.id = 'toast'; document.body.appendChild(t); }
    t.textContent = msg;
    t.className = 'toast show ' + (kind || '');
    clearTimeout(t._timer);
    t._timer = setTimeout(() => { t.className = 'toast ' + (kind || ''); }, 2600);
}

// ---------- boot ----------
fetch('/api/config')
    .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then(render)
    .catch(err => { $('app').innerHTML = '<p class="loading">Could not load /api/config: ' + esc(err.message) + '</p>'; });

// PWA: register the service worker for offline support (no-op if unsupported)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => navigator.serviceWorker.register('/service-worker.js').catch(() => {}));
}
