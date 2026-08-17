-- ============================================================
-- Migration V4: Add phone number support to users table
-- ============================================================

IF NOT EXISTS (
    SELECT * FROM sys.columns
    WHERE object_id = OBJECT_ID('users') AND name = 'phone'
)
BEGIN
    ALTER TABLE users
    ADD phone NVARCHAR(50) NULL;
END;
GO

-- Partial unique index: phone must be unique when not null
IF NOT EXISTS (
    SELECT * FROM sys.indexes WHERE name = 'idx_users_phone'
)
BEGIN
    CREATE UNIQUE INDEX idx_users_phone
    ON users (phone)
    WHERE phone IS NOT NULL;
END;
GO
