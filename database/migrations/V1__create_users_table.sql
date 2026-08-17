-- ============================================================
-- Migration V1: Create users table
-- ============================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        first_name   NVARCHAR(100)  NOT NULL,
        last_name    NVARCHAR(100)  NOT NULL,
        email        NVARCHAR(255)  NOT NULL UNIQUE,
        password     NVARCHAR(255)  NOT NULL,
        role         NVARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER',
        is_active    BIT            NOT NULL DEFAULT 1,
        created_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
        updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME()
    );

    CREATE INDEX idx_users_email ON users (email);
END;
