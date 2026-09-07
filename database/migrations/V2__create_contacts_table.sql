-- ============================================================
-- Migration V2: Create contacts table
-- ============================================================

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
            FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
    );

    CREATE INDEX idx_contacts_user_id   ON contacts (user_id);
    CREATE INDEX idx_contacts_email     ON contacts (email);
    CREATE INDEX idx_contacts_last_name ON contacts (last_name);
END;
