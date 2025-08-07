CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==========================================
-- SCRIPT DE PEUPLEMENT COMPLET
-- ==========================================

-- 1. INSERTION DES OBJECTIFS (GOALS)
-- ==========================================
INSERT INTO goal (id, name, elevation_gain, nb_of_week, nb_of_workouts_per_week, target_distance, sport)
VALUES
    (gen_random_uuid(), '5km', 50, 8, 2, 5, 'RUNNING'),
    (gen_random_uuid(), '10km', 100, 8, 2, 10, 'RUNNING'),
    (gen_random_uuid(), 'Half-marathon', 1500, 12, 3, 21.1, 'RUNNING'),
    (gen_random_uuid(), 'Marathon', 200, 20, 4, 42.2, 'RUNNING'),
    (gen_random_uuid(), '50km Cycling', 500, 6, 3, 50, 'CYCLING'),
    (gen_random_uuid(), '100km Cycling', 1000, 10, 4, 100, 'CYCLING'),
    (gen_random_uuid(), '1km Swimming', 0, 4, 2, 1, 'SWIMMING'),
    (gen_random_uuid(), '5km Swimming', 0, 8, 3, 5, 'SWIMMING');

-- 2. INSERTION DES COMPTES UTILISATEURS (ACCOUNTS)
-- ==========================================
INSERT INTO account (id, email, first_name, last_name, password, phone_number, birth_date, height, weight, fcmax)
VALUES
    (gen_random_uuid(), 'alice.martin@email.com', 'Alice', 'Martin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO.8xsgkbvVBbsF8bZI2', '+33123456789', '1990-05-15', 165.0, 60.0, 185),
    (gen_random_uuid(), 'bob.dupont@email.com', 'Bob', 'Dupont', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO.8xsgkbvVBbsF8bZI2', '+33987654321', '1985-09-22', 180.0, 75.0, 190),
    (gen_random_uuid(), 'claire.bernard@email.com', 'Claire', 'Bernard', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO.8xsgkbvVBbsF8bZI2', '+33456789123', '1992-12-03', 170.0, 65.0, 180),
    (gen_random_uuid(), 'david.moreau@email.com', 'David', 'Moreau', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO.8xsgkbvVBbsF8bZI2', '+33789123456', '1988-03-18', 175.0, 70.0, 195);

-- 3. INSERTION DES NIVEAUX DE FITNESS
-- ==========================================
INSERT INTO fitnesslevel (id, account_id, date, fitness_score)
VALUES
    -- Alice Martin
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), '2024-01-01', 65),

    -- Bob Dupont
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'bob.dupont@email.com'), '2024-01-01', 85),

    -- Claire Bernard
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'claire.bernard@email.com'), '2024-01-01', 45),

    -- David Moreau
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'david.moreau@email.com'), '2024-03-01', 78);

-- 4. INSERTION DES PLANS D'ENTRAINEMENT
-- ==========================================
INSERT INTO trainingplan (id, start_date, end_date)
VALUES
    (gen_random_uuid(), '2024-01-01', '2024-02-26'),  -- Plan 8 semaines pour Alice
    (gen_random_uuid(), '2024-02-01', '2024-04-25'),  -- Plan 12 semaines pour Bob
    (gen_random_uuid(), '2024-03-01', '2024-04-26'),  -- Plan 8 semaines pour Claire
    (gen_random_uuid(), '2024-01-15', '2024-06-15');  -- Plan 20 semaines pour David

-- 5. INSERTION DES JOURS D'ENTRAINEMENT
-- ==========================================
INSERT INTO trainingplan_daysofweek (trainingplan_id, days_of_week)
VALUES
    -- Plan Alice (2x/semaine : Mardi, Samedi)
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 'TUESDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 'SATURDAY'),

    -- Plan Bob (3x/semaine : Lundi, Mercredi, Vendredi)
    ((SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 'MONDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 'WEDNESDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 'FRIDAY'),

    -- Plan Claire (2x/semaine : Mercredi, Dimanche)
    ((SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), 'WEDNESDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), 'SUNDAY'),

    -- Plan David (4x/semaine : Mardi, Jeudi, Samedi, Dimanche)
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 'TUESDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 'THURSDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 'SATURDAY'),
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 'SUNDAY');

-- 6. INSERTION DES SORTIES LONGUES
-- ==========================================
INSERT INTO trainingplan_longoutgoing (trainingplan_id, long_outgoing)
VALUES
    -- Plan Alice : sortie longue le samedi
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 'SATURDAY'),

    -- Plan Bob : sortie longue le vendredi
    ((SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 'FRIDAY'),

    -- Plan Claire : sortie longue le dimanche
    ((SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), 'SUNDAY'),

    -- Plan David : sortie longue le dimanche
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 'SUNDAY');

-- 7. ASSOCIATION PLANS-OBJECTIFS
-- ==========================================
INSERT INTO training_plan_goals (training_plan_id, goal_id)
VALUES
    -- Alice vise le 5km
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), (SELECT id FROM goal WHERE name = '5km')),

    -- Bob vise le Half-marathon
    ((SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), (SELECT id FROM goal WHERE name = 'Half-marathon')),

    -- Claire vise le 10km
    ((SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), (SELECT id FROM goal WHERE name = '10km')),

    -- David vise le Marathon
    ((SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), (SELECT id FROM goal WHERE name = 'Marathon'));

-- 8. INSERTION DES WORKOUTS
-- ==========================================

-- Workouts d'Alice (Course à pied - Objectif 5km)
INSERT INTO workout (id, account_id, trainingplan_id, sport, start_time, end_time, duration_sec, distance_meters, average_speed, calories_kcal, avg_heart_rate, max_heart_rate, source, status)
VALUES
    -- Semaine 1
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 0, '2024-01-02 18:00:00+01', '2024-01-02 18:25:00+01', 1500, 2500, 6.0, 180, 155, 170, 'Manual', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 0, '2024-01-06 09:00:00+01', '2024-01-06 09:35:00+01', 2100, 3500, 6.0, 220, 150, 165, 'Manual', 'COMPLETED'),

    -- Semaine 2
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 0, '2024-01-09 18:00:00+01', '2024-01-09 18:28:00+01', 1680, 2800, 6.0, 195, 152, 168, 'Manual', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 0, '2024-01-13 09:00:00+01', '2024-01-13 09:40:00+01', 2400, 4000, 6.0, 250, 148, 163, 'Manual', 'COMPLETED'),

    -- Workout planifié
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'alice.martin@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-01'), 0, '2024-08-07 18:00:00+01', '2024-08-07 18:30:00+01', 1800, 3000, 6.0, 210, 146, 177, 'Manual', 'PLANNED');

-- Workouts de Bob (Course à pied - Objectif Half-marathon)
INSERT INTO workout (id, account_id, trainingplan_id, sport, start_time, end_time, duration_sec, distance_meters, average_speed, calories_kcal, avg_heart_rate, max_heart_rate, source, status)
VALUES
    -- Semaine 1
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'bob.dupont@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 0, '2024-02-05 07:00:00+01', '2024-02-05 07:45:00+01', 2700, 6000, 8.0, 420, 165, 180, 'Strava', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'bob.dupont@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 0, '2024-02-07 18:30:00+01', '2024-02-07 19:15:00+01', 2700, 6000, 8.0, 420, 168, 182, 'Strava', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'bob.dupont@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 0, '2024-02-09 08:00:00+01', '2024-02-09 09:30:00+01', 5400, 12000, 8.0, 650, 162, 175, 'Strava', 'COMPLETED'),

    -- Workout avorté
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'bob.dupont@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-02-01'), 0, '2024-02-12 18:30:00+01', '2024-02-12 18:45:00+01', 900, 1500, 6.0, 90, 170, 185, 'Strava', 'ABORTED');

-- Workouts de Claire (Course à pied - Objectif 10km)
INSERT INTO workout (id, account_id, trainingplan_id, sport, start_time, end_time, duration_sec, distance_meters, average_speed, calories_kcal, avg_heart_rate, max_heart_rate, source, status)
VALUES
    -- Première semaine
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'claire.bernard@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), 0, '2024-03-06 19:00:00+01', '2024-03-06 19:20:00+01', 1200, 2000, 6.0, 140, 145, 160, 'Garmin', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'claire.bernard@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-03-01'), 0, '2024-03-10 10:00:00+01', '2024-03-10 10:25:00+01', 1500, 2500, 6.0, 175, 142, 158, 'Garmin', 'COMPLETED');

-- Workouts de David (Course à pied - Objectif Marathon)
INSERT INTO workout (id, account_id, trainingplan_id, sport, start_time, end_time, duration_sec, distance_meters, average_speed, calories_kcal, avg_heart_rate, max_heart_rate, source, status)
VALUES
    -- Entrainements variés
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'david.moreau@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 0, '2024-01-16 06:30:00+01', '2024-01-16 07:15:00+01', 2700, 7000, 9.3, 480, 158, 175, 'Polar', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'david.moreau@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 0, '2024-01-18 18:00:00+01', '2024-01-18 18:40:00+01', 2400, 6000, 9.0, 420, 160, 178, 'Polar', 'COMPLETED'),
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'david.moreau@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 0, '2024-01-20 08:00:00+01', '2024-01-20 08:50:00+01', 3000, 8000, 9.6, 550, 155, 172, 'Polar', 'COMPLETED'),
    -- Sortie longue du dimanche
    (gen_random_uuid(), (SELECT id FROM account WHERE email = 'david.moreau@email.com'), (SELECT id FROM trainingplan WHERE start_date = '2024-01-15'), 0, '2024-01-21 09:00:00+01', '2024-01-21 11:15:00+01', 8100, 18000, 8.0, 980, 152, 168, 'Polar', 'COMPLETED');
