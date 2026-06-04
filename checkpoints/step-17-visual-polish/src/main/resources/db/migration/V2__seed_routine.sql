-- Flyway migration V2: seed the real routine.
--
-- This replaces the Java DataSeeder from steps 06-09. The advantage: the seed is now versioned and
-- travels with the schema, so a brand-new database (yours, a teammate's, CI's, production's) comes up
-- already filled with the routine - no extra code path, no "is it empty?" check. Like all migrations
-- it runs exactly once; later edits made through the API are just UPDATEs on top of these rows.
--
-- A SQL note: a literal apostrophe inside a string is written as two single quotes ('') - see the
-- "paneer''s" / "spinach''s" below.

INSERT INTO app_meta (id, title, tagline, month_label)
VALUES (1, 'Sahar', 'recover, build, fight', 'June 2026');

INSERT INTO prayer_times (id, fajr, sunrise, dhuhr, asr, maghrib, isha, method_note)
VALUES (1, '04:37', '05:59', '12:37', '17:12', '19:13', '20:36',
        'Karachi 18° Fajr/Isha, Hanafi Asr, computed for Thane (19.22N, 72.98E). Recheck monthly; drifts under ~10 min across a month.');

INSERT INTO blocks (id, label)
VALUES (1, '4-week block · 8 Jun – 5 Jul (Foundation · Build · Peak · Deload)');

INSERT INTO weeks (block_id, ordinal, name, start_date, end_date, training_focus, backend_focus, deload) VALUES
(1, 1, 'Foundation', '8 Jun',  '14 Jun', 'Moderate, no PM sessions; lock the schedule and the journaling habit.', 'Spring Boot core: setup, dependency injection, controllers, a CRUD REST API.', FALSE),
(1, 2, 'Build',      '15 Jun', '21 Jun', 'Add PM strength Mon and Thu; deeper deep-work; volume climbs.',          'Persistence: Spring Data JPA, Postgres, repositories, validation.',          FALSE),
(1, 3, 'Peak',       '22 Jun', '28 Jun', 'Full volume, sharpest spar, deepest learning.',                          'Docker and DevOps: Dockerfile, compose, env config, basic CI.',              FALSE),
(1, 4, 'Deload',     '29 Jun', '5 Jul',  'MMA down ~40%, light or skipped spar, weekday mains drilling only, more sleep.', 'Ship it: deploy the container, refactor, docs.',                     TRUE);

INSERT INTO schedule_items (slot_time, title, detail, category, day_type, sort_order) VALUES
('04:15', 'Wake, water, wudu, Tahajjud, Fajr', '500ml water · 20-min Tahajjud',                    'pray',    'weekday', 0),
('04:50', 'Morning bullet-journal log',        '5–10 min',                                          'journal', 'weekday', 1),
('05:00', 'Deep work — backend',               '120 min · backend focus per the current week',      'work',    'weekday', 2),
('07:30', 'MMA AM session',                    'discipline per the weekly grid',                    'train',   'weekday', 3),
('08:45', 'Shower, breakfast, B12',            'eggs in ghee + pumpkin seeds · Nurokind-OD B12',    'meal',    'weekday', 4),
('09:30', 'Buffer — mobility & admin',         NULL,                                                'admin',   'weekday', 5),
('11:15', 'Commute',                           NULL,                                                'admin',   'weekday', 6),
('11:30', 'Work block 1',                      'Dhuhr enters 12:37',                                'work',    'weekday', 7),
('13:30', 'Home, lunch, Qailulah nap',         '20–30 min nap · load-bearing',                      'meal',    'weekday', 8),
('14:20', 'Work block 2',                      'Asr 17:12',                                         'work',    'weekday', 9),
('18:20', 'Optional PM session',               'Mon & Thu, Build/Peak weeks only · first to cut when tired', 'train', 'weekday', 10),
('19:13', 'Maghrib',                           NULL,                                                'pray',    'weekday', 11),
('19:30', 'Light dinner',                      'fish / prawns / light · ~2h before sleep',          'meal',    'weekday', 12),
('20:36', 'Isha',                              NULL,                                                'pray',    'weekday', 13),
('20:50', 'Magnesium — Mgmax 400',             '~30 min pre-sleep',                                 'supp',    'weekday', 14),
('21:00', 'Night bullet-journal log',          '5–10 min',                                          'journal', 'weekday', 15),
('21:20', 'Lights out',                        '~7h sleep',                                         'sleep',   'weekday', 16);

INSERT INTO weekly_grid (day_of_week, discipline, note, sort_order) VALUES
('Mon', 'Muay Thai (technical)',          NULL, 0),
('Tue', 'Boxing',                         NULL, 1),
('Wed', 'Wrestling / BJJ (light)',        NULL, 2),
('Thu', 'Muay Thai',                      NULL, 3),
('Fri', 'Padwork + light drills (taper)', NULL, 4),
('Sat', 'SPAR + BJJ / MMA rounds',        NULL, 5),
('Sun', 'Rest / mobility walk',           NULL, 6);

INSERT INTO supplements (time_label, name, dose, purpose, note, sort_order) VALUES
('05:00',                 'Creatine + raw beetroot',                 '3–5 g',     'brain ATP + nitrates',       NULL, 0),
('~09:00 with breakfast', 'Nurokind-OD B12',                         '1500 mcg',  'myelin maintenance',         'not for cramps', 1),
('20:50',                 'Mgmax 400 (magnesium bisglycinate)',      '400 mg',    'pre-sleep CNS down-shift',   '~30 min before lights-out', 2),
('Sunday',                'Uprise-D3 60K',                           '60,000 IU', 'vitamin D repletion',        'With a fatty meal. 60K weekly is a repletion dose, not maintenance; retest at 8 weeks; past 50–60 ng/mL drop to 60K monthly. Open adds: omega-3 (or oily fish 2–3x/week), K2 MK-7 to pair with D3, collagen + vitamin C pre-training.', 3);

INSERT INTO diet_sections (ordinal, title, body) VALUES
(1, 'Breakfast',               '3–4 whole eggs in ghee + raw pumpkin seeds.'),
(2, 'Lunch (heavy)',           'Rotate beef/mutton (~2x/week) and chicken (~2–3x) with leafy greens; batch-cooks well.'),
(3, 'Dinner (light)',          'Oily fish, prawns, white fish, or light chicken; digests faster than red meat, protects deep sleep.'),
(4, 'Organ meats',             'Heart: eat freely (CoQ10, taurine, B12, lean). Liver: cap at ~100g once a week (vitamin A). Kidney: once a week (selenium, B12). Brain: small prion risk and you guard your CNS, so get DHA from fish; bheja occasionally only.'),
(5, 'Vegetables (the minimum)','Organs cover most micronutrients, so veg is for fibre and vitamin C. Hide it in stews; palak paneer (the paneer''s calcium binds spinach''s oxalate, lower stone risk); rotate greens.'),
(6, 'Omega-3 from food',       'Oily fish 2–3x/week; bangda (mackerel) is cheapest and highest, then surmai and rawas. Batch rule: batch red meat and chicken; cook fish and prawns fresh.');
