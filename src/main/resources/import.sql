-- import.sql
-- À placer dans src/main/resources/import.sql
-- Mots de passe hachés avec BCrypt (salt rounds = 12)

-- Comptes avec mots de passe hachés
-- password123 -> $2a$12$rOgK8K8K8K8K8K8K8K8K8e7vJ9J9J9J9J9J9J9J9J9J9J9J9J9J9J9
-- password456 -> $2a$12$sPhL9L9L9L9L9L9L9L9L9f8wK0K0K0K0K0K0K0K0K0K0K0K0K0K0K0
-- password789 -> $2a$12$tQiM1M1M1M1M1M1M1M1M1g9xL1L1L1L1L1L1L1L1L1L1L1L1L1L1L1

INSERT INTO Account (id, email, password, first_name, last_name, phone_number, birth_date, weight, height, fcmax) VALUES
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440001', 'alice@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Alice', 'Martin', '+41 79 123 45 67', '1990-05-15', 65.0, 170.0, 190),
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440002', 'bob@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Bob', 'Dupont', '+41 79 987 65 43', '1985-08-22', 80.0, 185.0, 185),
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440003', 'charlie@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Charlie', 'Rousseau', '+41 79 555 77 88', '1992-12-03', 58.0, 162.0, 195);

-- Niveaux de fitness
INSERT INTO FitnessLevel (id, account_id, date, fitness_score) VALUES
                                                                   ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', '2025-07-25', 7),
                                                                   ('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440002', '2025-08-01', 5),
                                                                   ('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440003', '2025-08-05', 8);

-- Objectifs
INSERT INTO Goal (id, sport, name, nb_of_workouts_per_week, nb_of_week, target_distance, elevation_gain) VALUES
                                                                                                             ('550e8400-e29b-41d4-a716-446655440021', 'RUNNING', 'Préparation Marathon', 4, 16, 42195.0, 500.0),
                                                                                                             ('550e8400-e29b-41d4-a716-446655440022', 'CYCLING', 'Sortie vélo 100km', 3, 8, 100000.0, 1200.0),
                                                                                                             ('550e8400-e29b-41d4-a716-446655440023', 'SWIMMING', 'Améliorer technique natation', 3, 12, 2000.0, 0.0),
                                                                                                             ('550e8400-e29b-41d4-a716-446655440024', 'RUNNING', 'Semi-marathon', 3, 10, 21097.5, 300.0);

-- Plans d'entraînement
INSERT INTO TrainingPlan (id, account_id, start_date, end_date) VALUES
                                                                    ('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440001', '2025-07-11', '2025-11-02'),
                                                                    ('550e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440002', '2025-07-25', '2025-09-19'),
                                                                    ('550e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440003', '2025-08-01', '2025-10-17');

-- Relations Plans-Objectifs
INSERT INTO training_plan_goals (training_plan_id, goal_id) VALUES
                                                                ('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440021'),
                                                                ('550e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440022'),
                                                                ('550e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440023'),
                                                                ('550e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440024');

-- Jours d'entraînement pour Alice (Marathon)
INSERT INTO TrainingPlan_daysOfWeek (TrainingPlan_id, days_of_week) VALUES
                                                                        ('550e8400-e29b-41d4-a716-446655440031', 'MONDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440031', 'WEDNESDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440031', 'FRIDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440031', 'SUNDAY');

-- Jours d'entraînement pour Bob (Cyclisme)
INSERT INTO TrainingPlan_daysOfWeek (TrainingPlan_id, days_of_week) VALUES
                                                                        ('550e8400-e29b-41d4-a716-446655440032', 'TUESDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440032', 'THURSDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440032', 'SATURDAY');

-- Jours d'entraînement pour Charlie (Natation + Course)
INSERT INTO TrainingPlan_daysOfWeek (TrainingPlan_id, days_of_week) VALUES
                                                                        ('550e8400-e29b-41d4-a716-446655440033', 'MONDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440033', 'WEDNESDAY'),
                                                                        ('550e8400-e29b-41d4-a716-446655440033', 'FRIDAY');

-- Sorties longues
INSERT INTO TrainingPlan_longOutgoing (TrainingPlan_id, long_outgoing) VALUES
                                                                           ('550e8400-e29b-41d4-a716-446655440031', 'SUNDAY'),
                                                                           ('550e8400-e29b-41d4-a716-446655440032', 'SATURDAY'),
                                                                           ('550e8400-e29b-41d4-a716-446655440033', 'FRIDAY');

-- Séances d'entraînement
-- Alice - Course à pied
INSERT INTO Workout (id, account_id, training_plan_id, sport, start_time, end_time, duration_sec, distance_meters, calories_kcal, avg_heart_rate, max_heart_rate, average_speed, source, status) VALUES
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440041', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440031', 'RUNNING', '2025-08-03 07:00:00+02:00', '2025-08-03 08:15:00+02:00', 4500, 12000.0, 650.0, 155, 175, 9.6, 'Garmin', 'COMPLETED'),
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440042', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440031', 'RUNNING', '2025-08-05 18:30:00+02:00', '2025-08-05 19:45:00+02:00', 4500, 10000.0, 580.0, 160, 180, 8.0, 'Garmin', 'COMPLETED'),
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440043', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440031', 'RUNNING', '2025-08-09 07:00:00+02:00', NULL, 0, 0.0, 0.0, 0, 0, NULL, 'Planned', 'PLANNED');

-- Bob - Cyclisme
INSERT INTO Workout (id, account_id, training_plan_id, sport, start_time, end_time, duration_sec, distance_meters, calories_kcal, avg_heart_rate, max_heart_rate, average_speed, source, status) VALUES
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440044', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440032', 'CYCLING', '2025-08-04 09:00:00+02:00', '2025-08-04 11:30:00+02:00', 9000, 65000.0, 1200.0, 140, 165, 26.0, 'Wahoo', 'COMPLETED'),
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440045', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440032', 'CYCLING', '2025-08-07 14:00:00+02:00', '2025-08-07 15:00:00+02:00', 3600, 30000.0, 450.0, 135, 155, 30.0, 'Wahoo', 'COMPLETED');

-- Charlie - Natation et Course
INSERT INTO Workout (id, account_id, training_plan_id, sport, start_time, end_time, duration_sec, distance_meters, calories_kcal, avg_heart_rate, max_heart_rate, average_speed, source, status) VALUES
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440046', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440033', 'SWIMMING', '2025-08-06 12:00:00+02:00', '2025-08-06 13:00:00+02:00', 3600, 1500.0, 420.0, 145, 170, 1.5, 'Polar', 'COMPLETED'),
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440047', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440033', 'RUNNING', '2025-08-02 19:00:00+02:00', '2025-08-02 20:00:00+02:00', 3600, 8000.0, 480.0, 150, 175, 8.0, 'Polar', 'COMPLETED'),
                                                                                                                                                                                                     ('550e8400-e29b-41d4-a716-446655440048', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440033', 'RUNNING', '2025-08-07 07:00:00+02:00', '2025-08-07 07:15:00+02:00', 900, 1000.0, 60.0, 140, 160, 4.0, 'Polar', 'ABORTED');