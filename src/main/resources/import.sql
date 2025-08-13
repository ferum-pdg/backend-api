CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE goal
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO Account (id, email, password, first_name, last_name, phone_number, birth_date, weight, height, fcmax) VALUES
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440001', 'alice@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Alice', 'Martin', '+41 79 123 45 67', '1990-05-15', 65.0, 170.0, 190),
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440002', 'bob@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Bob', 'Dupont', '+41 79 987 65 43', '1985-08-22', 80.0, 185.0, 185),
                                                                                                                      ('550e8400-e29b-41d4-a716-446655440003', 'charlie@example.com', '$2a$10$8dEnpqQxTmQJIZTe7Y21dOEf0jIHnmVghH6fnL5MD6jqPW5Jl7s7G', 'Charlie', 'Rousseau', '+41 79 555 77 88', '1992-12-03', 58.0, 162.0, 195);

INSERT INTO goal (name, elevation_gain, nb_of_week, nb_of_workouts_per_week, target_distance, sport)
VALUES
    ('5km', 50,8, 2, 5, 'RUNNING'),
    ('10km', 100, 8, 2, 10, 'RUNNING'),
    ('Half-marathon', 150, 12, 3, 21.1, 'RUNNING'),
    ('Marathon', 200, 20, 4, 42.2, 'RUNNING'),
    ('Cycling 20km', 300, 8, 2,20, 'CYCLING'),
    ('Cycling 40km', 600, 8, 2,40, 'CYCLING'),
    ('Cycling 100km', 1000, 12, 4,100, 'CYCLING'),
    ('Cycling 180km', 1500, 16, 5,180, 'CYCLING'),
    ('Swimming 500m', 0, 8, 2, 0.5, 'SWIMMING'),
    ('Swimming 1000m', 0, 8, 2, 1.0, 'SWIMMING'),
    ('Swimming 1800m', 0, 10, 3, 1.8, 'SWIMMING'),
    ('Swimming 3900m', 0, 10, 4, 3.9, 'SWIMMING');

