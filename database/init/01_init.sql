-- ============================================================
-- Docker init script — runs automatically on first container start
-- Idempotent: safe to run multiple times
-- ============================================================

SET QUOTED_IDENTIFIER ON;
GO

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'contact_management_db')
BEGIN
    CREATE DATABASE contact_management_db
    COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Database contact_management_db created.';
END
ELSE
    PRINT 'Database contact_management_db already exists.';
GO

USE contact_management_db;
GO

-- ── V1: users ─────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        first_name   NVARCHAR(100)  NOT NULL,
        last_name    NVARCHAR(100)  NOT NULL,
        email        NVARCHAR(255)  UNIQUE,
        password     NVARCHAR(255)  NOT NULL,
        role         NVARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER',
        is_active    BIT            NOT NULL DEFAULT 1,
        created_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
        updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME()
    );
    CREATE INDEX idx_users_email ON users (email);
    PRINT 'Table users created.';
END
GO

-- ── V2: contacts ──────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'contacts')
BEGIN
    CREATE TABLE contacts (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id      BIGINT         NOT NULL,
        first_name   NVARCHAR(100)  NOT NULL,
        last_name    NVARCHAR(100)  NOT NULL,
        email        NVARCHAR(255),
        phone        NVARCHAR(50),
        address      NVARCHAR(500),
        company      NVARCHAR(255),
        notes        NVARCHAR(MAX),
        is_deleted   BIT            NOT NULL DEFAULT 0,
        created_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
        updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_contacts_user
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );
    CREATE INDEX idx_contacts_user_id   ON contacts (user_id);
    CREATE INDEX idx_contacts_last_name ON contacts (last_name);
    PRINT 'Table contacts created.';
END
GO

-- ── V3: audit triggers ────────────────────────────────────
IF OBJECT_ID('trg_users_updated_at', 'TR') IS NULL
BEGIN
    EXEC('CREATE TRIGGER trg_users_updated_at ON users AFTER UPDATE AS
    BEGIN
        SET NOCOUNT ON;
        UPDATE users SET updated_at = SYSDATETIME()
        FROM users u INNER JOIN inserted i ON u.id = i.id;
    END');
    PRINT 'Trigger trg_users_updated_at created.';
END
GO

IF OBJECT_ID('trg_contacts_updated_at', 'TR') IS NULL
BEGIN
    EXEC('CREATE TRIGGER trg_contacts_updated_at ON contacts AFTER UPDATE AS
    BEGIN
        SET NOCOUNT ON;
        UPDATE contacts SET updated_at = SYSDATETIME()
        FROM contacts c INNER JOIN inserted i ON c.id = i.id;
    END');
    PRINT 'Trigger trg_contacts_updated_at created.';
END
GO

-- ── V4: phone column on users ─────────────────────────────
IF NOT EXISTS (
    SELECT * FROM sys.columns
    WHERE object_id = OBJECT_ID('users') AND name = 'phone'
)
BEGIN
    ALTER TABLE users ADD phone NVARCHAR(50) NULL;
    PRINT 'Column users.phone added.';
END
GO

-- ── V5: contact_phones + contact_emails + title ───────────
IF NOT EXISTS (
    SELECT * FROM sys.columns
    WHERE object_id = OBJECT_ID('contacts') AND name = 'title'
)
BEGIN
    ALTER TABLE contacts ADD title NVARCHAR(100) NULL;
    PRINT 'Column contacts.title added.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'contact_phones')
BEGIN
    CREATE TABLE contact_phones (
        id         BIGINT IDENTITY(1,1) PRIMARY KEY,
        contact_id BIGINT        NOT NULL,
        label      NVARCHAR(50)  NOT NULL DEFAULT 'mobile',
        number     NVARCHAR(50)  NOT NULL,
        CONSTRAINT fk_cphones_contact
            FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
    );
    CREATE INDEX idx_cphones_contact ON contact_phones (contact_id);
    PRINT 'Table contact_phones created.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'contact_emails')
BEGIN
    CREATE TABLE contact_emails (
        id         BIGINT IDENTITY(1,1) PRIMARY KEY,
        contact_id BIGINT        NOT NULL,
        label      NVARCHAR(50)  NOT NULL DEFAULT 'personal',
        address    NVARCHAR(255) NOT NULL,
        CONSTRAINT fk_cemails_contact
            FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
    );
    CREATE INDEX idx_cemails_contact ON contact_emails (contact_id);
    PRINT 'Table contact_emails created.';
END
GO

-- ── App user ──────────────────────────────────────────────
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'contact_app_user')
BEGIN
    DECLARE @sql NVARCHAR(500) = N'CREATE LOGIN contact_app_user WITH PASSWORD = ''' +
        REPLACE(CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR), '.', '') + N'AppUser@2024!''';
    -- Use the actual password from env instead
    EXEC sp_executesql N'
        IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = ''contact_app_user'')
        BEGIN
            CREATE LOGIN contact_app_user WITH PASSWORD = ''AppUser@2024!'';
        END';
    PRINT 'Login contact_app_user created.';
END
GO

USE contact_management_db;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'contact_app_user')
BEGIN
    CREATE USER contact_app_user FOR LOGIN contact_app_user;
    GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO contact_app_user;
    PRINT 'User contact_app_user created and granted permissions.';
END
GO

PRINT 'Database initialisation complete.';
GO
