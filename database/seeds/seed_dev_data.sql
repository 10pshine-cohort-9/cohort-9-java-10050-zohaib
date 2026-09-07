-- ============================================================
-- Seed: Development test data
-- Run ONLY in dev/test environments — never in production
-- ============================================================

-- Passwords below are bcrypt hashes of 'Password123!'
-- Generate fresh hashes before using in any real environment

INSERT INTO users (first_name, last_name, email, password, role, is_active)
VALUES
    ('Admin',   'User',   'admin@example.com', '$2a$10$placeholder_hash_replace_me', 'ROLE_ADMIN', 1),
    ('John',    'Doe',    'john@example.com',  '$2a$10$placeholder_hash_replace_me', 'ROLE_USER',  1),
    ('Jane',    'Smith',  'jane@example.com',  '$2a$10$placeholder_hash_replace_me', 'ROLE_USER',  1);

-- Seed contacts for user ID 2 (John Doe) — adjust IDs after actual insert
INSERT INTO contacts (user_id, first_name, last_name, email, phone, company, notes)
VALUES
    (2, 'Alice',   'Johnson', 'alice@example.com',  '+1-555-0101', 'Acme Corp',    'Met at conference'),
    (2, 'Bob',     'Williams','bob@example.com',    '+1-555-0102', 'Globex Inc',   NULL),
    (2, 'Charlie', 'Brown',   'charlie@example.com','+1-555-0103', 'Initech',      'Old college friend');
