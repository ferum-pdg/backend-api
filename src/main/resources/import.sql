CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE goal
    ALTER COLUMN id SET DEFAULT gen_random_uuid();


INSERT INTO goal (name, elevation_gain, nb_of_week, nb_of_workouts_per_week, target_distance, sport)
VALUES
    ('5km', 50,8, 2, 5, 'RUNNING'),
    ('10km', 100, 8, 2, 10, 'RUNNING'),
    ('Half-marathon', 1500, 12, 3, 21.1, 'RUNNING'),
    ('Marathon', 200, 20, 4, 42.2, 'RUNNING');
