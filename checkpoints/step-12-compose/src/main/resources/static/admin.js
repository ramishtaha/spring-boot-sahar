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

// Pull the validation messages out of our 400 ApiError shape, if present.
function errorText(res) {
    if (res.body && Array.isArray(res.body.messages)) return res.body.messages.join('; ');
    if (res.body && res.body.message) return res.body.message;     // ResponseStatusException reason
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

load();
