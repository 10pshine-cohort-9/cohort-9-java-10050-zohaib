-- ============================================================
-- Migration V3: Add updated_at triggers for automatic timestamps
-- ============================================================

-- Trigger for users table
IF OBJECT_ID('trg_users_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER trg_users_updated_at;
GO

CREATE TRIGGER trg_users_updated_at
ON users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE users
    SET updated_at = SYSDATETIME()
    FROM users u
    INNER JOIN inserted i ON u.id = i.id;
END;
GO

-- Trigger for contacts table
IF OBJECT_ID('trg_contacts_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER trg_contacts_updated_at;
GO

CREATE TRIGGER trg_contacts_updated_at
ON contacts
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE contacts
    SET updated_at = SYSDATETIME()
    FROM contacts c
    INNER JOIN inserted i ON c.id = i.id;
END;
GO
